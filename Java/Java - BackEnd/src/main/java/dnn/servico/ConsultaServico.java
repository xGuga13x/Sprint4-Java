package dnn.servico;

import dnn.boo.ApiExcecao;
import dnn.dao.Conexao;
import dnn.modelo.Consulta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// CRUD de consultas com validações de banco
public class ConsultaServico {

    private static final String SQL_BASE =
            "SELECT c.id_consulta, c.id_paciente, c.cpf_paciente, c.cro_dentista, c.cpf_dentista, " +
                    "TO_CHAR(c.data_consulta,'YYYY-MM-DD') AS data_consulta, " +
                    "c.horario, c.turno, c.status, c.tipo, c.distancia_km, c.observacoes, " +
                    "pac.nome AS nome_paciente, den.nome AS nome_dentista " +
                    "FROM tdb_Consulta c " +
                    "JOIN tdb_Paciente pa ON pa.id_paciente = c.id_paciente " +
                    "JOIN tdb_Pessoa pac ON pac.cpf = pa.cpf " +
                    "JOIN tdb_Dentista d ON d.cro = c.cro_dentista AND d.cpf = c.cpf_dentista " +
                    "JOIN tdb_Pessoa den ON den.cpf = d.cpf ";

    public List<Consulta> listar() throws SQLException {
        List<Consulta> lista = new ArrayList<>();
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "ORDER BY c.data_consulta DESC");
             ResultSet r = s.executeQuery()) {
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    public List<Consulta> listarPorPaciente(int idPaciente) throws SQLException {
        List<Consulta> lista = new ArrayList<>();
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(
                     SQL_BASE + "WHERE c.id_paciente = ? ORDER BY c.data_consulta DESC")) {
            s.setInt(1, idPaciente);
            ResultSet r = s.executeQuery();
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    public Consulta buscar(int id) throws SQLException {
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "WHERE c.id_consulta = ?")) {
            s.setInt(1, id);
            ResultSet r = s.executeQuery();
            return r.next() ? mapear(r) : null;
        }
    }

    public Consulta criar(Consulta con) throws SQLException {
        if (!con.dataValida()) throw new ApiExcecao("Data da consulta obrigatoria.", 400);
        if (con.getIdPaciente() <= 0) throw new ApiExcecao("ID do paciente invalido.", 400);
        if (con.getCroDentista() == null || con.getCroDentista().isBlank())
            throw new ApiExcecao("CRO do dentista obrigatorio.", 400);

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            try {
                // Busca CPF do paciente
                String cpfPaciente;
                try (PreparedStatement si = conn.prepareStatement(
                        "SELECT cpf FROM tdb_Paciente WHERE id_paciente = ?")) {
                    si.setInt(1, con.getIdPaciente());
                    ResultSet ri = si.executeQuery();
                    if (!ri.next()) throw new ApiExcecao("Paciente nao encontrado.", 404);
                    cpfPaciente = ri.getString(1);
                }

                // Verifica conflito de agenda do dentista
                try (PreparedStatement sc = conn.prepareStatement(
                        "SELECT COUNT(*) FROM tdb_Consulta " +
                                "WHERE cro_dentista=? AND cpf_dentista=? " +
                                "AND data_consulta=TO_DATE(?,'YYYY-MM-DD') " +
                                "AND horario=? AND status NOT IN ('CANCELADA')")) {
                    sc.setString(1, con.getCroDentista());
                    sc.setString(2, con.getCpfDentista());
                    sc.setString(3, con.getDataConsulta());
                    sc.setString(4, con.getHorario());
                    ResultSet rc = sc.executeQuery();
                    if (rc.next() && rc.getInt(1) > 0)
                        throw new ApiExcecao("Dentista ja tem consulta neste horario.", 409);
                }

                int novoId;
                try (PreparedStatement si = conn.prepareStatement(
                        "SELECT NVL(MAX(id_consulta),0)+1 FROM tdb_Consulta");
                     ResultSet ri = si.executeQuery()) {
                    novoId = ri.next() ? ri.getInt(1) : 1;
                }

                PreparedStatement s = conn.prepareStatement(
                        "INSERT INTO tdb_Consulta(id_consulta,id_paciente,cpf_paciente," +
                                "cro_dentista,cpf_dentista,data_consulta,horario,turno,status,tipo,distancia_km,observacoes) " +
                                "VALUES(?,?,?,?,?,TO_DATE(?,'YYYY-MM-DD'),?,?,NVL(?,'AGENDADA'),?,?,?)");
                s.setInt(1, novoId);
                s.setInt(2, con.getIdPaciente());
                s.setString(3, cpfPaciente);
                s.setString(4, con.getCroDentista());
                s.setString(5, con.getCpfDentista());
                s.setString(6, con.getDataConsulta());
                s.setString(7, con.getHorario());
                s.setString(8, con.getTurno());
                s.setString(9, con.getStatus());
                s.setString(10, con.getTipo());
                s.setDouble(11, con.getDistanciaKm());
                s.setString(12, con.getObservacoes());
                s.executeUpdate();

                conn.commit();
                con.setIdConsulta(novoId);
                con.setCpfPaciente(cpfPaciente);
                return con;
            } catch (ApiExcecao e) {
                conn.rollback();
                throw e;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void atualizar(int id, Consulta con) throws SQLException {
        if (!con.statusValido())
            throw new ApiExcecao("Status invalido. Use AGENDADA, REALIZADA, CANCELADA ou FALTA.", 400);
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(
                     "UPDATE tdb_Consulta SET " +
                             "data_consulta=NVL(TO_DATE(?,'YYYY-MM-DD'),data_consulta), " +
                             "horario=NVL(?,horario), turno=NVL(?,turno), " +
                             "status=NVL(?,status), tipo=NVL(?,tipo), " +
                             "distancia_km=NVL(?,distancia_km), observacoes=NVL(?,observacoes) " +
                             "WHERE id_consulta=?")) {
            s.setString(1, con.getDataConsulta());
            s.setString(2, con.getHorario());
            s.setString(3, con.getTurno());
            s.setString(4, con.getStatus());
            s.setString(5, con.getTipo());
            s.setDouble(6, con.getDistanciaKm());
            s.setString(7, con.getObservacoes());
            s.setInt(8, id);
            if (s.executeUpdate() == 0) throw new ApiExcecao("Consulta nao encontrada.", 404);
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(
                     "DELETE FROM tdb_Consulta WHERE id_consulta=?")) {
            s.setInt(1, id);
            if (s.executeUpdate() == 0) throw new ApiExcecao("Consulta nao encontrada.", 404);
        }
    }

    private Consulta mapear(ResultSet r) throws SQLException {
        Consulta c = new Consulta();
        c.setIdConsulta(r.getInt("id_consulta"));
        c.setIdPaciente(r.getInt("id_paciente"));
        c.setCpfPaciente(r.getString("cpf_paciente"));
        c.setCroDentista(r.getString("cro_dentista"));
        c.setCpfDentista(r.getString("cpf_dentista"));
        c.setDataConsulta(r.getString("data_consulta"));
        c.setHorario(r.getString("horario"));
        c.setTurno(r.getString("turno"));
        c.setStatus(r.getString("status"));
        c.setTipo(r.getString("tipo"));
        c.setDistanciaKm(r.getDouble("distancia_km"));
        c.setObservacoes(r.getString("observacoes"));
        c.setNomePaciente(r.getString("nome_paciente"));
        c.setNomeDentista(r.getString("nome_dentista"));
        return c;
    }
}
