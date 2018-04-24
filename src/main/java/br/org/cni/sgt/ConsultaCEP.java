package br.org.cni.sgt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.xml.sax.SAXException;

public class ConsultaCEP extends AbstractMediator { 

	@Override
	public boolean mediate(MessageContext mc) {
		
		String b  = mc.getProperty("uri.var.execute").toString();
//		String cep = mc.getProperty("uri.var.cep").toString().replaceAll("\\D", "");
//		String text = mc.getEnvelope().getBody().getFirstElement().getText();
		String cep = mc.getEnvelope().getBody().getFirstElement().getText().replaceAll("\\D", "");
//		System.out.println(">>>>>>> text " + text );
//		System.out.println(">>>>>>>> CEP" + cep );
//		System.out.println(">>>>>>>>> b " + b);
		
		
		if ( cep.length() == 8 ) {
			try {
				CallWebServiceCEP service = new CallWebServiceCEP();
				EnderecoDTO erp = service.call(cep);
				CidadeSearchDTO dto = new CidadeSearchDTO("", erp.getCidade(), erp.getUf() );
				
				ConsultaCidadeSGT searchSGT = new ConsultaCidadeSGT();
				dto = searchSGT.findCidadeSGT(dto, b);
				
				/*
				 * Objeto para retorno
				 */
				Endereco endereco = new Endereco(
						erp.getBairro(), 
						erp.getCep(), 
						dto != null ? dto.getNome() : erp.getCidade() ,
						dto != null ? dto.getCodigo() : "" ,
								erp.getComplemento(),
								erp.getComplemento2(),
								erp.getEnd(),
								erp.getUf(),
								dto != null ? dto.getId() : "" );
				
				System.out.println( endereco );
				System.out.println( endereco.toJson() );
				mc.setProperty("uri.var.endereco", endereco.toJson() );
				mc.setProperty("uri.var.bairro", endereco.getBairro() );
				mc.setProperty("uri.var.cep", endereco.getCep() );
				mc.setProperty("uri.var.nmCidade", endereco.getNmCidade() );
				mc.setProperty("uri.var.ibge", endereco.getIbge() );
				mc.setProperty("uri.var.complemento", endereco.getComplemento() );
				mc.setProperty("uri.var.complemento2", endereco.getComplemento2() );
				mc.setProperty("uri.var.logradouro", endereco.getLogradouro() );
				mc.setProperty("uri.var.uf", endereco.getUf() );
				mc.setProperty("uri.var.sgt", endereco.getSgt() );
//				String teste = mc.getEnvelope().getBody().getFirstElement().getText();
//				System.out.println( teste );
				
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		ConsultaCEP c = new ConsultaCEP();
		c.mediate( null );
	}
}
