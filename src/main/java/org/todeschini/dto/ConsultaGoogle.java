package org.todeschini.dto;

import lombok.Data;

@Data
public class ConsultaGoogle {

    private String origem;
    private int numeroOrigem;
    private String destino;
    private int numeroDestino;
    private String key;
}
