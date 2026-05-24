package dnn.boo;

import dnn.modelo.Paciente;
import dnn.servico.PacienteServico;

import java.sql.SQLException;
import java.util.List;

// Regras de negócio para Pacientes
public class PacienteBoo {

    private final PacienteServico srv = new PacienteServico();

    public List<Paciente> listar() {
        try {
            return srv.listar();
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao listar pacientes: " + e.getMessage(), 500);
        }
    }

    public Paciente buscar(int id) {
        if (id <= 0) throw new ApiExcecao("ID invalido.", 400);
        try {
            Paciente p = srv.buscar(id);
            if (p == null) throw new ApiExcecao("Paciente nao encontrado.", 404);
            return p;
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao buscar paciente: " + e.getMessage(), 500);
        }
    }

    public Paciente cadastrar(Paciente p) {
        if (!p.nomeValido()) throw new ApiExcecao("Nome invalido (minimo 3 caracteres).", 400);
        if (!p.cpfValido()) throw new ApiExcecao("CPF invalido (informe 11 digitos).", 400);
        if (!p.programaValido()) throw new ApiExcecao("Programa invalido. Use DENTISTAS_DO_BEM ou APOLONICAS_DO_BEM.", 400);
        if (!p.rendaValida()) throw new ApiExcecao("Renda nao pode ser negativa.", 400);
        if (!p.distanciaValida()) throw new ApiExcecao("Distancia nao pode ser negativa.", 400);
        try {
            return srv.cadastrar(p);
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao cadastrar paciente: " + e.getMessage(), 500);
        }
    }

    public void atualizar(int id, Paciente p) {
        if (id <= 0) throw new ApiExcecao("ID invalido.", 400);
        if (!p.nomeValido()) throw new ApiExcecao("Nome invalido.", 400);
        if (!p.programaValido()) throw new ApiExcecao("Programa invalido.", 400);
        if (!p.rendaValida()) throw new ApiExcecao("Renda nao pode ser negativa.", 400);
        if (!p.distanciaValida()) throw new ApiExcecao("Distancia nao pode ser negativa.", 400);
        try {
            srv.atualizar(id, p);
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao atualizar paciente: " + e.getMessage(), 500);
        }
    }

    public void excluir(int id) {
        if (id <= 0) throw new ApiExcecao("ID invalido.", 400);
        try {
            srv.excluir(id);
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao excluir paciente: " + e.getMessage(), 500);
        }
    }
}
