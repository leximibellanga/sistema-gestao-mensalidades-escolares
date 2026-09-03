package App.dao;

import App.model.Turma;
import App.util.ConexaoBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {
    public void inserir(Turma turma) {
        String sql = "INSERT INTO turma (nome, valor_mensalidade) VALUES (?, ?)";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, turma.getNome());
            stmt.setDouble(2, turma.getValorMensalidade());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    turma.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir turma: " + e.getMessage(), e);
        }
    }

    public void atualizar(Turma turma) {
        String sql = "UPDATE turma SET nome = ?, valor_mensalidade = ? WHERE id = ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setString(1, turma.getNome());
            stmt.setDouble(2, turma.getValorMensalidade());
            stmt.setInt(3, turma.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar turma: " + e.getMessage(), e);
        }
    }

    public void remover(int id) {
        String sql = "DELETE FROM turma WHERE id = ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover turma: " + e.getMessage(), e);
        }
    }

    public Turma buscarPorId(int id) {
        String sql = "SELECT * FROM turma WHERE id = ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar turma: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Turma> listarTodas() {
        List<Turma> lista = new ArrayList<>();
        String sql = "SELECT * FROM turma ORDER BY nome";
        try (Statement stmt = ConexaoBD.getConexao().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar turmas: " + e.getMessage(), e);
        }
        return lista;
    }

    private Turma mapear(ResultSet rs) throws SQLException {
        return new Turma(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getDouble("valor_mensalidade")
        );
    }
}
