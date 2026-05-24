package dnn.servico;

import dnn.boo.ApiExcecao;
import dnn.dao.Conexao;
import dnn.modelo.Procedimento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// CRUD de procedimentos vinculados a consultas
public class ProcedimentoServico {

    private static final String SQL_BASE =
            "SELECT id_procedimento,id_consulta,cro_dentista,cpf_dentista," +
                    "nome,tipo,duracao_min,custo,orientacao FROM tdb_Procedimento ";

    public List<Procedimento> listarTodos() throws SQLException {
        List<Procedimento> lista = new ArrayList<>();
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "ORDER BY id_procedimento DESC");
             ResultSet r = s.executeQuery()) {
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    public List<Procedimento> listarPorConsulta(int idConsulta) throws SQLException {
        List<Procedimento> lista = new ArrayList<>();
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "WHERE id_consulta=? ORDER BY id_procedimento")) {
            s.setInt(1, idConsulta);
            ResultSet r = s.executeQuery();
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    // ID e INSERT na mesma conexão — evita race condition
    public Procedimento criar(Procedimento p) throws SQLException {
        if (!p.nomeValido()) throw new ApiExcecao("Nome do procedimento obrigatorio.", 400);
        if (!p.custoValido()) throw new ApiExcecao("Custo nao pode ser negativo.", 400);
        if (p.getIdConsulta() <= 0) throw new ApiExcecao("ID da consulta obrigatorio.", 400);

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement si = conn.prepareStatement(
                        "SELECT NVL(MAX(id_procedimento),0)+1 FROM tdb_Procedimento");
                     ResultSet ri = si.executeQuery()) {
                    id = ri.next() ? ri.getInt(1) : 1;
                }
                try (PreparedStatement s = conn.prepareStatement(
                        "INSERT INTO tdb_Procedimento(id_procedimento,id_consulta,cro_dentista," +
                                "cpf_dentista,nome,tipo,duracao_min,custo,orientacao) VALUES(?,?,?,?,?,?,?,?,?)")) {
                    s.setInt(1, id);
                    s.setInt(2, p.getIdConsulta());
                    s.setString(3, p.getCroDentista());
                    s.setString(4, p.getCpfDentista());
                    s.setString(5, p.getNome());
                    s.setString(6, p.getTipo());
                    s.setInt(7, p.getDuracaoMin());
                    s.setDouble(8, p.getCusto());
                    s.setString(9, p.getOrientacao());
                    s.executeUpdate();
                }
                conn.commit();
                p.setIdProcedimento(id);
                return p;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement("DELETE FROM tdb_Procedimento WHERE id_procedimento=?")) {
            s.setInt(1, id);
            if (s.executeUpdate() == 0) throw new ApiExcecao("Procedimento nao encontrado.", 404);
        }
    }

    private Procedimento mapear(ResultSet r) throws SQLException {
        Procedimento p = new Procedimento();
        p.setIdProcedimento(r.getInt("id_procedimento"));
        p.setIdConsulta(r.getInt("id_consulta"));
        p.setCroDentista(r.getString("cro_dentista"));
        p.setCpfDentista(r.getString("cpf_dentista"));
        p.setNome(r.getString("nome"));
        p.setTipo(r.getString("tipo"));
        p.setDuracaoMin(r.getInt("duracao_min"));
        p.setCusto(r.getDouble("custo"));
        p.setOrientacao(r.getString("orientacao"));
        return p;
    }
}
