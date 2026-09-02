package App.model;

import java.time.LocalDate;

public class Pagamento {
    private int id;
    private Mensalidade mensalidade;
    private LocalDate dataPagamento;
    private double valorPago;
    private String metodo;
    
    // construtores
    public Pagamento() {
    }
    
    public Pagamento(Mensalidade mensalidade, LocalDate dataPagamento, double valorPago, String metodo) {
        this.mensalidade = mensalidade;
        this.dataPagamento = dataPagamento;
        this.valorPago = valorPago;
        this.metodo = metodo;
    }
    
    // getters e setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Mensalidade getMensalidade() {
        return mensalidade;
    }

    public void setMensalidade(Mensalidade mensalidade) {
        this.mensalidade = mensalidade;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public double getValorPago() {
        return valorPago;
    }

    public void setValorPago(double valorPago) {
        this.valorPago = valorPago;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }
    
    // confirmar o pagamento e actualiza o estado da mensalidade associada.
    public void registar() {
        if (mensalidade != null) {
            mensalidade.marcarComoPago();
        }
    }
    
    // gerar recibo de pagamento
    public String gerarRecibo() {
        return String.format(
                "RECIBO%nAluno: %s%nMes ref.: %s%nValor pago: %.2f MT%nData: %s%nMetodo: %s",
                mensalidade.getAluno().getNome(),
                mensalidade.getMesReferencia(),
                valorPago,
                dataPagamento,
                metodo
        );
    }

    @Override
    public String toString() {
        return "Pagamento{" + "dataPagamento=" + dataPagamento + ", valorPago=" + valorPago + '}';
    } 
}

