package org.todeschini.ibge;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.todeschini.dto.MuniciopioIbge;
import org.todeschini.dto.EstadoIbge;
import org.todeschini.exception.IbgeCrawlerException;
import org.todeschini.utils.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class IbgeCrawler {

    private Logger LOGGER = Logger.getLogger(IbgeCrawler.class.getName());

    private static final String URL_IBGE = "https://www.ibge.gov.br/explica/codigos-dos-municipios.php";

    private Map<String, EstadoIbge> ESTATOS = new HashMap<>();

    private static final String FILE_NAME_HTML_SAVE = "file-ibge-page-{0}.html";

    public String openPage() throws IOException {
        var url = new URL(URL_IBGE);
        var connection = (HttpsURLConnection) url.openConnection();

        var html = new StringBuilder();

        try {
            var wsReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line = wsReader.readLine();
            while (line != null) {
                html.append(line);
                line = wsReader.readLine();
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
        return System.getProperty("user.home").concat("/").concat(MessageFormat.format(FILE_NAME_HTML_SAVE, LocalDate.now()));
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

    public Optional<MuniciopioIbge> findMunicipioIbge(String uf, String municipio) {
        Optional<MuniciopioIbge> find = Optional.empty();

        var html = new StringBuilder();
        try {

            var wsReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.getArquivoIbge())));
            String line = wsReader.readLine();
            while (line != null) {
                html.append(line);
                line = wsReader.readLine();
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

        EstadoIbge estado = ESTATOS.get(uf);

        Elements tds = null;
        //ESTATOS.clear();

        if (estado == null) {
            LOGGER.info("Extraindo os estados pelo CRAWLER");
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
                estado.setSigla(tds.get(0).child(0).attr("href").replace("#", ""));

                // codigo
                //System.out.println("mostra o codigo");
                //System.out.println(tds.get(1).child(0).text().replaceAll("[^\\d.]", ""));
                estado.setCodigo(Integer.parseInt(tds.get(1).child(0).text().replaceAll("[^\\d.]", "")));
//            System.out.println(estado);
                ESTATOS.put(estado.getSigla(), estado);
            }

            LOGGER.info("FIM DO CRAWLER DE ESTADOS");
        }


        estado = ESTATOS.get(uf);

        if (estado == null) {
            throw new IbgeCrawlerException("Erro ao obter estados pelo CRAWLER");
        }

        tds = null;

        MuniciopioIbge muncipio = null;

        if (estado != null) {
            LOGGER.info( MessageFormat.format("INICIANDO A EXTRACAO DAS CIDADES DO ESTADO {0}", uf));

            if (estado.getMuniciopios().size() == 0) {
                var municipios = tables.get(estado.getIndex()).select("tr");
                //System.out.println(cidades);
                // 0 nao precisa e o cabecario da tabela de cidades
                for (int i = 1; i < municipios.size(); i++) {
                    muncipio = new MuniciopioIbge();

                    tds = municipios.get(i).children();

                    //System.out.println(tds);
                    // nome da cidade
                    //System.out.println(tds.get(0).text());
                    muncipio.setNome(tds.get(0).text());

                    // normalize para encontrar as cidades por nome sem acentuacao
                    muncipio.setNormalize(muncipio.getNome());

                    // codigo
                    //System.out.println(tds.get(1).text());
                    muncipio.setCodigo(tds.get(1).text());

//                    muncipio.setEstado(estado);
                    muncipio.setUf(estado.getSigla());

//                    System.out.println(cidade);

                    estado.getMuniciopios().add(muncipio);
                }
                LOGGER.info( MessageFormat.format("FINALIZADO A EXTRACAO DAS CIDADES DO ESTADO {0} com {1} cidades.", uf, estado.getMuniciopios().size()));
            }

//            System.out.println("eu passei aqui procurando por " + municipio);
            String busca = StringUtils.normalize(municipio);

            LOGGER.info("Realizando a busca em memoria agora do municipio");
            find = estado.getMuniciopios().stream().filter(m -> m.getNormalize().equals(busca)).findFirst();

        } else {
            throw new IbgeCrawlerException("sigla do estado inválida");
        }

        return find;
    }


//    public static void main(String[] args) {
//        var ibge = new IbgeCrawler();
//
//        var municipio = ibge.findMunicipioIbge("SC", "São José");
//        System.out.println(municipio.isPresent());
//        System.out.println(municipio.get().getCodigo());
//        System.out.println(municipio.get().getNome());
//        System.out.println(municipio.get().getEstado().getSigla());
//        System.out.println(municipio.get().getEstado().getNome());

//    }
}