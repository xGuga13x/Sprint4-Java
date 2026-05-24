package dnn.api;

import dnn.boo.ApiExcecao;
import dnn.boo.ConsultaBoo;
import dnn.modelo.Consulta;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

// CRUD consultas — /api/consultas
@Path("/api/consultas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsultaApi {

    private final ConsultaBoo boo = new ConsultaBoo();

    @GET
    public Response listar() {
        try {
            return Response.ok(boo.listar()).build();
        } catch (ApiExcecao e) {
            return erro(e);
        }
    }

    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") int id) {
        try {
            return Response.ok(boo.buscar(id)).build();
        } catch (ApiExcecao e) {
            return erro(e);
        }
    }

    @GET
    @Path("/paciente/{idPaciente}")
    public Response listarPorPaciente(@PathParam("idPaciente") int idPaciente) {
        try {
            return Response.ok(boo.listarPorPaciente(idPaciente)).build();
        } catch (ApiExcecao e) {
            return erro(e);
        }
    }

    @POST
    public Response criar(Consulta c) {
        try {
            return Response.status(201).entity(boo.criar(c)).build();
        } catch (ApiExcecao e) {
            return erro(e);
        }
    }

    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") int id, Consulta c) {
        try {
            boo.atualizar(id, c);
            return Response.ok(Map.of("mensagem", "Consulta atualizada!")).build();
        } catch (ApiExcecao e) {
            return erro(e);
        }
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") int id) {
        try {
            boo.excluir(id);
            return Response.ok(Map.of("mensagem", "Consulta removida!")).build();
        } catch (ApiExcecao e) {
            return erro(e);
        }
    }

    private Response erro(ApiExcecao e) {
        return Response.status(e.getStatus()).entity(Map.of("erro", e.getMessage())).build();
    }
}
