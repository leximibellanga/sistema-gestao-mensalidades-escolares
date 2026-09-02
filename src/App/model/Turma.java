package App.model;

public class Turma {
    private int id;
    private String nome;
    private double valorMensalidade;
    
    // construtores
    public Turma() {
    }

    public Turma(String nome, double valorMensalidade) {
        this.nome = nome;
        this.valorMensalidade = valorMensalidade;
    }

    public Turma(int id, String nome, double valorMensalidade) {
        this.id = id;
        this.nome = nome;
        this.valorMensalidade = valorMensalidade;
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

    public double getValorMensalidade() {
        return valorMensalidade;
    }

    public void setValorMensalidade(double valorMensalidade) {
        this.valorMensalidade = valorMensalidade;
    }

    // util para exibir no ComboBox
    @Override
    public String toString() {
        return "Turma: " + nome;
    }
}

