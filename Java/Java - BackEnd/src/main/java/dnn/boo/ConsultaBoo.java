package dnn.boo;

import dnn.modelo.Consulta;
import dnn.servico.ConsultaServico;

import java.sql.SQLException;
import java.util.List;

// Regras de negócio para Consultas
public class ConsultaBoo {

    private final ConsultaServico srv = new ConsultaServico();

    public List<Consulta> listar() {
        try {
            return srv.listar();
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao listar consultas: " + e.getMessage(), 500);
        }
    }

    public List<Consulta> listarPorPaciente(int idPaciente) {
        if (idPaciente <= 0) throw new ApiExcecao("ID do paciente invalido.", 400);
        try {
            return srv.listarPorPaciente(idPaciente);
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao listar consultas: " + e.getMessage(), 500);
        }
    }

    public Consulta buscar(int id) {
        if (id <= 0) throw new ApiExcecao("ID invalido.", 400);
        try {
            Consulta c = srv.buscar(id);
            if (c == null) throw new ApiExcecao("Consulta nao encontrada.", 404);
            return c;
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao buscar consulta: " + e.getMessage(), 500);
        }
    }

    public Consulta criar(Consulta c) {
        if (!c.dataValida()) throw new ApiExcecao("Data da consulta obrigatoria.", 400);
        if (c.getIdPaciente() <= 0) throw new ApiExcecao("ID do paciente invalido.", 400);
        if (c.getCroDentista() == null || c.getCroDentista().isBlank())
            throw new ApiExcecao("CRO do dentista obrigatorio.", 400);
        if (c.getCpfDentista() == null || c.getCpfDentista().isBlank())
            throw new ApiExcecao("CPF do dentista obrigatorio.", 400);
        try {
            return srv.criar(c);
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao criar consulta: " + e.getMessage(), 500);
        }
    }

    public void atualizar(int id, Consulta c) {
        if (id <= 0) throw new ApiExcecao("ID invalido.", 400);
        if (c.getStatus() != null && !c.statusValido())
            throw new ApiExcecao("Status invalido. Use AGENDADA, REALIZADA, CANCELADA ou FALTA.", 400);
        try {
            srv.atualizar(id, c);
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao atualizar consulta: " + e.getMessage(), 500);
        }
    }

    public void excluir(int id) {
        if (id <= 0) throw new ApiExcecao("ID invalido.", 400);
        try {
            srv.excluir(id);
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao excluir consulta: " + e.getMessage(), 500);
        }
    }
}
