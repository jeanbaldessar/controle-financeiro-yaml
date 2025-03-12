package br.baldessar.model;

import java.math.BigDecimal;
import java.util.Date;

public class Lancamento {
    private String conciliado;
    private String identificado;
    private String origem;
    private Date data;
    private BigDecimal valor;
    private String descricao;
    private String destino;

    // Getters e Setters
    public String getConciliado() {
        return conciliado;
    }

    public void setConciliado(String conciliado) {
        this.conciliado = conciliado;
    }

    public String getIdentificado() {
        return identificado;
    }

    public void setIdentificado(String identificado) {
        this.identificado = identificado;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    @Override
    public String toString() {
        return "Lancamento{conciliado='" + conciliado + "', identificado='" + identificado + "', origem='" + origem +
                "', data='" + data + "', valor=" + valor + ", descricao='" + descricao + "', destino='" + destino + "'}";
    }
}