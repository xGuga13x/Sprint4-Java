package dnn.api;

import dnn.boo.ApiExcecao;
import dnn.boo.PacienteBoo;
import dnn.modelo.Paciente;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

// CRUD pacientes — /api/pacientes
@Path("/api/pacientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PacienteApi {

    private final PacienteBoo boo = new PacienteBoo();

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

    @POST
    public Response cadastrar(Paciente p) {
        try {
            return Response.status(201).entity(boo.cadastrar(p)).build();
        } catch (ApiExcecao e) {
            return erro(e);
        }
    }

    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") int id, Paciente p) {
        try {
            boo.atualizar(id, p);
            return Response.ok(Map.of("mensagem", "Paciente atualizado!")).build();
        } catch (ApiExcecao e) {
            return erro(e);
        }
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") int id) {
        try {
            boo.excluir(id);
            return Response.ok(Map.of("mensagem", "Paciente removido!")).build();
        } catch (ApiExcecao e) {
            return erro(e);
        }
    }

    private Response erro(ApiExcecao e) {
        return Response.status(e.getStatus()).entity(Map.of("erro", e.getMessage())).build();
    }
}
