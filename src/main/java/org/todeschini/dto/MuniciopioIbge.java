package org.todeschini.dto;

//import com.fasterxml.jackson.annotation.JsonBackReference;
import org.todeschini.utils.StringUtils;
import java.io.Serializable;

public class MuniciopioIbge implements Serializable {

    private String nome;
    private String normalize;
    private String codigo;
    private String uf;


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNormalize() {
        return normalize;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setNormalize(String normalize) {
        this.normalize = StringUtils.normalize(normalize);
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}
