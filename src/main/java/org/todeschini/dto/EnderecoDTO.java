package org.todeschini.dto;

import java.io.Serializable;

public class EnderecoDTO implements Serializable {

    private String bairro;
    private String cep;
    private String cidade;
    private String complemento;
    private String end;
    private String uf;

    private String ibge;

    public EnderecoDTO() {
    }

    public EnderecoDTO(String bairro, String cep, String cidade, String complemento, String end, String uf) {
        this.bairro = bairro;
        this.cep = cep;
        this.cidade = cidade;
        this.complemento = complemento;
        this.end = end;
        this.uf = uf;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getIbge() {
        return ibge;
    }

    public void setIbge(String ibge) {
        this.ibge = ibge;
    }
}
