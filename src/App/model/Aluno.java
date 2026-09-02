package App.model;

import java.time.LocalDate;

public class Aluno {
    private int id;
    private String nome;
    private String numeroEstudante;
    private String encarregado;
    private String contacto;
    private Turma turma;
    private LocalDate dataMatricula;
    
    // construtores
    public Aluno() {
    }

    public Aluno(String nome, String numeroEstudante, String encarregado, String contacto, Turma turma, LocalDate dataMatricula) {
        this.nome = nome;
        this.numeroEstudante = numeroEstudante;
        this.encarregado = encarregado;
        this.contacto = contacto;
        this.turma = turma;
        this.dataMatricula = dataMatricula;
    }
    
    // getters e setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNumeroEstudante() {
        return numeroEstudante;
    }

    public void setNumeroEstudante(String numeroEstudante) {
        this.numeroEstudante = numeroEstudante;
    }

    public String getEncarregado() {
        return encarregado;
    }

    public void setEncarregado(String encarregado) {
        this.encarregado = encarregado;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public LocalDate getDataMatricula() {
        return dataMatricula;
    }

    public void setDataMatricula(LocalDate dataMatricula) {
        this.dataMatricula = dataMatricula;
    }

    /**
     * Cria uma mensalidade para este usuario referente ao mes,
     * usando o valor definido na turma
     * @param mesReferencia
     * @return 
    */
    public Mensalidade gerarMensalidadeMensal(LocalDate mesReferencia) {
        double valor = (turma != null) ? turma.getValorMensalidade() : 0.0;
        return new Mensalidade(this, mesReferencia, valor, StatusMensalidade.PENDENTE);
    }

    @Override
    public String toString() {
        return "Aluno{" + "id=" + id + ", nome=" + nome + ", numeroEstudante=" + numeroEstudante + ", encarregado=" + encarregado + ", contacto=" + contacto + ", turma=" + turma + ", dataMatricula=" + dataMatricula + '}';
    }
}
