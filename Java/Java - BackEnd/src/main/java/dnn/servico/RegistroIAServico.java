package dnn.servico;

import dnn.boo.ApiExcecao;
import dnn.dao.Conexao;
import dnn.modelo.RegistroIA;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Leitura e gravação do histórico de predições da IA
public class RegistroIAServico {

    private static final String SQL_BASE =
            "SELECT id_registro,id_consulta,tipo_predicao,entrada_json,prob_resultado," +
                    "classe_prevista,risco,modelo_versao,acerto," +
                    "TO_CHAR(dt_predicao,'YYYY-MM-DD') AS dt_predicao FROM tdb_RegistroIA ";

    public List<RegistroIA> listarTodos() throws SQLException {
        List<RegistroIA> lista = new ArrayList<>();
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "ORDER BY dt_predicao DESC");
             ResultSet r = s.executeQuery()) {
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    public List<RegistroIA> listarPorTipo(String tipo) throws SQLException {
        List<RegistroIA> lista = new ArrayList<>();
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "WHERE tipo_predicao=? ORDER BY dt_predicao DESC")) {
            s.setString(1, tipo);
            ResultSet r = s.executeQuery();
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    public RegistroIA buscar(int id) throws SQLException {
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "WHERE id_registro=?")) {
            s.setInt(1, id);
            ResultSet r = s.executeQuery();
            return r.next() ? mapear(r) : null;
        }
    }

    // ID e INSERT na mesma conexão — evita race condition
    public RegistroIA salvar(RegistroIA reg) throws SQLException {
        if (reg.getEntradaJson() == null || reg.getEntradaJson().isBlank())
            throw new ApiExcecao("Entrada JSON obrigatoria.", 400);

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement si = conn.prepareStatement(
                        "SELECT NVL(MAX(id_registro),0)+1 FROM tdb_RegistroIA");
                     ResultSet ri = si.executeQuery()) {
                    id = ri.next() ? ri.getInt(1) : 1;
                }
                try (PreparedStatement s = conn.prepareStatement(
                        "INSERT INTO tdb_RegistroIA(id_registro,id_consulta,tipo_predicao,entrada_json," +
                                "prob_resultado,classe_prevista,risco,modelo_versao,dt_predicao) " +
                                "VALUES(?,?,?,?,?,?,?,?,SYSDATE)")) {
                    s.setInt(1, id);
                    if (reg.getIdConsulta() != null) s.setInt(2, reg.getIdConsulta());
                    else s.setNull(2, Types.NUMERIC);
                    s.setString(3, reg.getTipoPredicao());
                    s.setString(4, reg.getEntradaJson());
                    s.setDouble(5, reg.getProbResultado());
                    s.setString(6, reg.getClassePrevista());
                    s.setString(7, reg.getRisco());
                    s.setString(8, reg.getModeloVersao() != null ? reg.getModeloVersao() : "v1.0");
                    s.executeUpdate();
                }
                conn.commit();
                reg.setIdRegistro(id);
                return reg;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private RegistroIA mapear(ResultSet r) throws SQLException {
        RegistroIA reg = new RegistroIA();
        reg.setIdRegistro(r.getInt("id_registro"));
        int idC = r.getInt("id_consulta");
        reg.setIdConsulta(r.wasNull() ? null : idC);
        reg.setTipoPredicao(r.getString("tipo_predicao"));
        reg.setEntradaJson(r.getString("entrada_json"));
        reg.setProbResultado(r.getDouble("prob_resultado"));
        reg.setClassePrevista(r.getString("classe_prevista"));
        reg.setRisco(r.getString("risco"));
        reg.setModeloVersao(r.getString("modelo_versao"));
        reg.setAcerto(r.getString("acerto"));
        reg.setDtPredicao(r.getString("dt_predicao"));
        return reg;
    }
}
