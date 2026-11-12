package App.dao;

import App.model.Aluno;
import App.model.Turma;
import App.util.ConexaoBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlunoDAO {

    private final TurmaDAO turmaDAO = new TurmaDAO();

    public void inserir(Aluno aluno) {
        String sql = "INSERT INTO aluno (nome, numero_estudante, encarregado, contacto, turma_id, data_matricula) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getNumeroEstudante());
            stmt.setString(3, aluno.getEncarregado());
            stmt.setString(4, aluno.getContacto());
            stmt.setInt(5, aluno.getTurma().getId());
            stmt.setDate(6, Date.valueOf(aluno.getDataMatricula()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    aluno.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir aluno: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza apenas os dados pessoais: nome, encarregado e contacto.
     */
    public void atualizar(Aluno aluno) {
        String sql = "UPDATE aluno SET nome = ?, encarregado = ?, contacto = ? WHERE id = ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getEncarregado());
            stmt.setString(3, aluno.getContacto());
            stmt.setInt(4, aluno.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar aluno: " + e.getMessage(), e);
        }
    }

    public void remover(String nrEstudante) {
        String sql = "DELETE FROM aluno WHERE numero_estudante = ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setString(1, nrEstudante);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover aluno: " + e.getMessage(), e);
        }
    }

    public Aluno buscarPorId(int id) {
        String sql = "SELECT * FROM aluno WHERE id = ?";
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

    public Aluno buscarPorNrEstudante(String nrEst) {
        String sql = "SELECT * FROM aluno WHERE numero_estudante = ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setString(1, nrEst);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Aluno> listarTodos() {
        List<Aluno> lista = new ArrayList<>();
        String sql = "SELECT * FROM aluno ORDER BY turma_id DESC, numero_estudante, nome";
        try (Statement stmt = ConexaoBD.getConexao().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar alunos: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Aluno> listarPorTurma(int turmaId) {
        List<Aluno> lista = new ArrayList<>();
        String sql = "SELECT * FROM aluno WHERE turma_id = ? ORDER BY nome";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, turmaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar alunos por turma: " + e.getMessage(), e);
        }
        return lista;
    }

    private Aluno mapear(ResultSet rs) throws SQLException {
        Turma turma = turmaDAO.buscarPorId(rs.getInt("turma_id"));

        Aluno aluno = new Aluno();
        aluno.setId(rs.getInt("id"));
        aluno.setNome(rs.getString("nome"));
        aluno.setNumeroEstudante(rs.getString("numero_estudante"));
        aluno.setEncarregado(rs.getString("encarregado"));
        aluno.setContacto(rs.getString("contacto"));
        aluno.setTurma(turma);
        aluno.setDataMatricula(rs.getDate("data_matricula").toLocalDate());

        return aluno;
    }

    /**
     * Maior número de sequência já usado por um curso num ano. Ex: com
     * GES.9.2026 e GES.10.2026 gravados, ultimoSequencial("GES", 2026) devolve
     * 10. Devolve 0 se ainda não houver nenhum.
     */
    public int ultimoSequencial(String prefixo, int ano) {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(numero_estudante, '.', 2), '.', -1) AS UNSIGNED)), 0) "
                + "FROM aluno WHERE numero_estudante LIKE ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setString(1, prefixo + ".%." + ano);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular o próximo número de estudante: " + e.getMessage(), e);
        }
    }
}
