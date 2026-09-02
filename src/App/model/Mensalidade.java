package App.model;

import java.time.LocalDate;

public class Mensalidade {
    private int id;
    private Aluno aluno;
    private LocalDate mesReferencia;
    private double valor;
    private StatusMensalidade status;

    // construtores
    public Mensalidade() {
    }
    
    public Mensalidade(Aluno aluno, LocalDate mesReferencia, double valor, StatusMensalidade status) {
        this.aluno = aluno;
        this.mesReferencia = mesReferencia;
        this.valor = valor;
        this.status = status;
    }
    
    // getters e setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public LocalDate getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(LocalDate mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public StatusMensalidade getStatus() {
        return status;
    }

    public void setStatus(StatusMensalidade status) {
        this.status = status;
    }
    
    public void marcarComoPago() {
        this.status = StatusMensalidade.PAGO;
    }
    
    // verifica se a mensalidade esta em atraso: ainda pendente e a data de referencia ja passou
    public boolean calcularAtraso() {
        if (status == StatusMensalidade.PAGO) {
            return false;
        }
        boolean atrasada = mesReferencia.isBefore(LocalDate.now());
        if (atrasada) {
            this.status = StatusMensalidade.ATRASADO;
        }
        return atrasada;
    }
    
    @Override
    public String toString() {
        return aluno.getNome() + " - " + mesReferencia + " - " + status;
    }
}
