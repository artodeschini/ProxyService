package org.todeschini.dto;


import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;


public class EstadoIbge {

    private String nome;
    private String sigla;
    private Integer codigo;
    private Integer index;

    @JsonManagedReference
    private List<MuniciopioIbge> municiopios = new ArrayList<>();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public List<MuniciopioIbge> getMuniciopios() {
        return municiopios;
    }
}
