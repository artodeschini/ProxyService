package org.todeschini.correios;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.todeschini.dto.MuniciopioIbge;
import org.todeschini.exception.CorreiosServiceException;
import org.todeschini.dto.EnderecoCep;
import org.todeschini.ibge.IbgeCrawler;
import org.todeschini.utils.HTTPS;
import org.todeschini.utils.StringUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.text.MessageFormat.format;

@ApplicationScoped
@Slf4j
public class ProxyWebServiceCorreios {

    @Inject
    IbgeCrawler crawler;

    private final AtomicInteger i = new AtomicInteger(0);

    public EnderecoCep call(String cep) {
        EnderecoCep e = null;
        switch (i.get()) {
            case 1:
                log.info("Executando call viacep ");
                e = callViaCep(cep);
                break;
            case 2:
                log.info("Executando call AwesomeApi ");
                e = callAwesomeApi(cep);
                break;
            default:
                log.info("Executando crawler correios ");
                e = crawlerWebSiteCorreios(cep);
                break;
        }

        i.incrementAndGet();
        if (i.get() > 3) {
            i.set(0);
        }

        return e;
    }

    @Deprecated
    public EnderecoCep callOld(String cep) throws CorreiosServiceException {
        cep = removeNoDigitsFromCep(cep);
        try {
            HTTPS.k();
            log.info("executando nao coferencia de certificados https");
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Erro ao adicionar salta de seguranca ao chamar o webServices metodo k", e);
            throw new CorreiosServiceException("Erro ao adicionar salta de seguranca ao chamar o webServices metodo k", e);
        }

        var envelope = new StringBuilder()
                .append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cli=\"http://cliente.bean.master.sigep.bsb.correios.com.br/\">")
                .append("   <soapenv:Header/>")
                .append("   <soapenv:Body>")
                .append("      <cli:consultaCEP>")
                .append("         <cep>").append(removeNoDigitsFromCep(cep)).append("</cep>")
                .append("     </cli:consultaCEP>")
                .append("   </soapenv:Body>")
                .append("</soapenv:Envelope>").toString();

        var xml = new StringBuilder(1024);
        log.info("envelope soap > ".concat(envelope.toString()));

        try {
            var ws = new URL("https://apps.correios.com.br/SigepMasterJPA/AtendeClienteService/AtendeCliente?wsdl");
            var connectionWS = (HttpsURLConnection) ws.openConnection();

            connectionWS.setDoOutput(true);
            connectionWS.setDoInput(true);
            connectionWS.setRequestMethod("POST");
            connectionWS.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

            // envia envelope soap
            var osw = new OutputStreamWriter(connectionWS.getOutputStream());
            osw.write(envelope);
            osw.flush();

            // le resposta envelope soap
            BufferedReader wsReader = null;

            try {

                wsReader = new BufferedReader(new InputStreamReader(connectionWS.getInputStream(), StandardCharsets.UTF_8));
                String line = wsReader.readLine();
                while (line != null) {
                    xml.append(line);
                    line = wsReader.readLine();
                }
            } catch (IOException io) {
                log.error(format("Falha ao ler o InputStream {0}", io.getMessage()), io);
            }

        } catch (IOException e) {
            log.error("Erro ao enviar envelope soap para o webservice dos correios", e);
            throw new CorreiosServiceException("Erro ao enviar envelope soap para o webservice dos correios", e);
        }

        // decompoem o xml para nodes para tag a tag do envelope soap

        Object returnTagToXml = null;

