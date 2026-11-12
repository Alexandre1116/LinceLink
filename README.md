# Tailcat Multiplayer

Mod cliente para partilhar um mundo Minecraft Java através do Tailcat. O anfitrião abre o mundo, copia um convite e envia-o ao convidado. O convidado cola o convite no menu multiplayer.

## Estado atual

O projeto tem nove módulos: Forge 1.12.2 e Fabric/Forge para 1.16.5, 1.18.2, 1.20.1 e 1.21.1. Consulta [a matriz de dependências](docs/compatibilidade.md).

O transporte foi validado com dois processos reais do Tailcat v0.7.0 e um round trip TCP, incluindo execução do núcleo em Java 8. **Ainda falta validação manual com dois clientes Minecraft**, incluindo ligação entre redes diferentes. Os builds são experimentais.

Versões intermédias e posteriores a 1.21.1 não estão implementadas. Usa sempre o JAR correspondente à versão exata do Minecraft e ao loader.

## Instalação

**Forge 1.20.1, mod 0.1.2+:** basta instalar o JAR. No primeiro arranque, o mod procura Tailcat na pasta da instância e no PATH; se faltar, descarrega a versão oficial v0.7.0 para `tailcat/`. A instalação corre em segundo plano e o ecrã Tailcat mostra o progresso. Não precisas do ZIP nem de instalar Tailcat manualmente nesta versão. É necessária Internet para o primeiro download.

A instalação automática suporta Windows x64/ARM64 e Linux x64/ARM64. Em macOS ou outra arquitetura, instala Tailcat manualmente. O download é verificado com SHA-256 fixo; o executável existente não é atualizado nem substituído. Uma falha de download não impede abrir o Minecraft: o botão Abrir/Conectar volta a tentar. Voltar/Parar durante a instalação cancela a abertura do túnel; o download em segundo plano pode terminar para ficar disponível na próxima tentativa.

**Outros builds e versões anteriores:** segue a instalação manual abaixo.

Nos dois computadores:

1. Instala a versão Minecraft/loader indicada na [matriz de dependências](docs/compatibilidade.md). Fabric precisa da Fabric API indicada; Forge não usa Fabric API.
2. Copia o JAR do mod correspondente ao loader para a pasta `mods` da instância.
3. Obtém o **Tailcat v0.7.0** oficial em https://github.com/tailscale/tailcat/releases/tag/v0.7.0 . Extrai o executável e coloca-o em `<pasta da instância>/tailcat/tailcat` (Linux/macOS) ou `<pasta da instância>/tailcat/tailcat.exe` (Windows). Em Unix, torna-o executável com `chmod +x`. Também pode estar no PATH, ou ser indicado por `-Dtailcat.executable=/caminho/absoluto/tailcat` nos argumentos Java.

O executável não está incluído nos JARs. Só Forge 1.20.1 a partir do mod 0.1.2 o descarrega automaticamente. Não precisas de conta Tailscale. O projeto não é oficial da Tailscale.

## Utilização

**Anfitrião:** abre o mundo singleplayer, prime Esc e escolhe **Abrir para Tailcat** → **Abrir mundo** → **Copiar convite**. Envia o convite ao teu amigo e mantém o mundo aberto.

**Convidado:** abre **Multiplayer** → **Conectar com Tailcat** → **Colar convite** → **Conectar**.

O convite é uma única string `tailcat-mc:1:<versão Minecraft>:<endereço tc…>:<porta>`. O mod rejeita convites de outra versão do Minecraft. Usa a mesma versão do mod nos dois lados; em mundos com outros mods, mantém também os mods de conteúdo compatíveis.

**Parar Tailcat** termina o túnel e desliga os convidados. Sair do mundo também termina o túnel. O mundo é aberto usando o servidor LAN integrado do Minecraft: parar o túnel não fecha esse servidor LAN; para o fechar, sai do mundo. A abertura desativa a pausa do mundo. A configuração normal de autenticação do Minecraft é mantida.

O convite dá acesso ao servidor a quem o receber: partilha-o apenas com os jogadores convidados. Cada nova sessão Tailcat cria um novo endereço. Só a porta do mundo é publicada pelo Tailcat. O cliente escuta em loopback (`127.0.0.1`).

## Ping na lista Tab

