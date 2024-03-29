package org.todeschini.correios;

import io.vertx.core.json.JsonObject;
import org.todeschini.dto.MuniciopioIbge;
import org.todeschini.exception.CorreiosServiceException;
import org.todeschini.dto.EnderecoCep;
import org.todeschini.ibge.IbgeCrawler;
import org.todeschini.utils.HTTPS;
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
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class ProxyWebServiceCorreios {

    @Inject
    IbgeCrawler crawler;

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
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
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
                io.printStackTrace();
            }

        } catch (IOException e) {
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
        cep = cep.replaceAll("","");

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
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(response.body());

        var endereco = new EnderecoCep();

        var json = new JsonObject(response.body().toString());
        if (!json.getBoolean("erro")) {
            var array = json.getJsonArray("dados");
            var dados = array.getJsonObject(0);
            endereco = EnderecoCep.builder()
                    .uf(dados.getString("uf"))
                    .cidade(dados.getString("localidade"))
                    .end(dados.getString("logradouroDNEC"))
                    .bairro(dados.getString("bairro"))
                    .cep(dados.getString("cep"))
                    .build();

            Optional<MuniciopioIbge> municiopioIbge = crawler.findMunicipioIbge(endereco.getUf(), endereco.getCidade());

            if (municiopioIbge.isPresent()) {
                endereco.setIbge(municiopioIbge.get().getCodigo());
            }
        }

        return endereco;
    }

//	public static void main(String[] args) {
//		ProxyWebServiceCorreios m = new ProxyWebServiceCorreios();
//		System.out.println(m.crawlerWebSiteCorreios("88090352"));
//	}

}
