package App.service;

import App.dao.AlunoDAO;
import App.model.Aluno;

import java.util.List;

public class AlunoService {
    private final AlunoDAO alunoDAO = new AlunoDAO();

    public void cadastrar(Aluno aluno) {
        validar(aluno);
        alunoDAO.inserir(aluno);
    }

    public void atualizar(Aluno aluno) {
        validar(aluno);
        alunoDAO.atualizar(aluno);
    }

    public void remover(int id) {
        alunoDAO.remover(id);
    }

    public Aluno buscarPorId(int id) {
        return alunoDAO.buscarPorId(id);
    }

    public List<Aluno> listarTodos() {
        return alunoDAO.listarTodos();
    }

    public List<Aluno> listarPorTurma(int turmaId) {
        return alunoDAO.listarPorTurma(turmaId);
    }

    private void validar(Aluno aluno) {
        if (aluno.getNome() == null || aluno.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do aluno é obrigatório.");
        }
        if (aluno.getNumeroEstudante() == null || aluno.getNumeroEstudante().isBlank()) {
            throw new IllegalArgumentException("O número de estudante é obrigatório.");
        }
        if (aluno.getTurma() == null) {
            throw new IllegalArgumentException("O aluno deve estar associado a uma turma.");
        }
    }
}
