package br.baldessar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

import org.yaml.snakeyaml.Yaml;

import br.baldessar.model.Conta;
import br.baldessar.model.Lancamento;
import br.baldessar.model.Saldo;
import br.baldessar.util.LocaleUtils;

public class Main {

	
    

    public static void main(String[] args) throws FileNotFoundException {
        long inicio = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // Diretório onde os arquivos YAML estão localizados
        String directoryPath = "data";

        // Cria um objeto File para o diretório
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".yaml"));
        // Ordena os arquivos pelo nome
        Arrays.sort(files, Comparator.comparing(File::getName));

        Yaml yaml = new Yaml();

        List<Conta> contas = new ArrayList<Conta>();
        List<Saldo> saldos = new ArrayList<Saldo>();
        List<Lancamento> lancamentos = new ArrayList<Lancamento>();

        // Itera sobre cada arquivo YAML
        for (File file : files) {
            InputStream inputStream = new FileInputStream(file);
            try {

                // Carrega o conteúdo do arquivo YAML
                Map<String, Object> obj = yaml.load(inputStream);

                contas.addAll(leContas(obj));

                saldos.addAll(leSaldos(obj));

                lancamentos.addAll(leLancamentos(obj));
            } catch (Exception e) {
                throw new RuntimeException("Erro ao ler arquivo " + file.getAbsolutePath(), e);
            }

        }
        
        Map<String, Conta> contasPorApelido = transformarListaContasEmMapaPorApelido(contas);

        // Conciliação de saldos
        System.out.println("\nConciliação de saldos:");
        conciliarSaldos(saldos, lancamentos, contas);

        System.out.println("\nBalanço:");
        List<TreeMap<String, BigDecimal>> listaSaldos = calcularBalancoContas(contasPorApelido, lancamentos);
		Map<String, BigDecimal> calcularBalancoContas = listaSaldos.get(0);
		Map<String, BigDecimal> calcularRegistrosContas = listaSaldos.get(1);
        for (Entry<String, BigDecimal> balanco : calcularBalancoContas.entrySet()) {
            BigDecimal bigDecimal = calcularRegistrosContas.get(balanco.getKey());
			System.out.println(balanco.getKey() + ": " + LocaleUtils.decimalFormat.format(balanco.getValue())+" ("+bigDecimal+")");
        }

        long fim = System.currentTimeMillis();

        // Calcula o tempo decorrido
        long tempoDecorrido = fim - inicio;

        // Exibe o tempo decorrido em milissegundos
        System.out.println("\n\nTempo decorrido: " + tempoDecorrido + " ms");
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        System.out.println("Memória utilizada: " + memoryUsed + " bytes");
        System.out.println("Memória utilizada: " + (memoryUsed / 1024) + " KB");
        System.out.println("Memória utilizada: " + (memoryUsed / (1024 * 1024)) + " MB");

    }

    private static List<TreeMap<String, BigDecimal>> calcularBalancoContas(Map<String, Conta> contasPorApelido, List<Lancamento> lancamentos) {
	    Map<String, BigDecimal> balancoContas = new HashMap<>();
	    Map<String, BigDecimal> registrosContas = new HashMap<>();
	
	    // Processa os lançamentos
	    for (Lancamento lancamento : lancamentos) {
	        String origem = lancamento.getOrigem();
	        String destino = lancamento.getDestino();
	        if(origem==null&&destino==null) throw new RuntimeException("Lançamento sem origem e destino");
	        if(origem==null) origem = "outro";
	        if(destino==null) destino = "outro";
	        BigDecimal valor = lancamento.getValor();
	
	        // Subtrai o valor da conta de origem
	        balancoContas.put(origem, balancoContas.getOrDefault(origem, BigDecimal.ZERO).subtract(valor));
	        registrosContas.put(origem, registrosContas.getOrDefault(origem, BigDecimal.ZERO).add(BigDecimal.ONE));
	
	        // Adiciona o valor à conta de destino
	        balancoContas.put(destino, balancoContas.getOrDefault(destino, BigDecimal.ZERO).add(valor));
	        registrosContas.put(destino, registrosContas.getOrDefault(destino, BigDecimal.ZERO).add(BigDecimal.ONE));
	    }
	
	    // Agrupa os saldos por hierarquia de contas
	    Map<String, BigDecimal> balancoAgrupado = new HashMap<>();
	    for (Map.Entry<String, BigDecimal> entry : balancoContas.entrySet()) {
	        String conta = entry.getKey();
	        BigDecimal valor = entry.getValue();
	
	        // Adiciona o valor à conta principal e a todas as suas subcontas
	        Conta conta2 = contasPorApelido.get(conta);
	        if(conta2==null) throw new RuntimeException("Não encontrou a conta "+conta);
			String[] partesConta = conta2.getNome().split(" *> *");
	        String contaAtual = "";
	        for (String parte : partesConta) {
	            contaAtual = contaAtual.isEmpty() ? parte : contaAtual + " > " + parte;
	            balancoAgrupado.put(contaAtual, balancoAgrupado.getOrDefault(contaAtual, BigDecimal.ZERO).add(valor));
	        }
	    }
	    
	 // Agrupa os saldos por hierarquia de contas
	    Map<String, BigDecimal> registrosContasAgrupado = new HashMap<>();
	    for (Map.Entry<String, BigDecimal> entry : registrosContas.entrySet()) {
	        String conta = entry.getKey();
	        BigDecimal valor = entry.getValue();
	
	        // Adiciona o valor à conta principal e a todas as suas subcontas
	        Conta conta2 = contasPorApelido.get(conta);
	        if(conta2==null) throw new RuntimeException("Não encontrou a conta "+conta);
			String[] partesConta = conta2.getNome().split(" *> *");
	        String contaAtual = "";
	        for (String parte : partesConta) {
	            contaAtual = contaAtual.isEmpty() ? parte : contaAtual + " > " + parte;
	            registrosContasAgrupado.put(contaAtual, registrosContasAgrupado.getOrDefault(contaAtual, BigDecimal.ZERO).add(valor));
	        }
	    }
	
	    return Arrays.asList(new TreeMap<String, BigDecimal>(balancoAgrupado), new TreeMap<String, BigDecimal>(registrosContasAgrupado));
	}

    private static Map<String, Conta> transformarListaContasEmMapaPorApelido(List<Conta> contas) {
	    Map<String, Conta> mapaContasPorApelido = new HashMap<>();
	
	    for (Conta conta : contas) {
	        String apelido = conta.getApelido();
	        if (apelido != null && !apelido.isEmpty()) {
	            mapaContasPorApelido.put(apelido, conta);
	        }
	    }
	
	    return mapaContasPorApelido;
	}

	private static void conciliarSaldos(List<Saldo> saldosInformados, List<Lancamento> lancamentos, List<Conta> contas) {
	    // Itera sobre os saldos informados
		
		Collections.sort(saldosInformados, Comparator.comparing(Saldo::getConta).thenComparing(Saldo::getData));
		
		String contaAnterior = "X";
		StringBuffer buf = new StringBuffer();
	    for (Saldo saldoInformado : saldosInformados) {
	        String conta = saldoInformado.getConta();
	        BigDecimal saldoCalculado = calcularSaldoPorConta(conta, lancamentos, saldoInformado.getData());
	
	        if(!contaAnterior.equals(saldoInformado.getConta())) {
	        	buf.append("\nConciliação para a conta: " + conta+"\n");
	        	contaAnterior = saldoInformado.getConta();
	        }
	        boolean bateu = saldoInformado.getValor().compareTo(saldoCalculado) == 0;
	        if(bateu) {
	        	buf.append("  - " + LocaleUtils.dateFormat.format(saldoInformado.getData())+" - sucesso "+LocaleUtils.decimalFormat.format(saldoInformado.getValor())+"\n");
	        }else {
	        	throw new RuntimeException("Erro ao conciliar saldo: "+saldoInformado+" Calculado: "+LocaleUtils.decimalFormat.format(saldoCalculado));
//	        	buf.append("  - " + dateFormat.format(saldoInformado.getData())+" - "+(bateu ?"sucesso":"divergência de "+decimalFormat.format(saldoInformado.getValor().subtract(saldoCalculado)))+"\n");
//	        	buf.append("    Saldo informado: " + decimalFormat.format(saldoInformado.getValor())+"\n");
//	        	buf.append("    Saldo calculado: " + decimalFormat.format(saldoCalculado)+"\n");
	        }
	
	    }
//	    System.out.println(buf);
	}

	private static List<Saldo> leSaldos(Map<String, Object> obj) {
        List<Map<String, Object>> saldosYaml = (List<Map<String, Object>>) obj.get("saldos");
        if (saldosYaml == null) return new ArrayList<Saldo>();
        saldosYaml = leAgrupamento(saldosYaml, "agrupados");
        List<Saldo> saldos = new ArrayList<>();
        for (Map<String, Object> saldoMap : saldosYaml) {
            Saldo saldo = new Saldo();
            saldo.setConta((String) saldoMap.get("conta"));
            saldo.setData((Date) saldoMap.get("data"));
            saldo.setValor(new BigDecimal(saldoMap.get("valor").toString()));
            saldos.add(saldo);
        }
        return saldos;
    }

    private static List<Lancamento> leLancamentos(Map<String, Object> obj) {
        // Mapear lançamentos
        List<Map<String, Object>> lancamentosYaml = (List<Map<String, Object>>) obj.get("lancamentos");
        if (lancamentosYaml == null) return new ArrayList<Lancamento>();
        lancamentosYaml = leAgrupamento(lancamentosYaml, "agrupados");
        List<Lancamento> lancamentos = new ArrayList<>();
        for (Map<String, Object> lancamentoMap : lancamentosYaml) {
            try {
				Lancamento lancamento = new Lancamento();
				lancamento.setConciliado((String) lancamentoMap.get("conciliado"));
				lancamento.setIdentificado((String) lancamentoMap.get("identificado"));
				lancamento.setOrigem((String) lancamentoMap.get("origem"));
				lancamento.setData((Date) lancamentoMap.get("data"));
				lancamento.setValor(new BigDecimal(lancamentoMap.get("valor").toString()));
				lancamento.setDescricao((String) lancamentoMap.get("descricao"));
				lancamento.setDestino((String) lancamentoMap.get("destino"));
				lancamentos.add(lancamento);
			} catch (Exception e) {
				throw new RuntimeException(lancamentoMap.toString(), e);
			}
        }
        return lancamentos;
    }

    private static List<Conta> leContas(Map<String, Object> obj) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contasYaml = (List<Map<String, Object>>) obj.get("contas");
        if (contasYaml == null) return new ArrayList<Conta>();
        contasYaml = leAgrupamento(contasYaml, "agrupados");
        List<Conta> contas = new ArrayList<>();
        for (Map<String, Object> contaMap : contasYaml) {
            Conta conta = new Conta();
            conta.setNome((String) contaMap.get("nome"));
            conta.setApelido((String) contaMap.get("apelido"));
            contas.add(conta);
        }
        return contas;
    }

    private static List<Map<String, Object>> leAgrupamento(List<Map<String, Object>> desagrupado, String propriedadeAgrupadora) {
        List<Map<String, Object>> listaDesagrupado = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> map : desagrupado) {
            List<Map<String, Object>> agrupados = (List<Map<String, Object>>) map.get(propriedadeAgrupadora);
            if (agrupados != null) {
                agrupados = leAgrupamento(agrupados, propriedadeAgrupadora);
                for (Entry<String, Object> entradasAtuais : map.entrySet()) {
                    if (!entradasAtuais.getKey().equals(propriedadeAgrupadora))
                        for (Map<String, Object> map2 : agrupados) {
                            map2.put(entradasAtuais.getKey(), entradasAtuais.getValue());
                        }
                }
            } else {
                agrupados = Arrays.asList(map);
            }
            listaDesagrupado.addAll(agrupados);
        }
        return listaDesagrupado;
    }

    private static BigDecimal calcularSaldoPorConta(String conta, List<Lancamento> lancamentos, Date date) {
        BigDecimal saldoCalculado = BigDecimal.ZERO;

        // Itera sobre os lançamentos para calcular o saldo da conta
        for (Lancamento lancamento : lancamentos) {
            if (lancamento.getData().after(date))
                continue;
            if (lancamento.getOrigem().equals(conta)) {
                saldoCalculado = saldoCalculado.subtract(lancamento.getValor()); // Subtrai o valor do lançamento (saída de dinheiro)
            }
            if ((lancamento.getDestino()==null?"outro":lancamento.getDestino()).equals(conta)) {
                saldoCalculado = saldoCalculado.add(lancamento.getValor()); // Adiciona o valor do lançamento (entrada de dinheiro)
            }
        }

        return saldoCalculado;
    }
}