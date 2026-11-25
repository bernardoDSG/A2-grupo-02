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
        double areaPorLinha;
        int linhas;
        int primeiraLinha;
    }

    public static void main(String[] args) throws Exception {
        Path arquivoCsv = Path.of("dados-grupo-02.csv");
        if (!Files.isRegularFile(arquivoCsv)) {
            arquivoCsv = Path.of("projeto", "dados-grupo-02.csv");
        }
        List<String> linhas = Files.readAllLines(arquivoCsv);
        List<Cultura> culturas = lerCulturas(linhas);
        int totalLinhas = 0;
        for (Cultura cultura : culturas) {
            totalLinhas += cultura.linhas;
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

    static List<Cultura> lerCulturas(List<String> linhas) {
        if (linhas.isEmpty() || linhas.get(0).trim().isEmpty()) {
            throw new IllegalArgumentException("O arquivo CSV esta vazio ou sem cabecalho.");
        }

        String separador = linhas.get(0).contains(";") ? ";" : ",";
        String[] cabecalho = linhas.get(0).split(separador, -1);
        int colCultura = acharColuna(cabecalho, "cultura");
        int colLucro = acharColuna(cabecalho, "lucro_hectare");
        int colAgua = acharColuna(cabecalho, "agua_m3");
        int colHoras = acharColuna(cabecalho, "horas_maquina");
        int colMaoObra = acharColuna(cabecalho, "mao_obra_dias");
        int colArea = acharColuna(cabecalho, "area_maxima");

        List<Cultura> culturas = new ArrayList<>();
        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i).trim();
            if (linha.isEmpty()) {
                continue;
            }

            int numeroLinha = i + 1;
            String[] partes = linha.split(separador, -1);
            if (partes.length != cabecalho.length) {
                throw new IllegalArgumentException("Linha " + numeroLinha + ": esperado "
                        + cabecalho.length + " colunas, mas foram encontradas " + partes.length + ".");
            }

            String nome = partes[colCultura].trim();
            if (nome.isEmpty()) {
                throw new IllegalArgumentException("Linha " + numeroLinha + ": a cultura esta vazia.");
            }

            double lucro = numero(partes[colLucro], numeroLinha, "lucro_hectare");
            double agua = numero(partes[colAgua], numeroLinha, "agua_m3");
            double horas = numero(partes[colHoras], numeroLinha, "horas_maquina");
            double maoObra = numero(partes[colMaoObra], numeroLinha, "mao_obra_dias");
            double areaMaxima = numero(partes[colArea], numeroLinha, "area_maxima");

            validarNaoNegativo(agua, numeroLinha, "agua_m3");
            validarNaoNegativo(horas, numeroLinha, "horas_maquina");
            validarNaoNegativo(maoObra, numeroLinha, "mao_obra_dias");
            validarNaoNegativo(areaMaxima, numeroLinha, "area_maxima");

            Cultura cultura = null;
            for (Cultura existente : culturas) {
                if (existente.nome.equals(nome)) {
                    cultura = existente;
                    break;
                }
            }

            if (cultura == null) {
                cultura = new Cultura();
                cultura.nome = nome;
                cultura.lucro = lucro;
                cultura.agua = agua;
                cultura.horas = horas;
                cultura.maoObra = maoObra;
                cultura.areaPorLinha = areaMaxima;
                cultura.primeiraLinha = numeroLinha;
                culturas.add(cultura);
            } else {
                validarConsistencia(cultura, "lucro_hectare", cultura.lucro, lucro, numeroLinha);
                validarConsistencia(cultura, "agua_m3", cultura.agua, agua, numeroLinha);
                validarConsistencia(cultura, "horas_maquina", cultura.horas, horas, numeroLinha);
                validarConsistencia(cultura, "mao_obra_dias", cultura.maoObra, maoObra, numeroLinha);
                validarConsistencia(cultura, "area_maxima", cultura.areaPorLinha, areaMaxima, numeroLinha);
            }

            double areaAcumulada = cultura.areaMaxima + areaMaxima;
            if (!Double.isFinite(areaAcumulada)) {
                throw new IllegalArgumentException("Linha " + numeroLinha
                        + ": a soma de 'area_maxima' da cultura '" + nome + "' nao e valida.");
            }
            cultura.areaMaxima = areaAcumulada;
            cultura.linhas++;
        }

        if (culturas.isEmpty()) {
            throw new IllegalArgumentException("O arquivo CSV nao possui linhas de dados.");
        }
        return culturas;
    }

    static int acharColuna(String[] cabecalho, String nome) {
        for (int i = 0; i < cabecalho.length; i++) {
            if (cabecalho[i].trim().equalsIgnoreCase(nome)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Coluna obrigatoria nao encontrada: " + nome);
    }

    static void validarConsistencia(Cultura cultura, String coluna, double valorEsperado,
            double valorAtual, int numeroLinha) {
        if (Double.compare(valorEsperado, valorAtual) != 0) {
            throw new IllegalArgumentException("Linha " + numeroLinha + ": a coluna '" + coluna
                    + "' da cultura '" + cultura.nome + "' difere da linha " + cultura.primeiraLinha
                    + " (" + valorEsperado + " != " + valorAtual + ").");
        }
    }

    static void validarNaoNegativo(double valor, int numeroLinha, String coluna) {
        if (valor < 0) {
            throw new IllegalArgumentException("Linha " + numeroLinha + ": a coluna '" + coluna
                    + "' nao pode ser negativa (" + valor + ").");
        }
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

    static double numero(String texto, int numeroLinha, String coluna) {
        double valor;
        try {
            valor = Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Linha " + numeroLinha + ": valor numerico invalido na coluna '"
                    + coluna + "': '" + texto + "'.", e);
        }
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("Linha " + numeroLinha + ": valor numerico nao finito na coluna '"
                    + coluna + "': '" + texto + "'.");
        }
        return valor;
    }
}
