package App.dao;

import App.model.Aluno;
import App.model.Mensalidade;
import App.model.Pagamento;
import App.model.StatusMensalidade;
import App.util.ConexaoBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas de apoio ao painel inicial: contagens, somas e listas curtas.
 */
public class DashboardDAO {

    public int contarAlunos() {
        return contar("SELECT COUNT(*) FROM aluno");
    }

    public int contarTurmas() {
        return contar("SELECT COUNT(*) FROM turma");
    }

    /**
     * Quantidade e valor total das mensalidades com um dado status. Com mes ==
     * null conta todos os meses. Devolve {quantidade, valor}.
     */
    public double[] totaisPorStatus(StatusMensalidade status, LocalDate mes) {
        String sql = "SELECT COUNT(*), COALESCE(SUM(valor), 0) FROM mensalidade WHERE status = ?"
                + (mes != null ? " AND mes_referencia = ?" : "");
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            if (mes != null) {
                stmt.setDate(2, Date.valueOf(mes));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return new double[]{rs.getInt(1), rs.getDouble(2)};
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar totais de mensalidades: " + e.getMessage(), e);
        }
    }

    /**
     * Total das mensalidades PAGAS de cada mês de referência, a partir de um
     * mês (inclusive).
     */
    public Map<LocalDate, Double> arrecadadoDesde(LocalDate desde) {
        Map<LocalDate, Double> porMes = new LinkedHashMap<>();
        String sql = "SELECT mes_referencia, SUM(valor) FROM mensalidade "
                + "WHERE status = 'PAGO' AND mes_referencia >= ? "
                + "GROUP BY mes_referencia ORDER BY mes_referencia";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(desde));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    porMes.put(rs.getDate(1).toLocalDate(), rs.getDouble(2));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar o arrecadado por mês: " + e.getMessage(), e);
        }
        return porMes;
    }

    /**
     * Os pagamentos mais recentes, com o aluno e o mês de referência (sem
     * turma).
     */
    public List<Pagamento> ultimosPagamentos(int limite) {
        List<Pagamento> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.data_pagamento, p.valor_pago, p.metodo, m.mes_referencia, a.nome, a.numero_estudante "
                + "FROM pagamento p "
                + "JOIN mensalidade m ON m.id = p.mensalidade_id "
                + "JOIN aluno a ON a.id = m.aluno_id "
                + "ORDER BY p.data_pagamento DESC, p.id DESC LIMIT ?";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Aluno aluno = new Aluno();
                    aluno.setNome(rs.getString("nome"));
                    aluno.setNumeroEstudante(rs.getString("numero_estudante"));

                    Mensalidade mensalidade = new Mensalidade();
                    mensalidade.setAluno(aluno);
                    mensalidade.setMesReferencia(rs.getDate("mes_referencia").toLocalDate());

                    Pagamento p = new Pagamento();
                    p.setId(rs.getInt("id"));
                    p.setMensalidade(mensalidade);
                    p.setDataPagamento(rs.getDate("data_pagamento").toLocalDate());
                    p.setValorPago(rs.getDouble("valor_pago"));
                    p.setMetodo(rs.getString("metodo"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar os últimos pagamentos: " + e.getMessage(), e);
        }
        return lista;
    }

    private int contar(String sql) {
        try (Statement stmt = ConexaoBD.getConexao().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar o painel: " + e.getMessage(), e);
        }
    }
}
