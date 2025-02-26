package br.baldessar;

import java.math.BigDecimal;
import java.util.Date;

public class Saldo {
    private String conta;
    private Date data;
    private BigDecimal valor;

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

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Saldo{conta='" + conta + "', data='" + data + "', valor=" + valor + "}";
    }
}