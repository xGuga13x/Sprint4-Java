package dnn.boo;

import dnn.modelo.Usuario;
import dnn.servico.UsuarioServico;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

// Regras de negócio para autenticação e usuários
public class UsuarioBoo {

    private final UsuarioServico srv = new UsuarioServico();

    // Gera hash acadêmico: "hash_" + primeira parte do login (antes do ponto)
    // Ex: login "joao.silva" → hash "hash_joao"
    // O banco foi populado com esse padrão nos inserts iniciais
    private String gerarHash(String login) {
        String[] partes = login.split("\\.");
        return "hash_" + partes[0];
    }

    public Map<String, Object> login(String login, String senha) {
        if (login == null || login.isBlank()) throw new ApiExcecao("Login obrigatorio.", 400);
        if (senha == null || senha.isBlank()) throw new ApiExcecao("Senha obrigatoria.", 400);
        try {
            Usuario u = srv.autenticar(login, gerarHash(login));
            if (u == null) throw new ApiExcecao("Login ou senha invalidos.", 401);
            if (!u.isAtivo()) throw new ApiExcecao("Usuario inativo.", 403);
            return Map.of(
                    "idUsuario", u.getIdUsuario(),
                    "nome", u.getNome(),
                    "login", u.getLogin(),
                    "perfil", u.getPerfil(),
                    "mensagem", "Login realizado com sucesso!"
            );
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao autenticar: " + e.getMessage(), 500);
        }
    }

    public List<Usuario> listar() {
        try {
            return srv.listar();
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao listar usuarios: " + e.getMessage(), 500);
        }
    }

    public Usuario cadastrar(Usuario u) {
        if (!u.loginValido()) throw new ApiExcecao("Login invalido (minimo 5 caracteres).", 400);
        if (!u.perfilValido())
            throw new ApiExcecao("Perfil invalido. Use: ADMIN, DENTISTA, VOLUNTARIO ou GESTOR.", 400);
        if (u.getCpf() == null || u.getCpf().isBlank()) throw new ApiExcecao("CPF obrigatorio.", 400);
        u.setSenhaHash(gerarHash(u.getLogin()));
        try {
            srv.salvar(u);
            return u;
        } catch (SQLException e) {
            if (e.getMessage().contains("UK_TDB_USUARIO_LOGIN"))
                throw new ApiExcecao("Login ja existe.", 409);
            throw new ApiExcecao("Erro ao cadastrar usuario: " + e.getMessage(), 500);
        }
    }

    public void desativar(int id) {
        if (id <= 0) throw new ApiExcecao("ID invalido.", 400);
        try {
            if (!srv.desativar(id)) throw new ApiExcecao("Usuario nao encontrado.", 404);
        } catch (ApiExcecao e) {
            throw e;
        } catch (SQLException e) {
            throw new ApiExcecao("Erro ao desativar usuario: " + e.getMessage(), 500);
        }
    }
}
