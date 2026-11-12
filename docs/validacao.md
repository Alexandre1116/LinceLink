# Validação 0.1.0

## Verificações automatizadas locais

- Build Fabric 1.16.5, 1.18.2, 1.20.1 e 1.21.1 com Loom 1.8.13.
- Build Forge 1.12.2 com ForgeGradle 3.0.197 e Gradle 4.10.3, incluindo reobfJar.
- Build Forge 1.16.5, 1.18.2, 1.20.1 e 1.21.1 com ForgeGradle 6.0.54.
- ReobfJar aplicado nos builds Forge 1.16.5, 1.18.2 e 1.20.1. O runtime 1.21.1 usa os nomes oficiais.
- Núcleo e teste compilados/executados com JDK 8: validação de convites e round trip TCP real através de dois processos Tailcat v0.7.0. Não é apenas um teste com mocks.

## Limites

A compilação não prova que os botões aparecem corretamente nem que o login Minecraft funciona. Não foi executado um cliente gráfico Minecraft nesta máquina. Não foi testada uma ligação entre redes distintas. Os JARs são experimentais até concluir estes testes.

## Teste manual por versão/loader

1. Instalar dois clientes com contas Minecraft distintas, as mesmas versões do jogo/mod e as dependências da matriz de compatibilidade.
2. Abrir um mundo novo no anfitrião, Esc → Abrir para Tailcat → Abrir mundo.
3. Copiar o convite; no outro cliente, Multiplayer → Conectar com Tailcat → Colar convite → Conectar.
4. Confirmar entrada, movimento, chat, colocação e destruição de blocos, sincronizados nos dois lados.
5. Confirmar que Parar Tailcat desliga o convidado. Reabrir o túnel e voltar a entrar.
6. Sair do mundo anfitrião; confirmar desligamento e ausência de processo Tailcat órfão.
7. Repetir com computadores em redes distintas e, se possível, uma rede que obrigue a relay.
8. Verificar convite inválido, versão diferente, executável ausente e cancelamento de ligação.
9. Redimensionar a janela e confirmar botões visíveis; repetir em Windows, Linux e macOS com executáveis correspondentes.

Registar versão/loader, sistema operativo, resultado e logs sem o convite secreto antes de promover um build a estável.

## Forge 1.20.1 / mod 0.1.2

Instalador automático em background no primeiro tick do cliente. SHA-256 fixo da release oficial v0.7.0; sem executar scripts de instalação. Build Forge e reobfJar verificados. Teste de integração do instalador em JDK 8: download Linux real, validação do hash, extração, permissão de execução, execução `--version`, reutilização, retry depois de falha, pedidos simultâneos; extração e hash do ZIP Windows oficial; rejeição de zip traversal e hash errado. Não foi executado o binário Windows nem o cliente gráfico Minecraft.

## Forge 1.20.1 / mod 0.1.3

Adicionado pack.mcmeta na raiz do JAR, com pack_format 15 conforme o MDK oficial Forge 1.20.1. Corrige a ausência de ResourcePackInfo que causava o aviso ao carregar o mod. Verificado build/reobfJar e conteúdo do JAR; o arranque no cliente gráfico continua por validar. O utilizador confirmou abertura do túnel e partilha do convite em 0.1.2; isso não confirma ainda a entrada do convidado.
