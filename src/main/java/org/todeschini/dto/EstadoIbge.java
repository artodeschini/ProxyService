package org.todeschini.dto;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EstadoIbge {

    private String nome;
    private String sigla;
    private Integer codigo;
    private Integer index;

    @JsonManagedReference
    private List<MuniciopioIbge> municiopios = new ArrayList<>();
}
