package App.service;

import App.dao.TurmaDAO;
import App.model.Turma;

import java.util.List;

public class TurmaService {

    private final TurmaDAO turmaDAO = new TurmaDAO();

    public void cadastrar(Turma turma) {
        validar(turma);
        turmaDAO.inserir(turma);
    }

    public void atualizar(Turma turma) {
        validar(turma);
        turmaDAO.atualizar(turma);
    }

    public void remover(int id) {
        turmaDAO.remover(id);
    }

    public Turma buscarPorId(int id) {
        return turmaDAO.buscarPorId(id);
    }

    public List<Turma> listarTodas() {
        return turmaDAO.listarTodas();
    }

    private void validar(Turma turma) {
        if (turma.getNome() == null || turma.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da turma é obrigatório.");
        }
        if (turma.getValorMensalidade() <= 0) {
            throw new IllegalArgumentException("O valor da mensalidade deve ser maior que zero.");
        }
    }
}