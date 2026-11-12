package App.service;

import App.dao.AlunoDAO;
import App.dao.MensalidadeDAO;
import App.dao.PagamentoDAO;
import App.model.Aluno;
import App.model.Mensalidade;
import App.model.Pagamento;
import App.model.StatusMensalidade;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MensalidadeService {

    private final MensalidadeDAO mensalidadeDAO = new MensalidadeDAO();
    private final PagamentoDAO pagamentoDAO = new PagamentoDAO();
    private final AlunoDAO alunoDAO = new AlunoDAO();

    /**
     * Gera uma mensalidade por aluno para o mês indicado, ignorando os alunos
     * que já têm mensalidade nesse mês (evita duplicados).
     * Devolve quantas mensalidades foram criadas.
     */
    public int gerarMensalidadesDoMes(LocalDate mesReferencia) {
        LocalDate mes = mesReferencia.withDayOfMonth(1);

        Set<Integer> alunosComMensalidade = new HashSet<>();
        for (Mensalidade m : mensalidadeDAO.listarPorMesReferencia(mes)) {
            alunosComMensalidade.add(m.getAluno().getId());
        }

        int geradas = 0;
        for (Aluno aluno : alunoDAO.listarTodos()) {
            if (alunosComMensalidade.contains(aluno.getId())) {
                continue;
            }
            mensalidadeDAO.inserir(aluno.gerarMensalidadeMensal(mes));
            geradas++;
        }
        return geradas;
    }

    /**
     * Marca como ATRASADO as pendentes de meses já terminados.
     * Devolve quantas foram actualizadas.
     */
    public int atualizarStatusAtrasados() {
        List<Mensalidade> pendentesVencidas = mensalidadeDAO.listarPendentesComDataVencida();

        for (Mensalidade m : pendentesVencidas) {
            mensalidadeDAO.atualizarStatus(m.getId(), StatusMensalidade.ATRASADO);
        }
        return pendentesVencidas.size();
    }

    /**
     * Regista o pagamento integral de uma mensalidade e marca-a como PAGO.
     */
    public Pagamento registrarPagamento(Mensalidade mensalidade, double valorPago,
                                         LocalDate dataPagamento, String metodo) {
        if (mensalidade.getStatus() == StatusMensalidade.PAGO) {
            throw new IllegalStateException("Esta mensalidade já foi paga.");
        }
        if (valorPago <= 0) {
            throw new IllegalArgumentException("O valor pago deve ser maior que zero.");
        }
        if (Math.abs(valorPago - mensalidade.getValor()) > 0.005) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "O valor pago deve ser igual ao valor da mensalidade (%.2f MT).",
                    mensalidade.getValor()));
        }

        Pagamento pagamento = new Pagamento(mensalidade, dataPagamento, valorPago, metodo);

        pagamentoDAO.inserir(pagamento);
        mensalidadeDAO.atualizarStatus(mensalidade.getId(), StatusMensalidade.PAGO);
        pagamento.registar(); // só depois de gravar: actualiza o objecto em memória

        return pagamento;
    }

    public List<Mensalidade> listarTodas() {
        return mensalidadeDAO.listarTodas();
    }

    public List<Mensalidade> listarPorMes(LocalDate mes) {
        return mensalidadeDAO.listarPorMesReferencia(mes.withDayOfMonth(1));
    }

    public List<Mensalidade> listarEmAtraso() {
        return mensalidadeDAO.listarEmAtraso();
    }

    public List<Mensalidade> listarPorAluno(int alunoId) {
        return mensalidadeDAO.listarPorAluno(alunoId);
    }

    /**
     * Total arrecadado: soma o valor das mensalidades PAGAS do mês.
     */
    public double calcularTotalArrecadado(LocalDate mesReferencia) {
        double total = 0;
        for (Mensalidade m : mensalidadeDAO.listarPorMesReferencia(mesReferencia.withDayOfMonth(1))) {
            if (m.getStatus() == StatusMensalidade.PAGO) {
                total += m.getValor();
            }
        }
        return total;
    }
}