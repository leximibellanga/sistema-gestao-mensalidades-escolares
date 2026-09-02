package App.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {
    private static final String URL = "jdbc:mysql://localhost:3306/sg_mensalidades_escolares";
    private static final String USUARIO = "root";
    private static final String SENHA = "";
    
    private static Connection conexao;
    
    private ConexaoBD() {
        // impede instaciacao - classe utilitaria
    }
    
    // pegar a conexao
    public static Connection getConexao() {
        try {
            if (conexao == null || conexao.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
                System.out.println("Conectado");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Erro ao conectar a base de dados: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return conexao;
    }
    
    // fechar a conexao
    public static void fecharConexao() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar conexao: " + e.getMessage());
        }
    }
}