No build Forge 1.20.1 a partir do mod 0.1.4, mantém Tab premido para ver o ping de cada jogador junto ao nome, em milissegundos: `Nome [42 ms]`. Os valores usam a latência comunicada pelo servidor ao Minecraft, incluindo o percurso pelo túnel quando ligado através de Tailcat. A atualização segue os dados recebidos do servidor; não é uma medição contínua independente. Um valor desconhecido aparece como `-- ms`.

O texto é verde abaixo de 150 ms, amarelo abaixo de 300 ms e vermelho a partir de 300 ms. Mantém as barras originais, os nomes personalizados, a formatação das equipas e a lista vanilla. A funcionalidade também funciona em servidores normais; só o cliente que quer ver os números precisa deste build.

## Compilar

Os builds modernos usam Gradle 8.10.2 e JDK 21. Forge 1.16.5 também precisa de JDK 8 instalado; Forge 1.18.2/1.20.1 precisam de JDK 17. O Gradle deteta as instalações normais e as variáveis `JAVA_HOME_8_X64`, `JAVA_HOME_17_X64` e `JAVA_HOME_21_X64`. Também podes indicar os caminhos com `-Porg.gradle.java.installations.paths=/caminho/jdk8,/caminho/jdk17`.

Forge 1.12.2 usa **JDK 8 e Gradle 4.10.3**, com wrapper próprio. O Gradle Wrapper está incluído.

```sh
./gradlew build
./gradlew -p forge-1.21.1 build
./gradlew -p forge-1.20.1 build
./gradlew -p forge-1.18.2 build
./gradlew -p forge-1.16.5 build
# Com JAVA_HOME a apontar para JDK 8:
(cd forge-1.12.2 && ./gradlew build)
```

Os JARs são produzidos em `<loader>-<versão>/build/libs`. Nas versões Forge anteriores a 1.21.1, o build também executa `reobfJar`, necessário para o jogo distribuído. O núcleo em `common/` é partilhado; as interfaces e eventos são específicos de cada loader.

Teste explícito do transporte (requer Internet e Tailcat):

```sh
mkdir -p /tmp/tailcat-smoke
javac -d /tmp/tailcat-smoke common/src/main/java/pt/tailcatmod/transport/*.java common/src/test/java/pt/tailcatmod/transport/TransportSmoke.java
java -Dtailcat.executable=/caminho/absoluto/tailcat -cp /tmp/tailcat-smoke pt.tailcatmod.transport.TransportSmoke
```

Se Tailcat não iniciar, verifica o caminho do executável e se estás a usar a versão indicada. O arranque tem um limite de 60 segundos. Os relays públicos e a travessia NAT dependem da disponibilidade da rede; ligações por relay podem ter mais latência.

## Teste manual pendente

Com duas contas Minecraft, abrir um mundo, copiar/colar o convite, entrar, mover e alterar blocos. Repetir com redes distintas, confirmar desconexão ao parar o túnel/sair do mundo e repetir uma segunda sessão. Verificar também convite inválido, versão diferente e executável ausente.

## Windows: erro CreateProcess error=2

Significa que o executável Tailcat não foi encontrado. Para Forge 1.20.1 existe um pacote experimental Windows x64 na release v0.1.1, com `mods/` e `tailcat/` prontos para extrair para a pasta da instância. Remove o JAR anterior antes de instalar o novo. O executável oficial v0.7.0 e a licença BSD original estão incluídos no ZIP. O JAR isolado continua a precisar de Tailcat instalado separadamente.

A versão 0.1.1 indica o caminho exato esperado quando não consegue iniciar o executável. Isto não diagnostica outros erros de arranque do Minecraft; para esses é necessário `logs/latest.log`.

### Testar o instalador (integração explícita)

Compila o núcleo e `common/src/test/java/pt/tailcatmod/transport/InstallerSmoke.java` com JDK 8 e executa `pt.tailcatmod.transport.InstallerSmoke /caminho/tailcat_0.7.0_windows_amd64.zip`. Este teste usa Internet para instalar e executar a release Linux oficial numa pasta temporária, valida extração Windows, SHA-256, rejeição de caminhos maliciosos, pedidos simultâneos, reutilização e retry. O ZIP Windows usado no teste deve ser o oficial v0.7.0.
