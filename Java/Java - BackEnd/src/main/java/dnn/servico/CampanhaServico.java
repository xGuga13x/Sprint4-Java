package dnn.servico;

import dnn.dao.Conexao;
import dnn.modelo.Campanha;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// CRUD de campanhas
public class CampanhaServico {

    private static final String SQL_BASE =
            "SELECT id_campanha,nome,descricao," +
                    "TO_CHAR(data_inicio,'YYYY-MM-DD') AS data_inicio," +
                    "TO_CHAR(data_fim,'YYYY-MM-DD') AS data_fim," +
                    "meta_valor,total_arrecadado,ativo FROM tdb_Campanha ";

    public List<Campanha> listar() throws SQLException {
        List<Campanha> lista = new ArrayList<>();
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "ORDER BY data_inicio DESC");
             ResultSet r = s.executeQuery()) {
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    public Campanha buscarPorId(int id) throws SQLException {
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "WHERE id_campanha=?")) {
            s.setInt(1, id);
            ResultSet r = s.executeQuery();
            return r.next() ? mapear(r) : null;
        }
    }

    public void salvar(Campanha c) throws SQLException {
        // ID e INSERT na mesma conexão — evita race condition
        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement si = conn.prepareStatement(
                        "SELECT NVL(MAX(id_campanha),0)+1 FROM tdb_Campanha");
                     ResultSet ri = si.executeQuery()) {
                    id = ri.next() ? ri.getInt(1) : 1;
                }
                try (PreparedStatement s = conn.prepareStatement(
                        "INSERT INTO tdb_Campanha(id_campanha,nome,descricao,data_inicio,data_fim," +
                                "meta_valor,total_arrecadado,ativo) VALUES(?,?,?,TO_DATE(?,'YYYY-MM-DD')," +
                                "TO_DATE(?,'YYYY-MM-DD'),?,0,'S')")) {
                    s.setInt(1, id);
                    s.setString(2, c.getNome());
                    s.setString(3, c.getDescricao());
                    s.setString(4, c.getDataInicio());
                    s.setString(5, c.getDataFim());
                    s.setDouble(6, c.getMetaValor());
                    s.executeUpdate();
                }
                conn.commit();
                c.setIdCampanha(id);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean atualizar(int id, Campanha c) throws SQLException {
        // Inclui data_inicio no UPDATE
        try (Connection conn = Conexao.conectar();
             PreparedStatement s = conn.prepareStatement(
                     "UPDATE tdb_Campanha SET nome=?,descricao=?," +
                             "data_inicio=NVL(TO_DATE(?,'YYYY-MM-DD'),data_inicio)," +
                             "data_fim=NVL(TO_DATE(?,'YYYY-MM-DD'),data_fim)," +
                             "meta_valor=?,ativo=? WHERE id_campanha=?")) {
            s.setString(1, c.getNome());
            s.setString(2, c.getDescricao());
            s.setString(3, c.getDataInicio());
            s.setString(4, c.getDataFim());
            s.setDouble(5, c.getMetaValor());
            s.setString(6, c.getAtivo());
            s.setInt(7, id);
            return s.executeUpdate() > 0;
        }
    }

    public boolean excluir(int id) throws SQLException {
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement("DELETE FROM tdb_Campanha WHERE id_campanha=?")) {
            s.setInt(1, id);
            return s.executeUpdate() > 0;
        }
    }

    private Campanha mapear(ResultSet r) throws SQLException {
        Campanha c = new Campanha();
        c.setIdCampanha(r.getInt("id_campanha"));
        c.setNome(r.getString("nome"));
        c.setDescricao(r.getString("descricao"));
        c.setDataInicio(r.getString("data_inicio"));
        c.setDataFim(r.getString("data_fim"));
        c.setMetaValor(r.getDouble("meta_valor"));
        c.setTotalArrecadado(r.getDouble("total_arrecadado"));
        c.setAtivo(r.getString("ativo"));
        return c;
    }
}
