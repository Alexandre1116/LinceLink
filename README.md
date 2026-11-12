# LinceLink

<p align="center"><img src="branding/lincelink.png" alt="LinceLink" width="220"></p>

Mod cliente para partilhar um mundo Minecraft Java através do Tailcat. O anfitrião abre o mundo, copia um convite e envia-o ao convidado. O convidado cola o convite no menu multiplayer.

## Estado atual

LinceLink é o nome do projeto; Tailcat v0.7.0 continua a ser o transporte utilizado.

O projeto tem nove módulos: Forge 1.12.2 e Fabric/Forge para 1.16.5, 1.18.2, 1.20.1 e 1.21.1. Consulta [a matriz de dependências](docs/compatibilidade.md).

O transporte foi validado com dois processos reais do Tailcat v0.7.0 e um round trip TCP, incluindo execução do núcleo em Java 8. **Ainda falta validação manual com dois clientes Minecraft**, incluindo ligação entre redes diferentes. Os builds são experimentais.

Versões intermédias e posteriores a 1.21.1 não estão implementadas. Usa sempre o JAR correspondente à versão exata do Minecraft e ao loader.

## Instalação

**Forge 1.20.1, mod 0.1.2+:** basta instalar o JAR. No primeiro arranque, o mod procura Tailcat na pasta da instância e no PATH; se faltar, descarrega a versão oficial v0.7.0 para `tailcat/`. A instalação corre em segundo plano e o ecrã Tailcat mostra o progresso. Não precisas do ZIP nem de instalar Tailcat manualmente nesta versão. É necessária Internet para o primeiro download.

A instalação automática suporta Windows x64/ARM64 e Linux x64/ARM64. Em macOS ou outra arquitetura, instala Tailcat manualmente. O download é verificado com SHA-256 fixo; o executável existente não é atualizado nem substituído. Uma falha de download não impede abrir o Minecraft: o botão Abrir/Conectar volta a tentar. Voltar/Parar durante a instalação cancela a abertura do túnel; o download em segundo plano pode terminar para ficar disponível na próxima tentativa.

**Outros builds e versões anteriores:** segue a instalação manual abaixo.

Nos dois computadores:

1. Instala a versão Minecraft/loader indicada na [matriz de dependências](docs/compatibilidade.md). Fabric precisa da Fabric API indicada; Forge não usa Fabric API.
2. Copia o JAR do mod correspondente ao loader para a pasta `mods` da instância. Desde o build 0.1.6, o nome do JAR começa por `lincelink-`; remove o JAR antigo antes de instalar o novo.
3. Obtém o **Tailcat v0.7.0** oficial em https://github.com/tailscale/tailcat/releases/tag/v0.7.0 . Extrai o executável e coloca-o em `<pasta da instância>/tailcat/tailcat` (Linux/macOS) ou `<pasta da instância>/tailcat/tailcat.exe` (Windows). Em Unix, torna-o executável com `chmod +x`. Também pode estar no PATH, ou ser indicado por `-Dtailcat.executable=/caminho/absoluto/tailcat` nos argumentos Java.

O executável não está incluído nos JARs. Só Forge 1.20.1 a partir do mod 0.1.2 o descarrega automaticamente. Não precisas de conta Tailscale. LinceLink não é um projeto oficial da Tailscale.

## Utilização

**Anfitrião:** abre o mundo singleplayer, prime Esc, escolhe o botão **LinceLink**, inicia a partilha do mundo e copia o convite. Envia o convite ao teu amigo e mantém o mundo aberto.

**Convidado:** abre **Multiplayer**, escolhe o botão **LinceLink**, cola o convite (`Ctrl+V`/`⌘V`) e entra no servidor.

O convite é uma única string `tailcat-mc:1:<versão Minecraft>:<endereço tc…>:<porta>`. O mod rejeita convites de outra versão do Minecraft. Usa a mesma versão do mod nos dois lados; em mundos com outros mods, mantém também os mods de conteúdo compatíveis.

O botão de desligar do **LinceLink** termina o túnel e desliga os convidados. Sair do mundo também termina o túnel. O mundo é aberto usando o servidor LAN integrado do Minecraft: parar o túnel não fecha esse servidor LAN; para o fechar, sai do mundo. A abertura desativa a pausa do mundo. A configuração normal de autenticação do Minecraft é mantida.

O convite dá acesso ao servidor a quem o receber: partilha-o apenas com os jogadores convidados. Cada nova sessão Tailcat cria um novo endereço. Só a porta do mundo é publicada pelo Tailcat. O cliente escuta em loopback (`127.0.0.1`).

## Ping na lista Tab

No build Forge 1.20.1 a partir do mod 0.1.4, mantém Tab premido para ver o ping de cada jogador junto ao nome, em milissegundos: `Nome [42 ms]`. Os valores usam a latência comunicada pelo servidor ao Minecraft, incluindo o percurso pelo túnel quando ligado através de Tailcat. A atualização segue os dados recebidos do servidor; não é uma medição contínua independente. Um valor desconhecido aparece como `-- ms`.

O texto é verde abaixo de 150 ms, amarelo abaixo de 300 ms e vermelho a partir de 300 ms. Mantém as barras originais, os nomes personalizados, a formatação das equipas e a lista vanilla. A funcionalidade também funciona em servidores normais; só o cliente que quer ver os números precisa deste build.

## Direta ou DERP no Tab

