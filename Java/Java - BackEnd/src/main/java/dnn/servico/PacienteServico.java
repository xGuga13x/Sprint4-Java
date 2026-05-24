package dnn.servico;

import dnn.boo.ApiExcecao;
import dnn.dao.Conexao;
import dnn.modelo.Paciente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// CRUD de pacientes com validações de banco
public class PacienteServico {

    private static final String SQL_BASE =
            "SELECT pa.id_paciente, pe.cpf, pe.nome, pe.telefone, pe.email, " +
                    "TO_CHAR(pe.data_nasc,'YYYY-MM-DD') AS data_nasc, " +
                    "pe.cep, pe.logradouro, pe.numero, pe.bairro, pe.cidade, pe.uf, " +
                    "pa.programa, pa.renda_familiar, pa.distancia_km, " +
                    "pa.turno_preferencial, pa.convenio, pa.observacoes, pe.ativo " +
                    "FROM tdb_Paciente pa JOIN tdb_Pessoa pe ON pe.cpf = pa.cpf ";

    public List<Paciente> listar() throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "ORDER BY pe.nome");
             ResultSet r = s.executeQuery()) {
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    public Paciente buscar(int id) throws SQLException {
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(SQL_BASE + "WHERE pa.id_paciente = ?")) {
            s.setInt(1, id);
            ResultSet r = s.executeQuery();
            return r.next() ? mapear(r) : null;
        }
    }

    public Paciente cadastrar(Paciente p) throws SQLException {
        if (!p.nomeValido()) throw new ApiExcecao("Nome obrigatorio (minimo 3 caracteres).", 400);
        if (!p.cpfValido()) throw new ApiExcecao("CPF invalido.", 400);
        if (!p.programaValido())
            throw new ApiExcecao("Programa invalido. Use DENTISTAS_DO_BEM ou APOLONICAS_DO_BEM.", 400);
        if (!p.rendaValida()) throw new ApiExcecao("Renda nao pode ser negativa.", 400);
        if (!p.distanciaValida()) throw new ApiExcecao("Distancia nao pode ser negativa.", 400);

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            try {
                int novoId;
                try (PreparedStatement si = conn.prepareStatement(
                        "SELECT NVL(MAX(id_paciente),0)+1 FROM tdb_Paciente");
                     ResultSet ri = si.executeQuery()) {
                    novoId = ri.next() ? ri.getInt(1) : 1;
                }

                PreparedStatement sp = conn.prepareStatement(
                        "INSERT INTO tdb_Pessoa(cpf,nome,data_nasc,telefone,email," +
                                "cep,logradouro,numero,bairro,cidade,uf,ativo,dt_cadastro) " +
                                "VALUES(?,?,TO_DATE(?,'YYYY-MM-DD'),?,?,?,?,?,?,?,?,'S',SYSDATE)");
                sp.setString(1, p.getCpf());
                sp.setString(2, p.getNome());
                sp.setString(3, p.getDataNasc());
                sp.setString(4, p.getTelefone());
                sp.setString(5, p.getEmail());
                sp.setString(6, p.getCep());
                sp.setString(7, p.getLogradouro());
                sp.setString(8, p.getNumero());
                sp.setString(9, p.getBairro());
                sp.setString(10, p.getCidade());
                sp.setString(11, p.getUf());
                sp.executeUpdate();

                PreparedStatement spa = conn.prepareStatement(
                        "INSERT INTO tdb_Paciente(id_paciente,cpf,programa," +
                                "renda_familiar,distancia_km,turno_preferencial,convenio,observacoes) " +
                                "VALUES(?,?,?,?,?,?,?,?)");
                spa.setInt(1, novoId);
                spa.setString(2, p.getCpf());
                spa.setString(3, p.getPrograma());
                spa.setDouble(4, p.getRendaFamiliar());
                spa.setDouble(5, p.getDistanciaKm());
                spa.setString(6, p.getTurnoPreferencial());
                spa.setString(7, p.getConvenio());
                spa.setString(8, p.getObservacoes());
                spa.executeUpdate();

                conn.commit();
                p.setIdPaciente(novoId);
                return p;
            } catch (SQLException e) {
                conn.rollback();
                if (e.getMessage().contains("PK_TDB_PESSOA") || e.getMessage().contains("UK_TDB_PACIENTE_CPF"))
                    throw new ApiExcecao("CPF ja cadastrado.", 409);
                throw e;
            }
        }
    }

    public void atualizar(int id, Paciente p) throws SQLException {
        if (!p.nomeValido()) throw new ApiExcecao("Nome obrigatorio.", 400);
        if (!p.programaValido()) throw new ApiExcecao("Programa invalido.", 400);
        if (!p.rendaValida()) throw new ApiExcecao("Renda nao pode ser negativa.", 400);
        if (!p.distanciaValida()) throw new ApiExcecao("Distancia nao pode ser negativa.", 400);

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement sp = conn.prepareStatement(
                        "UPDATE tdb_Pessoa SET nome=?,telefone=?,email=?,cep=?," +
                                "logradouro=?,numero=?,bairro=?,cidade=?,uf=? " +
                                "WHERE cpf=(SELECT cpf FROM tdb_Paciente WHERE id_paciente=?)");
                sp.setString(1, p.getNome());
                sp.setString(2, p.getTelefone());
                sp.setString(3, p.getEmail());
                sp.setString(4, p.getCep());
                sp.setString(5, p.getLogradouro());
                sp.setString(6, p.getNumero());
                sp.setString(7, p.getBairro());
                sp.setString(8, p.getCidade());
                sp.setString(9, p.getUf());
                sp.setInt(10, id);
                sp.executeUpdate();

                PreparedStatement spa = conn.prepareStatement(
                        "UPDATE tdb_Paciente SET programa=?,renda_familiar=?," +
                                "distancia_km=?,turno_preferencial=?,convenio=?,observacoes=? " +
                                "WHERE id_paciente=?");
                spa.setString(1, p.getPrograma());
                spa.setDouble(2, p.getRendaFamiliar());
                spa.setDouble(3, p.getDistanciaKm());
                spa.setString(4, p.getTurnoPreferencial());
                spa.setString(5, p.getConvenio());
                spa.setString(6, p.getObservacoes());
                spa.setInt(7, id);
                if (spa.executeUpdate() == 0) throw new ApiExcecao("Paciente nao encontrado.", 404);
                conn.commit();
            } catch (ApiExcecao e) {
                conn.rollback();
                throw e;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(
                     "DELETE FROM tdb_Pessoa WHERE cpf=(SELECT cpf FROM tdb_Paciente WHERE id_paciente=?)")) {
            s.setInt(1, id);
            if (s.executeUpdate() == 0) throw new ApiExcecao("Paciente nao encontrado.", 404);
        }
    }

    private Paciente mapear(ResultSet r) throws SQLException {
        Paciente p = new Paciente();
        p.setIdPaciente(r.getInt("id_paciente"));
        p.setCpf(r.getString("cpf"));
        p.setNome(r.getString("nome"));
        p.setTelefone(r.getString("telefone"));
        p.setEmail(r.getString("email"));
        p.setDataNasc(r.getString("data_nasc"));
        p.setCep(r.getString("cep"));
        p.setLogradouro(r.getString("logradouro"));
        p.setNumero(r.getString("numero"));
        p.setBairro(r.getString("bairro"));
        p.setCidade(r.getString("cidade"));
        p.setUf(r.getString("uf"));
        p.setPrograma(r.getString("programa"));
        p.setRendaFamiliar(r.getDouble("renda_familiar"));
        p.setDistanciaKm(r.getDouble("distancia_km"));
        p.setTurnoPreferencial(r.getString("turno_preferencial"));
        p.setConvenio(r.getString("convenio"));
        p.setObservacoes(r.getString("observacoes"));
        p.setAtivo(r.getString("ativo"));
        return p;
    }
}
