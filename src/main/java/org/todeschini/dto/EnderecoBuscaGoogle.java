package org.todeschini.dto;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static org.todeschini.utils.StringUtils.removerAcentos;
import static org.todeschini.utils.ReflectionsUtils.getValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class EnderecoBuscaGoogle {

    private String bairro;
    private String cep;
    private String cidade;
    private String end;
    private String numero;
//    private Double latitude;
//    private Double longitude;

    public static EnderecoBuscaGoogle toEnderecoCepToEnderecoBuscaGoogle(EnderecoCep from, String numero) {
        return EnderecoBuscaGoogle.builder()
                .cep(from.getCep().trim())
                .cidade(removerAcentos(from.getCidade()) + " - " + from.getUf().trim())
                .bairro(removerAcentos(from.getBairro().trim()))
                .end(removerAcentos(from.getEnd()))
                .numero(numero)
                .build();
    }

    public JsonObject toJsonObject() {
        var json = new JsonObject();

        // nao enviar esse campos na saida
        var notSend = Arrays.asList("log", "latitude", "longitude");

//        users.stream().filter(
//                u -> ! departmentIdList.contains(u.getDepartmentId())
//        ).collect(Collectors.toList())

        var fields = EnderecoBuscaGoogle.class.getDeclaredFields();
        Arrays.stream(fields).filter(f -> !notSend.contains(f.getName())).forEach(f -> {
            json.put(f.getName(), getValue(f, this));
        });

        return json;
    }
}