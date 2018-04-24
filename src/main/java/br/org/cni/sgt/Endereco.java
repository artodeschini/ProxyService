package br.org.cni.sgt;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.json.JSONStringer;
import org.json.JSONWriter;

@XmlRootElement
public class Endereco {
	
	private java.lang.String bairro;
    private java.lang.String cep;
    private java.lang.String nmCidade;
    private java.lang.String ibge;
    private java.lang.String complemento;
    private java.lang.String complemento2;
    private java.lang.String logradouro;
    private java.lang.String uf;
    private java.lang.String sgt;
    
	public Endereco(String bairro, String cep, String nmCidade, String ibge, String complemento, String complemento2, String logradouro, String uf, String sgt) {
		this.setBairro( bairro );
		this.setCep( cep );
		this.setNmCidade( nmCidade );
		this.setIbge( ibge );;
		this.setComplemento( complemento );
		this.setComplemento2( complemento2 );
		this.setLogradouro( logradouro );
		this.setUf( uf );
		this.setSgt(sgt);
	}
	
	public Endereco() {
		this("", "", "", "", "", "", "", "", "");
	}
	
	public java.lang.String getSgt() {
		return sgt;
	}
	@XmlElement
	public void setSgt(java.lang.String sgt) {
		this.sgt = sgt;
	}
	public java.lang.String getBairro() {
		return bairro;
	}
	@XmlElement
	public void setBairro(java.lang.String bairro) {
		this.bairro = bairro;
	}
	public java.lang.String getCep() {
		return cep;
	}
	@XmlElement
	public void setCep(java.lang.String cep) {
		this.cep = cep;
	}
	public java.lang.String getNmCidade() {
		return nmCidade;
	}
	@XmlElement
	public void setNmCidade(java.lang.String nmCidade) {
		this.nmCidade = nmCidade;
	}
	public java.lang.String getIbge() {
		return ibge;
	}
	@XmlElement
	public void setIbge(java.lang.String ibge) {
		this.ibge = ibge;
	}
	public java.lang.String getComplemento() {
		return complemento;
	}
	@XmlElement
	public void setComplemento(java.lang.String complemento) {
		this.complemento = complemento;
	}
	public java.lang.String getComplemento2() {
		return complemento2;
	}
	@XmlElement
	public void setComplemento2(java.lang.String complemento2) {
		this.complemento2 = complemento2;
	}
	public java.lang.String getLogradouro() {
		return logradouro;
	}
	@XmlElement
	public void setLogradouro(java.lang.String logradouro) {
		this.logradouro = logradouro;
	}
	public java.lang.String getUf() {
		return uf;
	}
	@XmlElement
	public void setUf(java.lang.String uf) {
		this.uf = uf;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cep == null) ? 0 : cep.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Endereco other = (Endereco) obj;
		if (cep == null) {
			if (other.cep != null)
				return false;
		} else if (!cep.equals(other.cep))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Endreco [bairro=" + bairro + ", cep=" + cep + ", nmCidade="
				+ nmCidade + ", ibge=" + ibge + ", complemento=" + complemento
				+ ", complemento2=" + complemento2 + ", logradouro="
				+ logradouro + ", uf=" + uf + ", sgt=" + sgt + "]";
	}
	
	public String toJson() {
		
		JSONWriter json = new JSONStringer()
		.object()
			.key( "bairro" )
			.value( this.getBairro() )
			.key("cep")
			.value( this.getCep() )
			.key("nmCidade")
			.value( this.getNmCidade() )
			.key("ibge")
			.value( this.getIbge() )
			.key("complemento")
			.value( this.getComplemento() )
			.key("complemento2")
			.value( this.getComplemento2() )
			.key("logradouro")
			.value( this.getLogradouro() )
			.key("uf")
			.value( this.getUf() )
			.key("sgt")
			.value( this.getSgt() )
		.endObject();
		
		return json.toString();
	}
}