No Forge 1.20.1 / mod 0.1.5, a lista mostra `Nome [42 ms] [Direta]` ou `Nome [90 ms] [DERP]`. **O anfitrião e os convidados devem usar uma versão com estes estados (0.1.5 ou posterior).** O estado é recolhido no anfitrião e enviado aos clientes do mod; não é deduzido do ping nem de um processo `tailcat ping` separado.

- `Direta`: o estado do túnel indica um endpoint UDP direto.
- `DERP`: o peer tem tráfego ativo confirmado no motor WireGuard, sem percurso direto exclusivo nem peer relay. Pode aparecer no arranque e durante mudanças de percurso.
- `Local`: o jogador é o dono do mundo singleplayer.
- `Sem Tailcat`: o cliente comunica que não está a usar um túnel gerido por este mod.
- `?`: ainda não existe associação/estado válido, a amostra expirou ou a outra versão não partilha estes dados. Dados desconhecidos não são apresentados como ligação direta ou DERP.

O Tailcat fornece amostras aproximadamente a cada cinco segundos. O mod distribui os estados uma vez por segundo e expira amostras com mais de 15 segundos. Cada convidado usa uma identidade Tailcat temporária por sessão; comunica apenas a chave pública ao anfitrião para a associação ao jogador. Os restantes clientes recebem apenas UUIDs e estados, sem endereços IP nem chaves. A identidade comunicada pelo cliente serve a apresentação do estado; não é uma nova forma de autenticação de jogadores.

Os pares sem o canal de estados continuam a poder ligar, mas os dados de percurso podem aparecer como `?`. Servidores normais sem este mod não fornecem os estados dos restantes jogadores. Esta funcionalidade é específica do build Forge 1.20.1 por agora.

## Idiomas

O build **Forge 1.20.1 / mod 0.1.6** acompanha automaticamente o idioma escolhido em **Opções → Idioma** do Minecraft. Usa as traduções nativas do jogo para os menus, botões, progresso, erros e estados do Tab. A auditoria dos assets oficiais verificou as 24 chaves utilizadas nos **145 idiomas** do seletor 1.20.1, incluindo os idiomas especiais e os escritos da direita para a esquerda.

Os nomes dos botões seguem agora os do próprio Minecraft: o botão do anfitrião tem a marca LinceLink e o texto nativo de abrir em LAN; o convidado tem LinceLink e o texto nativo de entrar no servidor. O botão de colar usa `Ctrl+V` ou `⌘V` em macOS; também podes usar o atalho diretamente no campo. O progresso usa os textos nativos de preparação, download, extração e conclusão.

LinceLink, Tailcat, DERP, unidades técnicas, atalhos e o código do convite mantêm-se como identificadores. Os detalhes técnicos ficam no `latest.log`; as mensagens apresentadas no ecrã são traduzidas. Este suporte de idiomas aplica-se ao build Forge 1.20.1; os builds antigos continuam com a interface anterior.

Ver [detalhes e verificação dos idiomas](docs/idiomas.md) e [relatório dos 145 idiomas](docs/idiomas.json). A auditoria confirma cobertura das chaves e argumentos, não uma inspeção visual de cada idioma dentro do jogo.

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

## Histórico: Windows e CreateProcess error=2

Nas versões atuais de Forge 1.20.1, o instalador automático trata do executável em falta. A informação seguinte refere-se ao antigo build 0.1.1.

O erro significa que o executável Tailcat não foi encontrado. Para Forge 1.20.1 existe um pacote experimental Windows x64 na release v0.1.1, com `mods/` e `tailcat/` prontos para extrair para a pasta da instância. Remove o JAR anterior antes de instalar o novo. O executável oficial v0.7.0 e a licença BSD original estão incluídos no ZIP. O JAR isolado continua a precisar de Tailcat instalado separadamente.

A versão 0.1.1 indica o caminho exato esperado quando não consegue iniciar o executável. Isto não diagnostica outros erros de arranque do Minecraft; para esses é necessário `logs/latest.log`.

### Testar o instalador (integração explícita)

Compila o núcleo e `common/src/test/java/pt/tailcatmod/transport/InstallerSmoke.java` com JDK 8 e executa `pt.tailcatmod.transport.InstallerSmoke /caminho/tailcat_0.7.0_windows_amd64.zip`. Este teste usa Internet para instalar e executar a release Linux oficial numa pasta temporária, valida extração Windows, SHA-256, rejeição de caminhos maliciosos, pedidos simultâneos, reutilização e retry. O ZIP Windows usado no teste deve ser o oficial v0.7.0.

## Marca e logo

O nome público é **LinceLink**. O logo fornecido pelo proprietário está em `branding/lincelink.png` e é incluído na lista de mods de cada build; também aparece no ecrã de ligação do Forge 1.20.1. O ficheiro original foi preservado. O ID interno `tailcatmod`, os códigos de convite e o protocolo existente são mantidos para compatibilidade. Não instalar o JAR antigo e o novo simultaneamente.

## CurseForge

Projeto existente: https://www.curseforge.com/minecraft/mc-mods/lincelink

Os ficheiros para preparar a página e os uploads estão em [curseforge/UPLOAD.md](curseforge/UPLOAD.md). Incluem descrição em inglês/português, resumo, links, changelog, dependências por loader e instruções para enviar cada JAR como Alpha.

Após compilar os nove módulos, `python3 tools/package_curseforge.py --version 0.1.7` valida os JARs e gera `release-artifacts/LinceLink-CurseForge-0.1.7.zip`. Extrair este kit e enviar os JARs individualmente; o ZIP não é um modpack. O script não acede à conta nem faz uploads para CurseForge.
