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

@ApplicationScoped
@Slf4j
public class ProxyWebServiceCorreios {

    @Inject
    IbgeCrawler crawler;// = new IbgeCrawler();

    /**
     * Este metodo com o wsdl agora requer autenticacao
     * @param cep
     * @return
     * @throws CorreiosServiceException
     */
    @Deprecated
    public EnderecoCep call(String cep) throws CorreiosServiceException {
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
                .append("         <cep>").append(cep.replaceAll("[^\\d.]", "")).append("</cep>")
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
                //System.out.println("Failed to read from Stream");
                log.error("Falha ao ler o InputStream ".concat(io.getMessage()), io);
                //io.printStackTrace();
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
        cep = cep.replaceAll("[^0-9]+","");
        log.info("Busca por cep ".concat(cep));

        var body = new StringBuilder("pagina=%2Fapp%2Fendereco%2Findex.php&cepaux=&mensagem_alerta=&endereco=")
                .append(cep).append("&tipoCEP=LOG");

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://buscacepinter.correios.com.br/app/endereco/carrega-cep-endereco.php"))
                //.POST(BodyPublishers.ofString("pagina=%2Fapp%2Fendereco%2Findex.php&cepaux=&mensagem_alerta=&endereco=88090350&tipoCEP=LOG"))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .setHeader("accept", "*/*")
                .setHeader("accept-language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .setHeader("cache-control", "no-store, no-cache, must-revalidate")
                .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                //.setHeader("cookie", "_ga=GA1.3.887944617.1711682292; _gid=GA1.3.1273195035.1711682296; rxVisitor=1711682303310C7S7RSSKJE9N3NPBA66601MEG2LO8EUV; rxvt=1711684103323|1711682303310; dtCookie=v_4_srv_1_sn_RBS8EN9H569H0T1HBAHDJJ5QM2I5EHVL_app-3A47fca5cf7bd2003c_1_ol_0_perc_100000_mul_1; dtSa=true%7CC%7C-1%7CBusca%20CEP%20ou%20Endere%C3%A7o%7C-%7C1711682353752%7C482303309_298%7Chttps%3A%2F%2Fwww.correios.com.br%2F%7C%7C%7C%7C; INGRESSCOOKIE=1711682355.215.10718.823969|a18ecd9549ddde51e3f47ee40455f85d; buscacep=v7fhph7naq8tk60cjl9n4t54fl; LBprdint2=1614413834.47873.0000; LBprdExt1=701038602.47873.0000; _ga_J59GSF3WW5=GS1.1.1711682291.1.1.1711682353.60.0.0; dtPC=1$482303309_298h-vVMEHHARALKMPSEDPIMEFGLPMFWNNHOSR-0e0")
                .setHeader("origin", "https://buscacepinter.correios.com.br")
                .setHeader("referer", "https://buscacepinter.correios.com.br/app/endereco/index.php")
                //.setHeader("sec-ch-ua", "\"Google Chrome\";v=\"123\", \"Not:A-Brand\";v=\"8\", \"Chromium\";v=\"123\"")
                //.setHeader("sec-ch-ua-mobile", "?0")
                //.setHeader("sec-ch-ua-platform", "\"macOS\"")
                //.setHeader("sec-fetch-dest", "empty")
                //.setHeader("sec-fetch-mode", "cors")
                //.setHeader("sec-fetch-site", "same-origin")
                //.setHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new CorreiosServiceException("Erro ao abrir o crawler do correios", e);
        } catch (InterruptedException e) {
            throw new CorreiosServiceException("Erro ao abrir o crawler do correios", e);
        }

        log.info("retorno do crawler >> " + response.body());

        var endereco = new EnderecoCep();

        var json = new JsonObject(response.body().toString());
        if (!json.getBoolean("erro")) {
            var array = json.getJsonArray("dados");
            var end = array.getJsonObject(0);
            /* var logradouroComplemento = dados.getString("logradouroDNEC");
            var has  = logradouroComplemento.indexOf("-") > 0;
            endereco = EnderecoCep.builder()
                    .uf(dados.getString("uf"))
                    .cidade(dados.getString("localidade"))
                    .end(has ? logradouroComplemento.split("-")[0].trim() : logradouroComplemento)
                    .complemento(has ? logradouroComplemento.split("-")[1].trim() : "")
                    .bairro(dados.getString("bairro"))
                    .cep(dados.getString("cep"))
                    .build();

            Optional<MuniciopioIbge> municiopioIbge = crawler.findMunicipioIbge(endereco.getUf(), endereco.getCidade());

            if (municiopioIbge.isPresent()) {
                endereco.setIbge(municiopioIbge.get().getCodigo());
            } */
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

        //Rua%20Felipe%20Schmidt&tipoCEP=ALL\""
        log.info("Realizando busca pelo crawler do correiros >>> " + busca.toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://buscacepinter.correios.com.br/app/endereco/carrega-cep-endereco.php"))
                .POST(HttpRequest.BodyPublishers.ofString(busca.toString()))
                .setHeader("accept", "*/*")
                .setHeader("accept-language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .setHeader("cache-control", "no-store, no-cache, must-revalidate")
                .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                //.setHeader("cookie", "rxVisitor=1711682303310C7S7RSSKJE9N3NPBA66601MEG2LO8EUV; _gid=GA1.3.81951936.1712287006; dtCookie=v_4_srv_2_sn_HOJVH40PO0C1UOK96E4D0V20PPL6081V_app-3A47fca5cf7bd2003c_1_ol_0_perc_100000_mul_1; INGRESSCOOKIE=1712287042.436.28332.786103|a18ecd9549ddde51e3f47ee40455f85d; buscacep=ptbcd0t4lbl1mn659edpeb56pn; LBprdint2=1278869514.47873.0000; LBprdExt1=701038602.47873.0000; _ga=GA1.3.887944617.1711682292; rxvt=1712288881826|1712287006644; dtPC=2$487081736_790h-vMSFCUGCSDGACPDIWMMUUULCJCGTPLHLP-0e0; dtSa=true%7CC%7C-1%7CBusca%20CEP%20ou%20Endere%C3%A7o%7C-%7C1712287084863%7C487081736_790%7Chttps%3A%2F%2Fwww.correios.com.br%2F%7C%7C%7C%7C; _ga_J59GSF3WW5=GS1.1.1712287005.3.1.1712287084.57.0.0")
                .setHeader("origin", "https://buscacepinter.correios.com.br")
                .setHeader("referer", "https://buscacepinter.correios.com.br/app/endereco/index.php?t")
                //.setHeader("sec-ch-ua", "\"Google Chrome\";v=\"123\", \"Not:A-Brand\";v=\"8\", \"Chromium\";v=\"123\"")
                //.setHeader("sec-ch-ua-mobile", "?0")
                //.setHeader("sec-ch-ua-platform", "\"macOS\"")
                //.setHeader("sec-fetch-dest", "empty")
                //.setHeader("sec-fetch-mode", "cors")
                //.setHeader("sec-fetch-site", "same-origin")
                //.setHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Resposta obtida pela busca por logradouro");
            log.info(response.body());
            //System.out.println(response.body());
            result = new JsonObject(response.body());

            if (!result.getBoolean("erro")) {
                var dados = result.getJsonArray("dados");
                for (int i = 0; i < dados.size(); i++) {
                    var json = dados.getJsonObject(i);
                    /* Optional<MuniciopioIbge> ibge =
                        crawler.findMunicipioIbge(temp.getString("uf"), temp.getString("localidade"));
                    if (ibge.isPresent()) {
                        // temp.put("ibge", ibge.get().getCodigo());
                        codigoCidadeIBGE = ibge.get().getCodigo();
                    } else {
                        codigoCidadeIBGE = null;
                    }
                    var logradouroComplemento = temp.getString("logradouroDNEC");
                    var has  = logradouroComplemento.indexOf("-") > 0;
                    endereco = EnderecoCep.builder()
                            .uf(temp.getString("uf"))
                            .cidade(temp.getString("localidade"))
                            .end(has ? logradouroComplemento.split("-")[0].trim() : logradouroComplemento)
                            .complemento(has ? logradouroComplemento.split("-")[1].trim() : "")
                            .bairro(temp.getString("bairro"))
                            .cep(temp.getString("cep"))
                            .ibge(codigoCidadeIBGE)
                            .build(); */

                    saida.add(this.parseJsonToEnderecoCep(json));
                }
            } else {
                log.error("nao obteve resposta do crawler");
            }

        } catch (IOException e) {
            throw new CorreiosServiceException(e.getMessage(), e);
        } catch (InterruptedException e) {
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

    // TODO https://docs.awesomeapi.com.br/api-cep

//	public static void main(String[] args) {
//		ProxyWebServiceCorreios m = new ProxyWebServiceCorreios();
//		//System.out.println(m.crawlerWebSiteCorreios("88090352"));
//        var s = m.buscaCepPorLogradouro("Rua Santa Rita de Cassia", "Florianopolis");
//        s.forEach(System.out::println);
//	}

}
