package org.todeschini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.todeschini.utils.StringUtils.removerAcentos;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnderecoBuscaGoogle {

    private String bairro;
    private String cep;
    private String cidade;
    private String end;
    private String numero;
    private Double latitude;
    private Double longitude;

    public static EnderecoBuscaGoogle toEnderecoCepToEnderecoBuscaGoogle(EnderecoCep from, String numero) {
        return EnderecoBuscaGoogle.builder()
                .cep(from.getCep().trim())
                .cidade(removerAcentos(from.getCidade()) + " - " + from.getUf().trim())
                .bairro(removerAcentos(from.getBairro().trim()))
                .end(removerAcentos(from.getEnd()))
                .numero(numero)
                .build();
    }
}