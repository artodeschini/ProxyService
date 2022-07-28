package org.todeschini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoDdd {

    private String uf;
    private String nome;
    private String regiao;
    private List<Integer> codigos = new ArrayList<>();
}
