package org.todeschini.ddd;

import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.todeschini.dto.EstadoDdd;
import org.todeschini.dto.EstadoIbge;
import org.todeschini.ibge.IbgeCrawler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class EstadosRegioesDDD {

    private static final String SUDESTE = "SUDESTE";
    private static final String NORTE = "NORTE";
    private static final String NORDESTE = "NORDESTE";
    private static final String CENTRO_OESTE = "CENTRO OESTE";
    private static final String SUL = "SUL";

    private Map<String, EstadoDdd> ESTADOS = new HashMap<>();

    void onStart(@Observes StartupEvent ev) {
        log.info("INICIANDO CACHE DE ESTADOS REGIEOS E DDD");
        this.populaCache();
    }

    public void populaCache() {
        ESTADOS.put("AC", EstadoDdd.builder().uf("AC").nome("Acre").regiao(NORTE).codigos(Collections.singletonList(68)).build());

//        Alagoas	AL	Localizado na Região Nordeste
        //– Alagoas (82)
        ESTADOS.put("AL", EstadoDdd.builder().uf("AL").nome("Alagoas").regiao(NORTE).codigos(Collections.singletonList(82)).build());

//        Amapá	AP	Localizado na Região Norte
        //– Amapá (96)
        ESTADOS.put("AP", EstadoDdd.builder().uf("AP").nome("Amapá").regiao(NORTE).codigos(Collections.singletonList(96)).build());

//        Amazonas AM Localizado na Região Norte
        //– Amazonas (92 e 97)
        ESTADOS.put("AM", EstadoDdd.builder().uf("AM").nome("Amazonas").regiao(NORTE).codigos(Arrays.asList(92, 97)).build());

//        Bahia BA Localizado na Região Nordeste
        //– Bahia (71, 73, 74, 75 e 77)
        ESTADOS.put("BA", EstadoDdd.builder().uf("BA").nome("Bahia").regiao(NORDESTE).codigos(Arrays.asList(71, 73, 74, 75, 77)).build());

//        Ceará CE Localizado na Região Nordeste
        //– Ceará (85 e 88)
        ESTADOS.put("CE", EstadoDdd.builder().uf("CE").nome("Ceará").regiao(NORDESTE).codigos(Arrays.asList(85, 88)).build());

//        Distrito Federal DF Localizado na Região
        //– Distrito Federal (61)
        ESTADOS.put("DF", EstadoDdd.builder().uf("DF").nome("Distrito Federal").regiao(CENTRO_OESTE).codigos(Collections.singletonList(61)).build());

        //Espírito Santo ES Localizado na Região Sudeste
        //– Espírito Santo (27 e 28)
        ESTADOS.put("ES", EstadoDdd.builder().uf("ES").nome("Espírito Santo").regiao(SUDESTE).codigos(Arrays.asList(27, 28)).build());

//        Goiás GO Localizado na Região Centro -Oeste
//        – Goiás (62 e 64)
        ESTADOS.put("GO", EstadoDdd.builder().uf("Goiás").nome("Espírito Santo").regiao(CENTRO_OESTE).codigos(Arrays.asList(62, 64)).build());

//        Maranhão MA Localizado na Região Nordeste
        //– Maranhão (98 e 99)
        ESTADOS.put("MA", EstadoDdd.builder().uf("MA").nome("Maranhão").regiao(NORDESTE).codigos(Arrays.asList(98, 99)).build());

//        Mato Grosso MT Localizado na Região Centro - Oeste
        //– Mato Grosso (65 e 66)
        ESTADOS.put("MT", EstadoDdd.builder().uf("MT").nome("Mato Grosso").regiao(CENTRO_OESTE).codigos(Arrays.asList(65, 66)).build());

//        Mato Grosso do Sul MS Localizado na Região Centro -Oeste
        //– Mato Grosso do Sul (67)
        ESTADOS.put("MS", EstadoDdd.builder().uf("MS").nome("Mato Grosso do Sul").regiao(CENTRO_OESTE).codigos(Collections.singletonList(67)).build());

//        Minas Gerais MG Localizado na Região Sudeste
        //– Minas Gerais (31, 32, 33, 34, 35, 37 e 38)
        ESTADOS.put("MG", EstadoDdd.builder().uf("MG").nome("Minas Gerais").regiao(SUDESTE).codigos(Arrays.asList(31, 32, 33, 34, 35, 37, 38)).build());

//        Pará PA Localizado na Região Norte
        //– Pará (91, 93 e 94)
        ESTADOS.put("PA", EstadoDdd.builder().uf("PA").nome("Pará").regiao(NORTE).codigos(Arrays.asList(91, 93, 94)).build());

//        Paraíba PB Localizado na Região Nordeste
        //– Paraíba (83)
        ESTADOS.put("PB", EstadoDdd.builder().uf("PB").nome("Paraíba").regiao(NORDESTE).codigos(Collections.singletonList(83)).build());

//        Paraná PR Localizado na Região Sul
        //– Paraná (41, 42, 43, 44, 45 e 46)
        ESTADOS.put("PR", EstadoDdd.builder().uf("PR").nome("Paraná").regiao("SUL").codigos(Arrays.asList(41, 42, 43, 44, 45, 46)).build());

//        Pernambuco PE Localizado na Região Nordeste
        //– Pernambuco (81 e 87)
        ESTADOS.put("PE", EstadoDdd.builder().uf("PE").nome("Pernambuco").regiao(NORDESTE).codigos(Arrays.asList(81, 87)).build());

//        Piauí PI Localizado na Região Nordeste
        //– Piauí (86 e 89)
        ESTADOS.put("PI", EstadoDdd.builder().uf("PI").nome("Piauí").regiao(NORDESTE).codigos(Arrays.asList(86, 89)).build());

//        Rio de Janeiro RJ Localizado na Região Sudeste
        //– Rio de Janeiro (21, 22 e 24)
        ESTADOS.put("RJ", EstadoDdd.builder().uf("RJ").nome("Rio de Janeiro").regiao(SUDESTE).codigos(Arrays.asList(21,22,24)).build());

//        Rio Grande do Norte RN Localizado na Região Nordeste
        //– Rio Grande do Norte (84)
        ESTADOS.put("RN", EstadoDdd.builder().uf("RN").nome("Rio Grande do Norte").regiao(NORDESTE).codigos(Collections.singletonList(84)).build());

//        Rio Grande do Sul RS Localizado na Região Sul
        //– Rio Grande do Sul (51, 53, 54 e 55)
        ESTADOS.put("RS", EstadoDdd.builder().uf("RS").nome("Rio Grande do Sul").regiao(SUL).codigos(Arrays.asList(51, 53, 54, 55)).build());

//        Rondônia RO Localizado na Região Norte
        //– Rondônia (69)
        ESTADOS.put("RO", EstadoDdd.builder().uf("RO").nome("Rondônia").regiao(NORTE).codigos(Collections.singletonList(69)).build());

//        Roraima RR Localizado na Região Norte
        //– Roraima (95)
        ESTADOS.put("RR", EstadoDdd.builder().uf("RR").nome("Roraima").regiao(NORTE).codigos(Collections.singletonList(95)).build());

//        Santa Catarina SC Localizado na Região Sul
        //– Santa Catarina (47, 48 e 49)
        ESTADOS.put("SC", EstadoDdd.builder().uf("SC").nome("Santa Catarina").regiao(SUL).codigos(Arrays.asList(47, 48, 49)).build());

//        São Paulo SP Localizado na Região Sudeste
        //– São Paulo (11, 12, 13, 14, 15, 16, 17, 18 e 19)
        ESTADOS.put("SP", EstadoDdd.builder().uf("SP").nome("São Paulo").regiao(SUDESTE).codigos(Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19)).build());

//        Sergipe SE Localizado na Região Nordeste
        //– Sergipe (79)
        ESTADOS.put("SE", EstadoDdd.builder().uf("SE").nome("Sergipe").regiao(NORDESTE).codigos(Collections.singletonList(79)).build());

//        Tocantins TO Localizado na Região Norte
        //– Tocantins (63)
        ESTADOS.put("TO", EstadoDdd.builder().uf("TO").nome("Tocantins").regiao(NORTE).codigos(Collections.singletonList(63)).build());
    }

    public Optional<EstadoDdd> findEstadoByCodigoDdd(int ddd) {
        return ESTADOS.values().stream().filter(e -> e.getCodigos().contains(ddd)).findFirst();
    }

    public List<Integer> findDddByEstado(String uf) {
        return ESTADOS.get(uf.toUpperCase()).getCodigos();
    }

    public Collection<EstadoDdd> listaTodosEstados() {
        return ESTADOS.values();
    }

    public List<EstadoDdd> listaEstadosPorRegiao(String regiao) {
        return ESTADOS.values().stream().filter(e -> e.getRegiao().contains(regiao.toUpperCase())).collect(Collectors.toList());
    }
}