        try {
            var factory = DocumentBuilderFactory.newInstance();
            var builder = factory.newDocumentBuilder();
            var doc = builder.parse(new ByteArrayInputStream(xml.toString().getBytes(StandardCharsets.UTF_8)));
            var xpathfactory = XPathFactory.newInstance();
            var xpath = xpathfactory.newXPath();

            var expr = xpath.compile("/*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='consultaCEPResponse']/*[local-name()='return']//*");
            returnTagToXml = expr.evaluate(doc, XPathConstants.NODESET);

        } catch (ParserConfigurationException e) {
            throw new CorreiosServiceException("Erro ao decompor o xml com parse", e);
        } catch (SAXException e) {
            throw new CorreiosServiceException("Erro ao decompor o xml com sax", e);
        } catch (IOException e) {
            throw new CorreiosServiceException("Erro ao decompor o xml com io", e);
        } catch (XPathExpressionException e) {
            throw new CorreiosServiceException("Erro ao decompor o xml com xpath", e);
        }

        if (Objects.isNull(returnTagToXml)) {
            throw new CorreiosServiceException("Erro ao decompor o xml");
        }
        NodeList nodes = (NodeList) returnTagToXml;

        // de tempos em tempos o correio muda o index das tags
//		for (int i = 0; i < nodes.getLength(); i++) {
//			System.out.println("nome do nodo xml");
//			System.out.println( nodes.item(i).getNodeName() );
//			System.out.println( "indice do nodo" );
//			System.out.println( i );
//			System.out.println( "conteudo " );
//			System.out.println( nodes.item(i).getTextContent() );
//		}

        EnderecoCep enderecoCEP = new EnderecoCep(
//		           String bairro,
                nodes.item(0).getTextContent(),
//		           String cep,
                nodes.item(1).getTextContent(),
//		           String cidade,
                nodes.item(2).getTextContent(),
//		           String complemento,
                nodes.item(3).getTextContent(),
//		           String rua,
                nodes.item(4).getTextContent(),
//		           String uf,
                nodes.item(5).getTextContent()
        );

        Optional<MuniciopioIbge> municiopioIbge = crawler.findMunicipioIbge(enderecoCEP.getUf(), enderecoCEP.getCidade());

        if (municiopioIbge.isPresent()) {
            enderecoCEP.setIbge(municiopioIbge.get().getCodigo());
        }

