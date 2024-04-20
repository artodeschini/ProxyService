package org.todeschini.resource;

import org.todeschini.correios.ProxyWebServiceCorreios;
import org.todeschini.ddd.EstadosRegioesDDD;
import org.todeschini.dto.ConsultaGoogle;
import org.todeschini.fenaban.Fenaban;
import org.todeschini.googlemap.GoogleMaps;
import org.todeschini.ibge.IbgeCrawler;
import org.todeschini.moedas.ConversorMoedas;
import org.todeschini.receitaws.Receita;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/proxy")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProxyResource {

    @Inject
    private ProxyWebServiceCorreios correios;

    @Inject
    private IbgeCrawler ibge;

    @Inject
    private Fenaban fenaban;

    @Inject
    private Receita receita;

    @Inject
    private EstadosRegioesDDD estadosRegioes;

    @Inject
    private GoogleMaps google;

    @Inject
    private ConversorMoedas conversorMoedas;

    @GET
    @Path("correios/{cep}")
    public Response find(@PathParam("cep") String cep) {
        return Response.ok(correios.call(cep)).build();
    }

    @GET
    @Path("correios/busca/{logradouro}/{cidade}")
    public Response findPorLogradouro(@PathParam("logradouro") String logradouro, @PathParam("cidade") String cidade) {
        return Response.ok(correios.buscaCepPorLogradouro(logradouro, cidade)).build();
    }

    @GET
    @Path("ibge/{uf}/{municipio}")
    public Response find(@PathParam("uf") String uf, @PathParam("municipio") String municipio) {
        return Response.ok(ibge.findMunicipioIbge(uf, municipio)).build();
    }

    @GET
    @Path("ibge/{uf}")
    public Response findMunicipios(@PathParam("uf") String uf) {
        return Response.ok(ibge.todosMunicipoiosPorEstado(uf)).build();
    }

    @GET
    @Path("ibge/estado/{uf}")
    public Response findEstado(@PathParam("uf") String uf) {
        return Response.ok(ibge.getEstadoPorUf(uf)).build();
    }

    @GET
    @Path("/feriados/{ano}")
    public Response findFeriadosBancarios(@PathParam("ano") Integer ano) {
        return Response.ok(fenaban.feriadosBancariosByAno(ano)).build();
    }

    @GET
    @Path("/receita/{cnpj}")
    public Response findDadosReceitaByCnpj(@PathParam("cnpj") String cnpj) {
        return Response.ok(receita.call(cnpj)).build();
    }

    @GET
    @Path("/regiao/uf/ddd/{ddd}")
    public Response findEstadoByDdd(@PathParam("ddd") Integer ddd) {
        return Response.ok(estadosRegioes.findEstadoByCodigoDdd(ddd)).build();
    }

    @GET
    @Path("/regiao/{regiao}")
    public Response listaEstadosPorRegiao(@PathParam("regiao") String regiao) {
        return Response.ok(estadosRegioes.listaEstadosPorRegiao(regiao)).build();
    }

    @GET
    @Path("/regiao/uf/{uf}")
    public Response findDddByEstado(@PathParam("uf") String uf) {
        return Response.ok(estadosRegioes.findDddByEstado(uf)).build();
    }

    @GET
    @Path("/regiao/estados")
    public Response listaTodosEstados() {
        return Response.ok(estadosRegioes.listaTodosEstados()).build();
    }

    @POST
    @Path("/disatancia")
    public Response distanciaEntre(ConsultaGoogle consulta) {
        return Response.ok(google.calcularDistancia(
                consulta.getOrigem(),
                consulta.getNumeroOrigem(),
                consulta.getDestino(),
                consulta.getNumeroDestino())).build();
    }

    @GET
    @Path("/moedas/{de}/{para}")
    public Response cotacaoMoeda(@PathParam("de") String de, @PathParam("para") String para) {
        return Response.ok(conversorMoedas.converter(de, para)).build();
    }

    @GET
    @Path("/moedas/dolar")
    public Response cotacaoDolar() {
        return Response.ok(conversorMoedas.cotacaoDolarReal()).build();
    }

    @GET
    @Path("/moedas/euro")
    public Response cotacaoEuro() {
        return Response.ok(conversorMoedas.cotacaoEuroReal()).build();
    }
}
