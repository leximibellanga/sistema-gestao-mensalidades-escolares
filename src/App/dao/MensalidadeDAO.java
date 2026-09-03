package App.dao;

import App.model.Aluno;
import App.model.Mensalidade;
import App.model.StatusMensalidade;
import App.util.ConexaoBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MensalidadeDAO {
    private final AlunoDAO alunoDAO = new AlunoDAO();

    public void inserir(Mensalidade mensalidade) {
        String sql = "INSERT INTO mensalidade (aluno_id, mes_referencia, valor, status) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, mensalidade.getAluno().getId());
            stmt.setDate(2, Date.valueOf(mensalidade.getMesReferencia()));
            stmt.setDouble(3, mensalidade.getValor());
            stmt.setString(4, mensalidade.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    mensalidade.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir mensalidade: " + e.getMessage(), e);
        }
    }

    public void atualizarStatus(int id, StatusMensalidade status) {
        String sql = "UPDATE mensalidade SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status da mensalidade: " + e.getMessage(), e);
        }
    }

    public Mensalidade buscarPorId(int id) {
        String sql = "SELECT * FROM mensalidade WHERE id = ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar mensalidade: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Mensalidade> listarPorAluno(int alunoId) {
        List<Mensalidade> lista = new ArrayList<>();
        String sql = "SELECT * FROM mensalidade WHERE aluno_id = ? ORDER BY mes_referencia";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mensalidades por aluno: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Mensalidade> listarEmAtraso() {
        List<Mensalidade> lista = new ArrayList<>();
        String sql = "SELECT * FROM mensalidade WHERE status = 'ATRASADO' ORDER BY mes_referencia";
        try (Statement stmt = ConexaoBD.getConexao().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mensalidades em atraso: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Mensalidade> listarPendentesComDataVencida() {
        List<Mensalidade> lista = new ArrayList<>();
        String sql = "SELECT * FROM mensalidade WHERE status = 'PENDENTE' AND mes_referencia < CURDATE()";
        try (Statement stmt = ConexaoBD.getConexao().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pendentes vencidas: " + e.getMessage(), e);
        }
        return lista;
    }
    
    public List<Mensalidade> listarPorMesReferencia(LocalDate mesReferencia) {
        List<Mensalidade> lista = new ArrayList<>();
        String sql = "SELECT * FROM mensalidade WHERE mes_referencia = ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(mesReferencia));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mensalidades por mês: " + e.getMessage(), e);
        }
        return lista;
    }

    private Mensalidade mapear(ResultSet rs) throws SQLException {
        Aluno aluno = alunoDAO.buscarPorId(rs.getInt("aluno_id"));

        Mensalidade m = new Mensalidade();
        m.setId(rs.getInt("id"));
        m.setAluno(aluno);
        m.setMesReferencia(rs.getDate("mes_referencia").toLocalDate());
        m.setValor(rs.getDouble("valor"));
        m.setStatus(StatusMensalidade.valueOf(rs.getString("status")));

        return m;
    }
}

