package org.todeschini.correios;

import org.todeschini.dto.MuniciopioIbge;
import org.todeschini.exception.CorreiosServiceException;
import org.todeschini.dto.EnderecoDTO;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class ProxyWebServiceCorreios {

	@Inject
	IbgeCrawler crawler;
	
	public EnderecoDTO call(String cep) throws CorreiosServiceException {
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
				.append("         <cep>").append(cep).append("</cep>")
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
			var osw = new OutputStreamWriter( connectionWS.getOutputStream() );
			osw.write( envelope );
			osw.flush();

			// le resposta envelope soap
			BufferedReader wsReader = null;

			try {

				wsReader = new BufferedReader(new InputStreamReader(connectionWS.getInputStream(), StandardCharsets.UTF_8 ));
				String line = wsReader.readLine();
				while (line != null) {
					xml.append( line );
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
			var doc = builder.parse(new ByteArrayInputStream( xml.toString().getBytes(StandardCharsets.UTF_8)));
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
		
		EnderecoDTO enderecoCEP = new EnderecoDTO(
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
	
//	public static void main(String[] args) {
//		ProxyWebServiceCorreios m = new ProxyWebServiceCorreios();
//		System.out.println(m.call("88090352"));
//	}

}
