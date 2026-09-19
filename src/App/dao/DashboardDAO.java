package App.dao;

import App.util.ConexaoBD;
import java.sql.*;

public class DashboardDAO {
    
    public int countAlunos() {
        int totalAlunos = -1;
        
        String sql = "SELECT count(*) AS total FROM aluno";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalAlunos = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar nr total de alunos " + e.getMessage(), e);
        }
        return totalAlunos;
    }
    
    public int countTurmas() {
        int totalTurmas = -1;
        
        String sql = "SELECT count(*) AS total FROM turma";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalTurmas = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar nr total de turmas: " + e.getMessage(), e);
        }
        return totalTurmas;
    }
    
    public int countMensalidades() {
        int totalAMensalidades = -1;
        
        String sql = "SELECT count(*) AS total FROM mensalidade";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalAMensalidades = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar nr total de mensalidades: " + e.getMessage(), e);
        }
        return totalMensalidades;
    }
    
    public int countPagamentos() {
        int totalPagamentos = -1;
        
        String sql = "SELECT count(*) AS total FROM pagamento";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalPagamentos = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar nr total de pagamentos: " + e.getMessage(), e);
        }
        return totalPagamentos;
    }
}
