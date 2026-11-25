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
