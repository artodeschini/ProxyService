package org.todeschini.ibge;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.todeschini.dto.MuniciopioIbge;
import org.todeschini.dto.EstadoIbge;
import org.todeschini.exception.IbgeCrawlerException;
import org.todeschini.utils.HTTPS;
import org.todeschini.utils.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

@ApplicationScoped
public class IbgeCrawler {

    private Logger LOGGER = Logger.getLogger(IbgeCrawler.class.getName());

    private static final String URL_IBGE = "https://www.ibge.gov.br/explica/codigos-dos-municipios.php";

    private Map<String, EstadoIbge> ESTATOS = new HashMap<>();

    private static final String FILE_NAME_HTML_SAVE = "file-ibge-page-{0}.html";



    public String openPage() {
        try {
            HTTPS.k();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IbgeCrawlerException("erro ao ler dados da pagina do ibge desconsiderando https k", e);
        }

        var html = new StringBuilder();

        try {
            var url = new URL(URL_IBGE);
            var connection = (HttpsURLConnection) url.openConnection();

            var reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line = reader.readLine();

            while (line != null) {
                html.append(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            LOGGER.severe("erro ao ler os dados da pagina do ibge");
            throw new IbgeCrawlerException("erro ao ler dados da pagina do ibge ", e);
        }

        return html.toString();
    }

    private void createArquivoIbge() {
        try {
            Files.write(Paths.get(this.getFileName()), this.openPage().getBytes());
        } catch (IOException e) {
            LOGGER.severe("erro ao gravar dados da pagina do ibge em arquivio");
            throw new IbgeCrawlerException("erro ao gravar dados da pagina do ibge em arquivio", e);
        }
    }

    private String getFileName() {
        return System.getProperty("user.home").concat("/").concat(format(FILE_NAME_HTML_SAVE, LocalDate.now()));
    }

    private void limparCache() {
        ESTATOS.values().forEach(e -> e.getMuniciopios().clear());
        ESTATOS = new HashMap<>();
        System.gc();
    }

    public File getArquivoIbge() {
        String path = this.getFileName();
        var f = new File(path);

        if (f.exists() && !f.isDirectory()) {
            return f;

        } else {
            this.createArquivoIbge();

            // limprar o cache do dia
            this.limparCache(); // cidades

            return new File(path);
        }
    }

    public List<MuniciopioIbge> todosMunicipoiosPorEstado(String uf) {
        this.findMunicipioIbge(uf, "Qualquer coisa");

        return ESTATOS.get(uf).getMuniciopios();
    }

    public Map<String, EstadoIbge> getEstados() {
        if (ESTATOS.size() == 0 ) {
            this.crawler();
        }

        return ESTATOS;
    }

    private void crawler() {
        // Zera o cache
        this.limparCache();

        var html = new StringBuilder();

        try {

            var reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.getArquivoIbge())));
            String line = reader.readLine();
            while (line != null) {
                html.append(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            LOGGER.severe("erro ao ler dados do arquivo da pagina do ibge ");
            throw new IbgeCrawlerException("erro ao ler dados do arquivo da pagina do ibge ", e);
        }

        LOGGER.info("FIM DA LEITURA DO ARQUIVO COMECANDO O PROCESSO DE CRAWLER");

        var doc = Jsoup.parse(html.toString());
        var tables = doc.select("table[class=container-uf]");
        var estados = tables.get(0);

        var descricoes = estados.select("tr");

        EstadoIbge estado = null;
        Elements tds = null;

        for (int i = 1; i < descricoes.size(); i++) {
            estado = new EstadoIbge();
            estado.setIndex(i);

            tds = descricoes.get(i).children();

            // nome do estado
            //System.out.println(tds);
            //System.out.println(tds.get(0).child(0).text());
            estado.setNome(tds.get(0).child(0).text());

            // sigla do estado
            //System.out.println(tds.get(0).child(0).attr("href").replace("#", ""));
            estado.setSigla(tds.get(0).child(0).attr("href").replace("#", "").toUpperCase());

            // codigo
            //System.out.println("mostra o codigo");
            //System.out.println(tds.get(1).child(0).text().replaceAll("[^\\d.]", ""));
            estado.setCodigo(Integer.parseInt(tds.get(1).child(0).text().replaceAll("[^\\d.]", "")));
            ESTATOS.put(estado.getSigla(), estado);
        }

        String uf = null;
        MuniciopioIbge muncipio = null;
        estado = null;

        LOGGER.info("FIM DA CARGA DE ESTADOS PELO CRAWLER");
        for (var index = 1; index < tables.size(); index++) {

            var thead = tables.get(index).select("thead").first();

            // pega a sigla do estado
            //System.out.println(theadEstado.attr("id"));
            uf = thead.attr("id").toUpperCase();

            LOGGER.info(format("INICIANDO A EXTRACAO DAS CIDADES DO ESTADO {0}", uf));

            estado = ESTATOS.get(uf);

            var municipios = tables.get(index).select("tr");

            // 0 nao precisa e o cabecario da tabela de cidades
            for (var i = 1; i < municipios.size(); i++) {
                muncipio = new MuniciopioIbge();

                tds = municipios.get(i).children();

                // nome da cidade
                //System.out.println(tds.get(0).text());
                muncipio.setNome(tds.get(0).text());

                // normalize para encontrar as cidades por nome sem acentuacao
                muncipio.setNormalize(muncipio.getNome());

                // codigo
                //System.out.println(tds.get(1).text());
                muncipio.setCodigo(tds.get(1).text());
                muncipio.setUf(estado.getSigla());

                estado.getMuniciopios().add(muncipio);
            }
            LOGGER.info(format("FINALIZADO A EXTRACAO DAS CIDADES DO ESTADO {0} com {1} cidades.", uf, estado.getMuniciopios().size()));
        }
        System.gc();
    }

    public Optional<MuniciopioIbge> findMunicipioIbge(String uf, String municipio) {
        uf = uf.toUpperCase();

        var estado = ESTATOS.get(uf);

        if (estado == null) {
            this.crawler();
        }

        estado = ESTATOS.get(uf);

        if (estado == null) {
            throw new IbgeCrawlerException("Erro ao obter estado pela sigla");
        }

        String busca = StringUtils.normalize(municipio);

        LOGGER.info("Realizando a busca em memoria agora do municipio");
        return estado.getMuniciopios().stream().filter(m -> m.getNormalize().equals(busca)).findFirst();
    }

//    public static void main(String[] args) {
//        var ibge = new IbgeCrawler();
//
//        var municipio = ibge.findMunicipioIbge("SC", "São José");
//        System.out.println(municipio.get().getNome());
//        var poa = ibge.findMunicipioIbge("RS", "Porto Alegre");
//        System.out.println(poa.get().getNome());
//
//    }
}