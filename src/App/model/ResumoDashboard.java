package App.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Dados do painel inicial. Objeto só de transporte: a lógica fica no
 * DashboardService.
 */
public class ResumoDashboard {

    public LocalDate mes;                              // mês em destaque (o actual)
    public int totalAlunos;
    public int totalTurmas;
    public int pagasQuantidade;                        // mensalidades pagas do mês
    public double pagasValor;
    public int pendentesQuantidade;                    // mensalidades pendentes do mês
    public double pendentesValor;
    public int atrasadasQuantidade;                    // em atraso, de todos os meses
    public double atrasadasValor;
    public Map<LocalDate, Double> arrecadadoPorMes;    // últimos 6 meses, do mais antigo ao mais recente
    public List<Pagamento> ultimosPagamentos;
}
