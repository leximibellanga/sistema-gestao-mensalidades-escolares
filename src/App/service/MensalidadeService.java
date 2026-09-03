package App.service;

import App.dao.AlunoDAO;
import App.dao.MensalidadeDAO;
import App.dao.PagamentoDAO;
import App.model.*;

import java.time.LocalDate;
import java.util.List;

public class MensalidadeService {

    private final MensalidadeDAO mensalidadeDAO = new MensalidadeDAO();
    private final PagamentoDAO pagamentoDAO = new PagamentoDAO();
    private final AlunoDAO alunoDAO = new AlunoDAO();

    /**
     * Gera uma mensalidade para cada aluno referente ao mês informado.
     * Deve ser chamado manualmente (botão na UI) ou por uma tarefa agendada.
     * @param mesReferencia
     */
    public void gerarMensalidadesDoMes(LocalDate mesReferencia) {
        List<Aluno> alunos = alunoDAO.listarTodos();

        for (Aluno aluno : alunos) {
            Mensalidade mensalidade = aluno.gerarMensalidadeMensal(mesReferencia);
            mensalidadeDAO.inserir(mensalidade);
        }
    }

    /**
     * Percorre as mensalidades pendentes com data vencida e marca-as
     * como ATRASADO. Deve ser chamado ao iniciar a aplicação ou periodicamente.
     */
    public void atualizarStatusAtrasados() {
        List<Mensalidade> pendentesVencidas = mensalidadeDAO.listarPendentesComDataVencida();

        for (Mensalidade m : pendentesVencidas) {
            mensalidadeDAO.atualizarStatus(m.getId(), StatusMensalidade.ATRASADO);
        }
    }

    /**
     * Regista o pagamento de uma mensalidade e actualiza o seu status.
     * @param mensalidade
     * @param valorPago
     * @param dataPagamento
     * @param metodo
     * @return 
     */
    public Pagamento registrarPagamento(Mensalidade mensalidade, double valorPago,
                                         LocalDate dataPagamento, String metodo) {
        if (mensalidade.getStatus() == StatusMensalidade.PAGO) {
            throw new IllegalStateException("Esta mensalidade já foi paga.");
        }

        Pagamento pagamento = new Pagamento(mensalidade, dataPagamento, valorPago, metodo);
        pagamento.registar(); // marca a mensalidade como PAGO em memória

        pagamentoDAO.inserir(pagamento);
        mensalidadeDAO.atualizarStatus(mensalidade.getId(), StatusMensalidade.PAGO);

        return pagamento;
    }

    public List<Mensalidade> listarEmAtraso() {
        return mensalidadeDAO.listarEmAtraso();
    }

    public List<Mensalidade> listarPorAluno(int alunoId) {
        return mensalidadeDAO.listarPorAluno(alunoId);
    }
    
    /**
    * Total arrecadado — soma o valor de todas as mensalidades já PAGAS
    * referentes a um dado mês.
     * @param mesReferencia
     * @return 
    */
    public double calcularTotalArrecadado(LocalDate mesReferencia) {
        List<Mensalidade> mensalidadesDoMes = mensalidadeDAO.listarPorMesReferencia(mesReferencia);

        double total = 0;
        for (Mensalidade m : mensalidadesDoMes) {
            if (m.getStatus() == StatusMensalidade.PAGO) {
                total += m.getValor();
            }
        }
        return total;
    }
}
