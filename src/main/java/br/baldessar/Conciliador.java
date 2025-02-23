package br.baldessar;
import java.util.List;

public class Conciliador {

    public static void conciliarSaldos(List<Saldo> saldosInformados, List<Lancamento> lancamentos) {
        // Itera sobre os saldos informados
        for (Saldo saldoInformado : saldosInformados) {
            String conta = saldoInformado.getConta();
            double saldoCalculado = calcularSaldoPorConta(conta, lancamentos);

            System.out.println("\nConciliação para a conta: " + conta);
            System.out.println("Saldo informado: " + saldoInformado.getValor());
            System.out.println("Saldo calculado: " + saldoCalculado);

            if (saldoInformado.getValor() == saldoCalculado) {
                System.out.println("Status: Conciliado com sucesso!");
            } else {
                System.out.println("Status: Divergência encontrada!");
            }
        }
    }

    private static double calcularSaldoPorConta(String conta, List<Lancamento> lancamentos) {
        double saldoCalculado = 0.0;

        // Itera sobre os lançamentos para calcular o saldo da conta
        for (Lancamento lancamento : lancamentos) {
            if (lancamento.getOrigem().equals(conta)) {
                saldoCalculado -= lancamento.getValor(); // Subtrai o valor do lançamento (saída de dinheiro)
            }
            if (lancamento.getDestino().equals(conta)) {
                saldoCalculado += lancamento.getValor(); // Adiciona o valor do lançamento (entrada de dinheiro)
            }
        }

        return saldoCalculado;
    }
}