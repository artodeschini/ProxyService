package org.todeschini.resource;

import org.todeschini.correios.ProxyWebServiceCorreios;
import org.todeschini.dto.EnderecoDTO;
import org.todeschini.dto.MuniciopioIbge;
import org.todeschini.ibge.IbgeCrawler;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

/**
 *
 * @author Artur
 */
@Path("/proxy")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProxyResource {

    @Inject
    ProxyWebServiceCorreios correios;

    @Inject
    IbgeCrawler ibge;

    @GET
    @Path("/{cep}")
    public EnderecoDTO find(@PathParam("cep") String cep) {
        return correios.call(cep.replaceAll("[^\\d.]", ""));
    }

    @GET
    @Path("/{uf}/{municipio}")
    public Optional<MuniciopioIbge> find(@PathParam("uf") String uf, @PathParam("municipio") String municipio) {
        return ibge.findMunicipioIbge(uf, municipio);
    }
}
