package App.dao;

import App.model.Mensalidade;
import App.model.Pagamento;
import App.util.ConexaoBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoDAO {

    private final MensalidadeDAO mensalidadeDAO = new MensalidadeDAO();

    public void inserir(Pagamento pagamento) {
        String sql = "INSERT INTO pagamento (mensalidade_id, data_pagamento, valor_pago, metodo) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pagamento.getMensalidade().getId());
            stmt.setDate(2, Date.valueOf(pagamento.getDataPagamento()));
            stmt.setDouble(3, pagamento.getValorPago());
            stmt.setString(4, pagamento.getMetodo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pagamento.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir pagamento: " + e.getMessage(), e);
        }
    }

    public List<Pagamento> listarPorMensalidade(int mensalidadeId) {
        List<Pagamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagamento WHERE mensalidade_id = ? ORDER BY data_pagamento";
        try (PreparedStatement stmt = ConexaoBD.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, mensalidadeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pagamentos: " + e.getMessage(), e);
        }
        return lista;
    }

    private Pagamento mapear(ResultSet rs) throws SQLException {
        Mensalidade mensalidade = mensalidadeDAO.buscarPorId(rs.getInt("mensalidade_id"));

        Pagamento p = new Pagamento();
        p.setId(rs.getInt("id"));
        p.setMensalidade(mensalidade);
        p.setDataPagamento(rs.getDate("data_pagamento").toLocalDate());
        p.setValorPago(rs.getDouble("valor_pago"));
        p.setMetodo(rs.getString("metodo"));

        return p;
    }
}
