package br.org.cni.sgt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.org.cni.sgt.utils.HTTPS;

public class CallWebServiceCEP {
	
	public EnderecoDTO call(String cep) throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		HTTPS.k();
		
		URL ws = new URL("https://apps.correios.com.br/SigepMasterJPA/AtendeClienteService/AtendeCliente?wsdl");
		HttpsURLConnection connectionWS = (HttpsURLConnection) ws.openConnection();
		
		connectionWS.setDoOutput(true);
		connectionWS.setDoInput(true);
		connectionWS.setRequestMethod("POST");
		connectionWS.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

		StringBuilder envelope = new StringBuilder() 
		.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cli=\"http://cliente.bean.master.sigep.bsb.correios.com.br/\">")
		.append("   <soapenv:Header/>")
		.append("   <soapenv:Body>")
		.append("      <cli:consultaCEP>")
		.append("         <cep>").append(cep).append("</cep>")
		.append("     </cli:consultaCEP>")
		.append("   </soapenv:Body>")
		.append("</soapenv:Envelope>");
		
		OutputStreamWriter osw = new OutputStreamWriter( connectionWS.getOutputStream() );
		osw.write( envelope.toString() );
		osw.flush();
		
        BufferedReader wsReader = null;
        StringBuilder xmlString = new StringBuilder(1024);
        try {
        	
        	wsReader = new BufferedReader(new InputStreamReader(connectionWS.getInputStream(), StandardCharsets.UTF_8 ));
            String line = wsReader.readLine();
            while (line != null) {
            	xmlString.append( line );
                line = wsReader.readLine();
            }
        } catch (IOException io) {
            System.out.println("Failed to read from Stream");
            io.printStackTrace();
        } 
        
        System.out.println( xmlString.toString() );
//		
//		String outFile = "";
//		byte[] byteBuf = new byte[1024];
////        FileOutputStream outStream = new FileOutputStream(outFile);
////        OutputStreamWriter outStream = new OutputStreamWriter( connectionWS.getInputStream() );
//        InputStream resStream = connectionWS.getInputStream();
//        int resLen = 0;
//        int len = resStream.read(byteBuf);
//        while (len > -1) {
//           resLen += len;
//           outStream.write(byteBuf,0,len);
//           len = resStream.read(byteBuf);
//        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream( xmlString.toString().getBytes(StandardCharsets.UTF_8) ) );
		
		XPathFactory xpathfactory = XPathFactory.newInstance();
		XPath xpath = xpathfactory.newXPath();
		XPathExpression expr = xpath.compile("/*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='consultaCEPResponse']/*[local-name()='return']//*");
		
		Object returnXML = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) returnXML;
		
//		System.out.println("Found " + nodes.getLength() + " matches");
		
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
//		           String complemento2,
				nodes.item(4).getTextContent(),
//		           String end,
				nodes.item(5).getTextContent(),
//		           String uf) {
				nodes.item(7).getTextContent() // O 6 seria o id não é necessario
		    );
		
		System.out.println( enderecoCEP );
		
		return enderecoCEP;
	}
	
	public static void main(String[] args) {
		CallWebServiceCEP m = new CallWebServiceCEP();
		try {
			Object o = m.call("88090350");
			System.out.println( o.toString() );
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
