package org.todeschini.resource;

import org.todeschini.correios.ProxyWebServiceCorreios;
import org.todeschini.ddd.EstadosRegioesDDD;
import org.todeschini.fenaban.Fenaban;
import org.todeschini.ibge.IbgeCrawler;
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
    ProxyWebServiceCorreios correios;

    @Inject
    IbgeCrawler ibge;

    @Inject
    Fenaban fenaban;

    @Inject
    Receita receita;

    @Inject
    EstadosRegioesDDD estadosRegioes;

    @GET
    @Path("correios/{cep}")
    public Response find(@PathParam("cep") String cep) {
        return Response.ok(correios.call(cep)).build();
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
}
