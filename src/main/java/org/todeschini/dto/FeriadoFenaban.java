package org.todeschini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeriadoFenaban {

    private LocalDate date;
    private String diaSemana;
    private String descricao;
}