        return enderecoCEP;
    }

    public EnderecoCep crawlerWebSiteCorreios(String cep) {
        // convertido do curl para java no site abaixo
        /// https://curlconverter.com/java/
        cep = removeNoDigitsFromCep(cep);
        log.info("Busca por cep ".concat(cep));

        var body = new StringBuilder("pagina=%2Fapp%2Fendereco%2Findex.php&cepaux=&mensagem_alerta=&endereco=")
                .append(cep).append("&tipoCEP=LOG");

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://buscacepinter.correios.com.br/app/endereco/carrega-cep-endereco.php"))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .setHeader("accept", "*/*")
                .setHeader("accept-language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .setHeader("cache-control", "no-store, no-cache, must-revalidate")
                .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .setHeader("origin", "https://buscacepinter.correios.com.br")
                .setHeader("referer", "https://buscacepinter.correios.com.br/app/endereco/index.php")
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new CorreiosServiceException("Erro ao abrir o crawler do correios", e);
        }

        // log.info("retorno do crawler >> " + response.body());

        var endereco = new EnderecoCep();

        var json = new JsonObject(response.body().toString());
        if (!json.getBoolean("erro")) {
            var array = json.getJsonArray("dados");
            var end = array.getJsonObject(0);

            return this.parseJsonToEnderecoCep(end);
        }

        log.info("endereo resultante " + endereco.toString());

        return endereco;
    }

    public List<EnderecoCep> buscaCepPorLogradouro(String logradouro, String cidade) {
        HttpClient client = HttpClient.newHttpClient();
        JsonObject result = null;

        var saida = new ArrayList<EnderecoCep>();

        var busca = new StringBuilder("pagina=%2Fapp%2Fendereco%2Findex.php&cepaux=&mensagem_alerta=&endereco=")
                ///Rua%20Santa%20Rita%20de%20C%C3%A1ssia%20Florianopolis&tipoCEP=ALL\"")
                .append(logradouro.replaceAll(" ", "+"));
        if (cidade != null) {
            cidade = StringUtils.normalize(cidade);
            busca.append("+").append(cidade.replaceAll(" ", "+"));
        }

        busca.append("&tipoCEP=ALL");

        log.info("Realizando busca pelo crawler do correiros >>> " + busca.toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://buscacepinter.correios.com.br/app/endereco/carrega-cep-endereco.php"))
                .POST(HttpRequest.BodyPublishers.ofString(busca.toString()))
                .setHeader("accept", "*/*")
                .setHeader("accept-language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .setHeader("cache-control", "no-store, no-cache, must-revalidate")
                .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .setHeader("origin", "https://buscacepinter.correios.com.br")
                .setHeader("referer", "https://buscacepinter.correios.com.br/app/endereco/index.php?t")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Resposta obtida pela busca por logradouro");
            log.info(response.body());

            result = new JsonObject(response.body());

            if (!result.getBoolean("erro")) {
                var dados = result.getJsonArray("dados");
                for (int i = 0; i < dados.size(); i++) {
                    var json = dados.getJsonObject(i);
                    saida.add(this.parseJsonToEnderecoCep(json));
                }
            } else {
                log.error("nao obteve resposta do crawler");
            }

        } catch (IOException | InterruptedException e) {
            throw new CorreiosServiceException(e.getMessage(), e);
        }

        return saida;
    }

    private EnderecoCep parseJsonToEnderecoCep(JsonObject json) {
        Optional<MuniciopioIbge> ibge =
                crawler.findMunicipioIbge(json.getString("uf"), json.getString("localidade"));

        String codigoCidadeIBGE = null;

        if (ibge.isPresent()) {
            codigoCidadeIBGE = ibge.get().getCodigo();
        } else {
            codigoCidadeIBGE = null;
        }
        var logradouroComplemento = json.getString("logradouroDNEC");
        var has = logradouroComplemento.indexOf("-") > 0;

        return EnderecoCep.builder()
                .uf(json.getString("uf"))
                .cidade(json.getString("localidade"))
                .end(has ? logradouroComplemento.split("-")[0].trim() : logradouroComplemento)
                .complemento(has ? logradouroComplemento.split("-")[1].trim() : "")
                .bairro(json.getString("bairro"))
                .cep(json.getString("cep"))
                .ibge(codigoCidadeIBGE)
                .build();
    }

    private String removeNoDigitsFromCep(String cep) {
        return cep.replaceAll("[^0-9]+","");
    }

    public EnderecoCep callViaCep(String cep) {
        cep = removeNoDigitsFromCep(cep);
        HttpClient client = HttpClient.newHttpClient();
        var url = format("https://viacep.com.br/ws/{0}/json/", cep);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var json = new JsonObject(response.body());

            return EnderecoCep.builder()
                    .cep(cep)
                    .uf(json.getString("uf"))
                    .cidade(json.getString("localidade"))
                    .bairro(json.getString("bairro"))
                    .ibge(json.getString("ibge"))
                    .end(json.getString("logradouro"))
                    .complemento(json.getString("complemento"))
                    .build();

        } catch (Exception e) {
            throw new CorreiosServiceException(format("Erro ao chamar a url {0} pela api ViaCep" , url));
        }
    }

    public EnderecoCep callAwesomeApi(String cep) {
        cep = removeNoDigitsFromCep(cep);
        HttpClient client = HttpClient.newHttpClient();
        var url = format("https://cep.awesomeapi.com.br/json/{0}", cep);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var json = new JsonObject(response.body());

            return EnderecoCep.builder()
                    .cep(cep)
                    .uf(json.getString("state"))
                    .cidade(json.getString("city"))
                    .bairro(json.getString("district"))
                    .ibge(json.getString("city_ibge"))
                    .end(json.getString("address"))
                    .build();

        } catch (Exception e) {
            throw new CorreiosServiceException(format("Erro ao chamar a url {0} pela api AwesomeApi" , url));
        }
    }
}
