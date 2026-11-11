package App.service;

import App.dao.MensalidadeDAO;
import App.dao.PagamentoDAO;
import App.model.Aluno;
import App.model.Mensalidade;
import App.model.Pagamento;
import App.model.Relatorio;
import App.model.StatusMensalidade;
import App.model.Turma;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RelatorioService {

    public static final String[] NOMES_MESES = {
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MM/yyyy");

    private final MensalidadeDAO mensalidadeDAO = new MensalidadeDAO();
    private final PagamentoDAO pagamentoDAO = new PagamentoDAO();
    private final MensalidadeService mensalidadeService = new MensalidadeService();

    /** Mensalidades em atraso; turma == null significa todas as turmas. */
    public Relatorio mensalidadesEmAtraso(Turma turma) {
        mensalidadeService.atualizarStatusAtrasados(); // mesma regra do ecrã de Mensalidades

        List<Mensalidade> lista = mensalidadeDAO.listarEmAtraso().stream()
                .filter(m -> turma == null
                        || (m.getAluno().getTurma() != null && m.getAluno().getTurma().getId() == turma.getId()))
                .sorted(Comparator.comparing(Mensalidade::getMesReferencia)
                        .thenComparing(m -> m.getAluno().getNome()))
                .collect(Collectors.toList());

        Relatorio r = new Relatorio("Mensalidades em atraso",
                "Turma: " + (turma == null ? "Todas" : turma.getNome()),
                new String[]{"Nr. Estudante", "Aluno", "Turma", "Mês Ref.", "Valor"});

        double total = 0;
        for (Mensalidade m : lista) {
            r.adicionarLinha(
                    m.getAluno().getNumeroEstudante(),
                    m.getAluno().getNome(),
                    nomeTurma(m),
                    m.getMesReferencia().format(FORMATO_MES),
                    m.getValor());
            total += m.getValor();
        }
        if (!lista.isEmpty()) {
            r.setLinhaTotal("TOTAL", "", "", "", total);
        }
        r.setResumo(lista.size() + " mensalidade(s) em atraso  |  Total em falta: " + moeda(total));
        return r;
    }

    /** Pagas, pendentes e atrasadas de cada turma num mês. */
    public Relatorio resumoPorTurma(LocalDate mes) {
        LocalDate mesRef = mes.withDayOfMonth(1);
        mensalidadeService.atualizarStatusAtrasados();

        Map<String, Acumulado> porTurma = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Mensalidade m : mensalidadeDAO.listarPorMesReferencia(mesRef)) {
            porTurma.computeIfAbsent(nomeTurma(m), k -> new Acumulado()).adicionar(m);
        }

        Relatorio r = new Relatorio("Resumo por turma", "Mês: " + nomeMes(mesRef),
                new String[]{"Turma", "Mensalidades", "Pagas", "Pendentes", "Atrasadas", "Arrecadado", "Em falta"});

        Acumulado geral = new Acumulado();
        for (Map.Entry<String, Acumulado> e : porTurma.entrySet()) {
            Acumulado a = e.getValue();
            r.adicionarLinha(e.getKey(), a.total, a.pagas, a.pendentes, a.atrasadas, a.arrecadado, a.emFalta);
            geral.somar(a);
        }
        if (!porTurma.isEmpty()) {
            r.setLinhaTotal("TOTAL", geral.total, geral.pagas, geral.pendentes, geral.atrasadas,
                    geral.arrecadado, geral.emFalta);
        }
        r.setResumo(geral.total + " mensalidade(s)  |  Arrecadado: " + moeda(geral.arrecadado)
                + "  |  Em falta: " + moeda(geral.emFalta));
        return r;
    }

    /** Total arrecadado em cada mês de um ano (conta pelo mês de referência da mensalidade). */
    public Relatorio arrecadadoPorMes(int ano) {
        int[] quantidade = new int[12];
        double[] valor = new double[12];

        for (Mensalidade m : mensalidadeDAO.listarTodas()) {
            if (m.getStatus() == StatusMensalidade.PAGO && m.getMesReferencia().getYear() == ano) {
                int i = m.getMesReferencia().getMonthValue() - 1;
                quantidade[i]++;
                valor[i] += m.getValor();
            }
        }

        Relatorio r = new Relatorio("Arrecadado por mês", "Ano: " + ano,
                new String[]{"Mês", "Mensalidades pagas", "Total arrecadado"});

        int somaQuantidade = 0;
        double somaValor = 0;
        for (int i = 0; i < 12; i++) {
            r.adicionarLinha(NOMES_MESES[i], quantidade[i], valor[i]);
            somaQuantidade += quantidade[i];
            somaValor += valor[i];
        }
        r.setLinhaTotal("TOTAL", somaQuantidade, somaValor);

        if (somaQuantidade == 0) {
            // sem pagamentos no ano: o painel mostra "nenhum registo"
            return new Relatorio(r.getTitulo(), r.getSubtitulo(), r.getColunas());
        }
        r.setResumo(somaQuantidade + " mensalidade(s) paga(s)  |  Total do ano: " + moeda(somaValor));
        return r;
    }

    /** Pagamentos feitos por um aluno, do mais antigo para o mais recente. */
    public Relatorio historicoPagamentos(Aluno aluno) {
        List<Pagamento> pagamentos = pagamentoDAO.listarPorAluno(aluno.getId());

        Relatorio r = new Relatorio("Histórico de pagamentos",
                "Aluno: " + aluno.getNome() + " (" + aluno.getNumeroEstudante() + ")",
                new String[]{"Mês Ref.", "Data Pagamento", "Valor", "Método"});

        double total = 0;
        for (Pagamento p : pagamentos) {
            r.adicionarLinha(
                    p.getMensalidade().getMesReferencia().format(FORMATO_MES),
                    p.getDataPagamento(),
                    p.getValorPago(),
                    p.getMetodo());
            total += p.getValorPago();
        }
        if (!pagamentos.isEmpty()) {
            r.setLinhaTotal("TOTAL", "", total, "");
        }
        r.setResumo(pagamentos.size() + " pagamento(s)  |  Total pago: " + moeda(total));
        return r;
    }

    // ---------- Auxiliares ----------

    private String nomeTurma(Mensalidade m) {
        return m.getAluno().getTurma() != null ? m.getAluno().getTurma().getNome() : "(sem turma)";
    }

    private String nomeMes(LocalDate mes) {
        return NOMES_MESES[mes.getMonthValue() - 1] + " / " + mes.getYear();
    }

    private String moeda(double valor) {
        return String.format(Locale.US, "%.2f MT", valor);
    }

    /** Contadores e somas de um grupo de mensalidades. */
    private static class Acumulado {
        int total;
        int pagas;
        int pendentes;
        int atrasadas;
        double arrecadado;
        double emFalta;

        void adicionar(Mensalidade m) {
            total++;
            switch (m.getStatus()) {
                case PAGO -> {
                    pagas++;
                    arrecadado += m.getValor();
                }
                case PENDENTE -> {
                    pendentes++;
                    emFalta += m.getValor();
                }
                case ATRASADO -> {
                    atrasadas++;
                    emFalta += m.getValor();
                }
            }
        }

        void somar(Acumulado o) {
            total += o.total;
            pagas += o.pagas;
            pendentes += o.pendentes;
            atrasadas += o.atrasadas;
            arrecadado += o.arrecadado;
            emFalta += o.emFalta;
        }
    }
}