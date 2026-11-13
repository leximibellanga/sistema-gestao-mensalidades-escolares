package App.service;

import App.dao.DashboardDAO;
import App.model.ResumoDashboard;
import App.model.StatusMensalidade;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardService {

    private static final int MESES_NO_GRAFICO = 6;
    private static final int PAGAMENTOS_RECENTES = 6;

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final MensalidadeService mensalidadeService = new MensalidadeService();

    public ResumoDashboard carregar() {
        // Marca as pendentes de meses já terminados como atrasadas: os números batem com os outros ecrãs
        mensalidadeService.atualizarStatusAtrasados();

        LocalDate mes = LocalDate.now().withDayOfMonth(1);

        ResumoDashboard r = new ResumoDashboard();
        r.mes = mes;
        r.totalAlunos = dashboardDAO.contarAlunos();
        r.totalTurmas = dashboardDAO.contarTurmas();

        double[] pagas = dashboardDAO.totaisPorStatus(StatusMensalidade.PAGO, mes);
        r.pagasQuantidade = (int) pagas[0];
        r.pagasValor = pagas[1];

        double[] pendentes = dashboardDAO.totaisPorStatus(StatusMensalidade.PENDENTE, mes);
        r.pendentesQuantidade = (int) pendentes[0];
        r.pendentesValor = pendentes[1];

        double[] atrasadas = dashboardDAO.totaisPorStatus(StatusMensalidade.ATRASADO, null);
        r.atrasadasQuantidade = (int) atrasadas[0];
        r.atrasadasValor = atrasadas[1];

        // Gráfico: os meses sem pagamentos aparecem com 0, para a série ter sempre 6 barras
        LocalDate inicio = mes.minusMonths(MESES_NO_GRAFICO - 1);
        Map<LocalDate, Double> existentes = dashboardDAO.arrecadadoDesde(inicio);
        Map<LocalDate, Double> serie = new LinkedHashMap<>();
        for (int i = 0; i < MESES_NO_GRAFICO; i++) {
            LocalDate m = inicio.plusMonths(i);
            serie.put(m, existentes.getOrDefault(m, 0.0));
        }
        r.arrecadadoPorMes = serie;

        r.ultimosPagamentos = dashboardDAO.ultimosPagamentos(PAGAMENTOS_RECENTES);
        return r;
    }
}
