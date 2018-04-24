package br.org.cni.sgt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class CallWSCepCorreiosOLD {
	
	public EnderecoDTO call(String cep) throws NoSuchAlgorithmException, KeyManagementException, IOException, UnsupportedOperationException, SOAPException, TransformerException, ParserConfigurationException, SAXException, XPathExpressionException {
		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection soapConnection = soapConnectionFactory.createConnection();

		String url = "https://apps.correios.com.br/SigepMasterJPA/AtendeClienteService/AtendeCliente?wsdl";

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessageRequest = messageFactory.createMessage();
		SOAPPart soapPart = soapMessageRequest.getSOAPPart();

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration("cli", "http://cliente.bean.master.sigep.bsb.correios.com.br/");
		
		// SOAP Body
		SOAPBody body = envelope.getBody();

		// Dados
		QName bodyName = envelope.createQName("consultaCEP", "cli");
		SOAPBodyElement q = body.addBodyElement(bodyName);
		SOAPElement soapCep = q.addChildElement("cep");
		soapCep.addTextNode( cep );
		
		soapMessageRequest.saveChanges();

//		/* Print the request message */
//		System.out.print("Request SOAP Message = ");
//		soapMessageRequest.writeTo(System.out);
//		System.out.println();

		//faz a chamada
		SOAPMessage soapResponse = soapConnection.call(soapMessageRequest, url);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		Source sourceContent = soapResponse.getSOAPPart().getContent();
		StringWriter write = new StringWriter();
		StreamResult result = new StreamResult(write);
		transformer.transform(sourceContent, result);
		
		
		String xmlString = result.getWriter().toString();
		System.out.println( xmlString );
//		String strUTF8 = new String( xmlString.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8 );
//		System.out.println( "UTF8 out >>>>>>>>>>>>>>>>>>>>>>>>>>>");
//		System.out.println( strUTF8 );
		 
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false); // never forget this!
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream( xmlString.getBytes(StandardCharsets.UTF_8) ) );
		
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
		soapConnection.close();
		
		return enderecoCEP;

	}

}
