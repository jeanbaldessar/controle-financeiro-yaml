package br.baldessar;

import java.util.Date;

public class Saldo {
    private String conta;
    private Date data;
    private double valor;

    // Getters e Setters
    public String getConta() {
        return conta;
    }

    public void setConta(String conta) {
        this.conta = conta;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
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