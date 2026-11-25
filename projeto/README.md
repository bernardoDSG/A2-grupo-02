COMO EXECUTAR

1. Descompacte este zip em qualquer lugar.
2. Abra o terminal dentro da pasta `projeto` (a que tem o `pom.xml`).
   No VS Code, abra a pasta do projeto e execute `Main.java` pelo botao Run,
   ou abra um terminal nessa pasta e rode os comandos abaixo.
3. Digite:

      mvn compile exec:java

Precisa ter Java 25 ou mais novo e o Maven instalados.

O `dados-grupo-02.csv` ja esta incluido, montado a partir da tabela consolidada 2.2
do relatorio do grupo.

Ao executar, o ojAlgo pode exibir um aviso dizendo que nenhum perfil de hardware
predefinido corresponde ao ambiente e que um perfil padrao foi escolhido. Essa
mensagem e esperada e informativa; nao e um erro, e o programa pode continuar.

Validacao dos dados do CSV:

Antes de montar o modelo, o programa verifica o cabecalho, a quantidade de colunas,
os valores numericos e se lucro, agua, horas de maquina, mao de obra e area maxima
sao iguais em todas as linhas da mesma cultura. O total de area maxima consolidado
e calculado somando a area de cada linha.
Se encontrar dados inconsistentes, o programa para e informa a linha, a cultura
e a coluna com divergencia. Por exemplo:

   Linha 3: a coluna 'lucro_hectare' da cultura 'Feijao Corda' difere da linha 2 (2800.0 != 4000.0).
   Linha 3: a coluna 'area_maxima' da cultura 'Feijao Corda' difere da linha 2 (1.0 != 2.0).

Tabela consolidada esperada:


TABELA CONSOLIDADA
Milho Verde | linhas: 160 | lucro: 3200.0 | agua: 2600.0 | horas: 6.0 | mao de obra: 3.2 | area maxima: 160.0
Tomate Industrial | linhas: 90 | lucro: 5400.0 | agua: 4200.0 | horas: 9.0 | mao de obra: 6.0 | area maxima: 90.0
Melancia | linhas: 120 | lucro: 4100.0 | agua: 3100.0 | horas: 5.5 | mao de obra: 4.1 | area maxima: 120.0
Feijao Corda | linhas: 150 | lucro: 2800.0 | agua: 1800.0 | horas: 4.5 | mao de obra: 2.7 | area maxima: 150.0
Pimentao | linhas: 70 | lucro: 6200.0 | agua: 4600.0 | horas: 10.5 | mao de obra: 6.8 | area maxima: 70.0


Valores que devem aparecer:

   Estado do solver: OPTIMAL
   Z = R$ 1621181,82
   x1 (Feijao Corda) = 150,00 ha
   x2 (Melancia) = 120,00 ha
   x3 (Milho Verde) = 0,00 ha
   x4 (Pimentao) = 61,14 ha
   x5 (Tomate Industrial) = 61,14 ha
   Area total plantada = 392,27 ha

Se o `dados-grupo-02.csv` original de 590 linhas aparecer, basta substituir o arquivo
com o mesmo nome. O Z e os valores nao mudam; so a contagem "Linhas lidas".

Erro "there is no POM in this directory": voce esta na pasta errada.
Entre na pasta que tem o pom.xml.
