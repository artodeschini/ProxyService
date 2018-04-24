package br.org.cni.sgt;

import java.io.Serializable;

import br.org.cni.sgt.utils.StringUtils;

public class CidadeSearchDTO implements Serializable {
	
	private static final long serialVersionUID = 3297354926265085097L;
	
	private String codigo, nome, uf, nomefmt, id;
	
	public CidadeSearchDTO(String codigo, String nome, String uf, String id) {
		this.setCodigo(codigo);
		this.setNome(nome);
		this.setUf(uf);
		this.setId(id);
	}
	
	public CidadeSearchDTO(String codigo, String nome, String uf) {
		this.setCodigo(codigo);
		this.setNome(nome);
		this.setUf(uf);
		this.setId("");
	}
	
	public CidadeSearchDTO() {
		this("","","");
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		if ( codigo != null) {
			this.codigo = codigo.trim();
		} else {
			this.codigo = "";
		}
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		if ( nome != null) {
			//this.nome = StringUtils.stringAsciiToUtf8(nome);
			this.nome = nome;
			this.nomefmt = StringUtils.normalize(this.nome).toUpperCase();
		} else {
			this.nome = "";
		}
	}

	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		if ( uf != null) {
			this.uf = uf.toUpperCase().trim();
		} else {
			this.uf = "";
		}
	}
	
	public String getNomefmt() {
		return nomefmt;
	}

	public void setNomefmt(String nomefmt) {
		this.nomefmt = nomefmt;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
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
		CidadeSearchDTO other = (CidadeSearchDTO) obj;
		if (codigo == null) {
			if (other.codigo != null)
				return false;
		} else if (!codigo.equals(other.codigo))
			return false;
		return true;
	}
	
	public String keyBusca() {
		return StringUtils.chaveBuscaCidade(this.nome, this.uf);
	}
	
//	@Deprecated
//	public String toJson() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("{\"codigo\":\"")
//		.append( this.getCodigo() )
//		.append("\",\"nome\":\"" )
//		.append( this.getNome() )
//		.append("\",\"uf\":\"" )
//		.append( this.getUf() )
//		.append("\"}" );
//		
//		return sb.toString();
//	}
	
//	@Deprecated
//	public String toXml() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("")
//		.append("<cidade>")
//		.append("<codigo>")
//		.append( this.getCodigo() )
//		.append("</codigo>")
//		.append("<nome>");
//		//byte ptext[] = myString.getBytes();
//		//String value = new String(ptext, "UTF-8");
//		sb.append(  this.getNome() );
////		sb.append(  new String( this.getNome().getBytes(ISO_8859_1), UTF_8 ) );
//		sb.append("</nome>")
//		.append("<uf>" )
//		.append( this.getUf() )
//		.append("</uf>" )
//		.append("</cidade>");
//		
//		return sb.toString();
//	}

	@Override
	public String toString() {
		return "Cidade [codigo=" + codigo + ", nome=" + nome + ", uf=" + uf + "]";
	}
}
