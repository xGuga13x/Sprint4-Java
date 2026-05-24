package dnn.servico;

import dnn.dao.Conexao;
import dnn.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acesso ao banco para tdb_Usuario
public class UsuarioServico {

    public Usuario autenticar(String login, String senhaHash) throws SQLException {
        String sql = "SELECT u.id_usuario,u.cpf,u.login,u.senha_hash,u.perfil,u.ativo,pe.nome " +
                "FROM tdb_Usuario u JOIN tdb_Pessoa pe ON pe.cpf=u.cpf " +
                "WHERE u.login=? AND u.senha_hash=? AND u.ativo='S'";
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, login);
            s.setString(2, senhaHash);
            ResultSet r = s.executeQuery();
            return r.next() ? mapear(r) : null;
        }
    }

    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario,u.cpf,u.login,u.senha_hash,u.perfil,u.ativo,pe.nome " +
                "FROM tdb_Usuario u JOIN tdb_Pessoa pe ON pe.cpf=u.cpf ORDER BY pe.nome";
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            while (r.next()) lista.add(mapear(r));
        }
        return lista;
    }

    // ID e INSERT na mesma conexão — evita race condition
    public void salvar(Usuario u) throws SQLException {
        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement si = conn.prepareStatement(
                        "SELECT NVL(MAX(id_usuario),0)+1 FROM tdb_Usuario");
                     ResultSet ri = si.executeQuery()) {
                    id = ri.next() ? ri.getInt(1) : 1;
                }
                try (PreparedStatement s = conn.prepareStatement(
                        "INSERT INTO tdb_Usuario(id_usuario,cpf,login,senha_hash,perfil,ativo,dt_cadastro) " +
                                "VALUES(?,?,?,?,?,'S',SYSDATE)")) {
                    s.setInt(1, id);
                    s.setString(2, u.getCpf());
                    s.setString(3, u.getLogin());
                    s.setString(4, u.getSenhaHash());
                    s.setString(5, u.getPerfil());
                    s.executeUpdate();
                }
                conn.commit();
                u.setIdUsuario(id);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean desativar(int id) throws SQLException {
        try (Connection c = Conexao.conectar();
             PreparedStatement s = c.prepareStatement("UPDATE tdb_Usuario SET ativo='N' WHERE id_usuario=?")) {
            s.setInt(1, id);
            return s.executeUpdate() > 0;
        }
    }

    private Usuario mapear(ResultSet r) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(r.getInt("id_usuario"));
        u.setCpf(r.getString("cpf"));
        u.setLogin(r.getString("login"));
        u.setSenhaHash(r.getString("senha_hash"));
        u.setPerfil(r.getString("perfil"));
        u.setAtivo(r.getString("ativo"));
        u.setNome(r.getString("nome"));
        return u;
    }
}
