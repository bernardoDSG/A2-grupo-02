package br.unitins.otimizacao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

public class Main {

    static class Cultura {
        String nome;
        double lucro;
        double agua;
        double horas;
        double maoObra;
        double areaMaxima;
        int linhas;
    }

    public static void main(String[] args) throws Exception {
        Path arquivoCsv = Path.of("dados-grupo-02.csv");
        if (!Files.isRegularFile(arquivoCsv)) {
            arquivoCsv = Path.of("projeto", "dados-grupo-02.csv");
        }
        List<String> linhas = Files.readAllLines(arquivoCsv);

        String separador = ",";
        if (linhas.get(0).contains(";")) {
            separador = ";";
        }

        String[] cabecalho = linhas.get(0).split(separador);
        int colLucro = acharColuna(cabecalho, "lucro_hectare");
        int colAgua = acharColuna(cabecalho, "agua_m3");
        int colHoras = acharColuna(cabecalho, "horas_maquina");
        int colMaoObra = acharColuna(cabecalho, "mao_obra_dias");
        int colArea = acharColuna(cabecalho, "area_maxima");

        List<Cultura> culturas = new ArrayList<>();
        int totalLinhas = 0;

        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i).trim();
            if (linha.isEmpty()) {
                continue;
            }
            totalLinhas++;

            String[] partes = linha.split(separador);
            String nome = partes[0].trim();

            Cultura cultura = null;
            for (Cultura c : culturas) {
                if (c.nome.equals(nome)) {
                    cultura = c;
                }
            }
            if (cultura == null) {
                cultura = new Cultura();
                cultura.nome = nome;
                culturas.add(cultura);
            }

            cultura.lucro = numero(partes[colLucro]);
            cultura.agua = numero(partes[colAgua]);
            cultura.horas = numero(partes[colHoras]);
            cultura.maoObra = numero(partes[colMaoObra]);
            cultura.areaMaxima = cultura.areaMaxima + numero(partes[colArea]);
            cultura.linhas++;
        }

        System.out.println("Linhas lidas: " + totalLinhas);
        System.out.println();
        System.out.println("TABELA CONSOLIDADA");
        for (Cultura c : culturas) {
            System.out.println(c.nome + " | linhas: " + c.linhas + " | lucro: " + c.lucro
                    + " | agua: " + c.agua + " | horas: " + c.horas
                    + " | mao de obra: " + c.maoObra + " | area maxima: " + c.areaMaxima);
        }

        Cultura[] ordem = {
            acharCultura(culturas, "feijao"),
            acharCultura(culturas, "melancia"),
            acharCultura(culturas, "milho"),
            acharCultura(culturas, "pimentao"),
            acharCultura(culturas, "tomate")
        };

        ExpressionsBasedModel modelo = new ExpressionsBasedModel();

        Variable[] x = new Variable[5];
        for (int i = 0; i < 5; i++) {
            x[i] = modelo.addVariable("x" + (i + 1));
            x[i].lower(0);
            x[i].upper(ordem[i].areaMaxima);
            x[i].weight(ordem[i].lucro);
        }
        x[0].lower(60);

        Expression area = modelo.addExpression("R1").upper(420);
        Expression agua = modelo.addExpression("R2").upper(1180000);
        Expression maquinas = modelo.addExpression("R3").upper(2650);
        Expression maoObra = modelo.addExpression("R4").upper(1750);

        for (int i = 0; i < 5; i++) {
            area.set(x[i], 1);
            agua.set(x[i], ordem[i].agua);
            maquinas.set(x[i], ordem[i].horas);
            maoObra.set(x[i], ordem[i].maoObra);
        }

        Expression pimentao = modelo.addExpression("R11").upper(0);
        pimentao.set(x[3], 1);
        pimentao.set(x[4], -1);

        Expression giro = modelo.addExpression("R12").lower(0);
        giro.set(x[0], -0.30);
        giro.set(x[1], 0.70);
        giro.set(x[2], 0.70);
        giro.set(x[3], -0.30);
        giro.set(x[4], -0.30);

        Optimisation.Result resultado = modelo.maximise();

        System.out.println();
        System.out.println("RESULTADO");
        System.out.println("Estado do solver: " + resultado.getState());
        System.out.printf("Z = R$ %.2f%n", resultado.getValue());

        double areaTotal = 0;
        for (int i = 0; i < 5; i++) {
            double valor = resultado.doubleValue(i);
            areaTotal = areaTotal + valor;
            System.out.printf("x%d (%s) = %.2f ha%n", i + 1, ordem[i].nome, valor);
        }
        System.out.printf("Area total plantada = %.2f ha%n", areaTotal);
    }

    static int acharColuna(String[] cabecalho, String nome) {
        for (int i = 0; i < cabecalho.length; i++) {
            if (cabecalho[i].trim().equalsIgnoreCase(nome)) {
                return i;
            }
        }
        throw new RuntimeException("Coluna nao encontrada: " + nome);
    }

    static Cultura acharCultura(List<Cultura> culturas, String nome) {
        for (Cultura c : culturas) {
            String nomeSimples = c.nome.toLowerCase().replace("ã", "a");
            if (nomeSimples.contains(nome)) {
                return c;
            }
        }
        throw new RuntimeException("Cultura nao encontrada: " + nome);
    }

    static double numero(String texto) {
        return Double.parseDouble(texto.trim().replace(",", "."));
    }
}
