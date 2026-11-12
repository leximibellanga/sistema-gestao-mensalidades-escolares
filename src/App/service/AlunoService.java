package App.service;

import App.dao.AlunoDAO;
import App.dao.MensalidadeDAO;
import App.model.Aluno;
import App.model.Turma;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class AlunoService {
    private final AlunoDAO alunoDAO = new AlunoDAO();
    private final MensalidadeDAO mensalidadeDAO = new MensalidadeDAO();

    /**
     * Regista um aluno novo. O número de estudante é gerado aqui, no formato
     * SIGLA.SEQUÊNCIA.ANO (ex: GES.10.2026), por curso e por ano de matrícula.
     */
    public void cadastrar(Aluno aluno) {
        validarNome(aluno);
        if (aluno.getTurma() == null) {
            throw new IllegalArgumentException("O aluno deve estar associado a uma turma.");
        }
        if (aluno.getDataMatricula() == null) {
            throw new IllegalArgumentException("A data de matrícula é obrigatória.");
        }

        String sigla = siglaDoCurso(aluno.getTurma());
        int ano = aluno.getDataMatricula().getYear();
        int proximo = alunoDAO.ultimoSequencial(sigla, ano) + 1;

        aluno.setNumeroEstudante(sigla + "." + proximo + "." + ano);
        alunoDAO.inserir(aluno);
    }

    /** Só os dados pessoais (nome, encarregado, contacto) podem ser alterados. */
    public void atualizar(Aluno aluno) {
        validarNome(aluno);
        alunoDAO.atualizar(aluno);
    }

    /**
     * Remove um aluno que nunca teve mensalidades. Se já tiver, não remove:
     * apagaria o histórico financeiro (e a base de dados também o impede).
     */
    public void remover(String nrEstudante) {
        Aluno aluno = alunoDAO.buscarPorNrEstudante(nrEstudante);
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não encontrado.");
        }
        int mensalidades = mensalidadeDAO.listarPorAluno(aluno.getId()).size();
        if (mensalidades > 0) {
            throw new IllegalStateException("Não é possível remover \"" + aluno.getNome() + "\".\n"
                    + "Tem " + mensalidades + " mensalidade(s) registada(s) e removê-lo apagaria o histórico financeiro.");
        }
        alunoDAO.remover(nrEstudante);
    }

    public Aluno buscarPorId(int id) {
        return alunoDAO.buscarPorId(id);
    }

    public Aluno buscarPorNrEstudante(String nrEst) {
        return alunoDAO.buscarPorNrEstudante(nrEst);
    }

    public List<Aluno> listarTodos() {
        return alunoDAO.listarTodos();
    }

    public List<Aluno> listarPorTurma(int turmaId) {
        return alunoDAO.listarPorTurma(turmaId);
    }

    private void validarNome(Aluno aluno) {
        if (aluno.getNome() == null || aluno.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do aluno é obrigatório.");
        }
    }

    /**
     * Sigla do curso: as 3 primeiras letras do nome da turma, sem acentos e em maiúsculas.
     * Ex: "Gestão" -> GES, "Informática" -> INF. É aqui que se muda a regra, se for preciso.
     */
    private String siglaDoCurso(Turma turma) {
        String letras = Normalizer.normalize(turma.getNome(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z]", "")
                .toUpperCase(Locale.ROOT);
        if (letras.isEmpty()) {
            throw new IllegalArgumentException(
                    "O nome da turma \"" + turma.getNome() + "\" não tem letras para formar a sigla do curso.");
        }
        return letras.length() > 3 ? letras.substring(0, 3) : letras;
    }
}