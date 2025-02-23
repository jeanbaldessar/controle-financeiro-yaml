package br.baldessar;
public class Saldo {
    private String conta;
    private String data;
    private double valor;

    // Getters e Setters
    public String getConta() {
        return conta;
    }

    public void setConta(String conta) {
        this.conta = conta;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Saldo{conta='" + conta + "', data='" + data + "', valor=" + valor + "}";
    }
}