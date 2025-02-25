package br.baldessar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.yaml.snakeyaml.Yaml;

public class YamlReader {
	
	
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
            }catch (Exception e) {
            	throw new RuntimeException("Erro ao ler arquivo "+file.getAbsolutePath(), e);
			}
                
        }
        
        System.out.println("Contas:");
        contas.forEach(System.out::println);
        Map<String, Conta> contasPorApelido = transformarListaContasEmMapaPorApelido(contas);
        
        System.out.println("\nSaldos:");
        saldos.forEach(System.out::println);

        System.out.println("\nLançamentos:");
        lancamentos.forEach(System.out::println);

        // Conciliação de saldos
        System.out.println("\nConciliação de saldos:");
        Conciliador.conciliarSaldos(saldos, lancamentos);
        
        System.out.println("\nBalanço:");
        Map<String, Double> calcularBalancoContas = calcularBalancoContas(contasPorApelido, lancamentos);
        for (Entry<String, Double> balanco : calcularBalancoContas.entrySet()) {
			System.out.println(balanco.getKey()+": "+balanco.getValue());
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

	private static List<Saldo> leSaldos(Map<String, Object> obj) {
		List<Map<String, Object>> saldosYaml = (List<Map<String, Object>>) obj.get("saldos");
		if(saldosYaml==null) return new ArrayList<Saldo>();
		saldosYaml = leAgrupamento(saldosYaml, "agrupados");
		List<Saldo> saldos = new ArrayList<>();
		for (Map<String, Object> saldoMap : saldosYaml) {
			Saldo saldo = new Saldo();
		    saldo.setConta((String) saldoMap.get("conta"));
	        saldo.setData((Date) saldoMap.get("data"));
	        saldo.setValor((Double) saldoMap.get("valor"));
	        saldos.add(saldo);
		}
		return saldos;
	}

	private static List<Lancamento> leLancamentos(Map<String, Object> obj) {
		// Mapear lançamentos
		List<Map<String, Object>> lancamentosYaml = (List<Map<String, Object>>) obj.get("lancamentos");
		if(lancamentosYaml==null) return new ArrayList<Lancamento>();
		lancamentosYaml = leAgrupamento(lancamentosYaml, "agrupados");
		List<Lancamento> lancamentos = new ArrayList<>();
		for (Map<String, Object> lancamentoMap : lancamentosYaml) {
		    Lancamento lancamento = new Lancamento();
            lancamento.setConciliado((String) lancamentoMap.get("conciliado"));
            lancamento.setIdentificado((String) lancamentoMap.get("identificado"));
            lancamento.setOrigem((String) lancamentoMap.get("origem"));
            lancamento.setData((Date) lancamentoMap.get("data"));
            lancamento.setValor((Double) lancamentoMap.get("valor"));
            lancamento.setDescricao((String) lancamentoMap.get("descricao"));
            lancamento.setDestino((String) lancamentoMap.get("destino"));
            lancamentos.add(lancamento);
		}
		return lancamentos;
	}
	
	 public static Map<String, Double> calcularBalancoContas(Map<String, Conta> contasPorApelido, List<Lancamento> lancamentos) {
	        Map<String, Double> balancoContas = new HashMap<>();

	        // Processa os lançamentos
	        for (Lancamento lancamento : lancamentos) {
	            String origem = lancamento.getOrigem();
	            String destino = lancamento.getDestino();
	            double valor = lancamento.getValor();

	            // Subtrai o valor da conta de origem
	            balancoContas.put(origem, balancoContas.getOrDefault(origem, 0.0) - valor);

	            // Adiciona o valor à conta de destino
	            balancoContas.put(destino, balancoContas.getOrDefault(destino, 0.0) + valor);
	        }

	        // Agrupa os saldos por hierarquia de contas
	        Map<String, Double> balancoAgrupado = new HashMap<>();
	        for (Map.Entry<String, Double> entry : balancoContas.entrySet()) {
	            String conta = entry.getKey();
	            double valor = entry.getValue();

	            // Adiciona o valor à conta principal e a todas as suas subcontas
	            String[] partesConta = contasPorApelido.get(conta).getNome().split(":");
	            String contaAtual = "";
	            for (String parte : partesConta) {
	                contaAtual = contaAtual.isEmpty() ? parte : contaAtual + ":" + parte;
	                balancoAgrupado.put(contaAtual, balancoAgrupado.getOrDefault(contaAtual, 0.0) + valor);
	            }
	        }


	        return new TreeMap<String, Double>(balancoAgrupado);
	    }

	private static List<Conta> leContas(Map<String, Object> obj) {
        @SuppressWarnings("unchecked")
		List<Map<String, Object>> contasYaml = (List<Map<String, Object>>) obj.get("contas");
        if(contasYaml==null) return new ArrayList<Conta>();
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
			if(agrupados!=null) {
				agrupados = leAgrupamento(agrupados, propriedadeAgrupadora);
				for (Entry<String, Object> entradasAtuais : map.entrySet()) {
					if(!entradasAtuais.getKey().equals(propriedadeAgrupadora))
						for (Map<String, Object> map2 : agrupados) {
							map2.put(entradasAtuais.getKey(), entradasAtuais.getValue());
						}
				}
			}else {
				agrupados = Arrays.asList(map);
			}
			listaDesagrupado.addAll(agrupados);
		}
		return listaDesagrupado;
	}
	
	public static Map<String, Conta> transformarListaContasEmMapaPorApelido(List<Conta> contas) {
        Map<String, Conta> mapaContasPorApelido = new HashMap<>();

        for (Conta conta : contas) {
            String apelido = conta.getApelido();
            if (apelido != null && !apelido.isEmpty()) {
                mapaContasPorApelido.put(apelido, conta);
            }
        }

        return mapaContasPorApelido;
    }
}