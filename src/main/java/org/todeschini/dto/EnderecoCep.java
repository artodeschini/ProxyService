package org.todeschini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnderecoCep implements Serializable {

    private String bairro;
    private String cep;
    private String cidade;
    private String complemento;
    private String end;
    private String uf;

    private String ibge;

    public EnderecoCep(String bairro, String cep, String cidade, String complemento, String end, String uf) {
        this.bairro = bairro;
        this.cep = cep;
        this.cidade = cidade;
        this.complemento = complemento;
        this.end = end;
        this.uf = uf;
    }
}
