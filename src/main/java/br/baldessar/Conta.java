package br.baldessar;
public class Conta {
    private String nome;
    private String apelido;

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    @Override
    public String toString() {
        return "Conta{nome='" + nome + "', apelido='" + apelido + "'}";
    }
}