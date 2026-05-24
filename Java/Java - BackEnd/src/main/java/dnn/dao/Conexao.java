package dnn.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Conexão com Oracle — credenciais via variáveis de ambiente (seguro no Render)
public class Conexao {

    private static final String URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "rm568419");
    private static final String PASS = System.getenv().getOrDefault("DB_PASSWORD", "250204");

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
