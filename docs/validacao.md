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

## Forge 1.20.1 / mod 0.1.4

Ping numérico na lista Tab, baseado em PlayerInfo.getLatency(). Usa os eventos de renderização da lista vanilla para acrescentar temporariamente o valor ao nome e permitir que o Minecraft calcule a largura das colunas. Restaura os nomes no final da lista/HUD ou ao desligar; preserva alterações de outros listeners. Não envia pacotes nem faz medições externas.

Build/reobfJar concluídos. JAR verificado: classes do overlay e instalador, versão e pack.mcmeta. Falta teste visual no cliente Minecraft: dois jogadores, nomes/equipas personalizados, espectadores, scoreboard, reconexão e lista com várias colunas. Confirmar que os valores não se acumulam nos nomes e desaparecem fora do Tab.

## Forge 1.20.1 / mod 0.1.5

Estado Direta/DERP ao lado do ping, por jogador. Usa TAILCAT_STATUS_LOOP=1 do servidor Tailcat v0.7.0 e uma identidade temporária por convidado. Canal Forge opcional: cliente comunica chave pública; anfitrião associa ao UUID do remetente e distribui apenas UUID/enum. Estado indisponível ou expirado aparece como ?. O anfitrião aparece como Local.

Validação executada: cinco testes de classificação de estados, incluindo múltiplos peers, endpoint direto com relay configurado, relay sem código regional, dados desconhecidos/inválidos, expiração, remoção e distinção de peer relay. Integração com dois processos Tailcat reais e stream TCP: rota DIRECT e rota DERP com TS_DEBUG_ALWAYS_USE_DERP=true; ambos associaram corretamente a chave do convidado ao estado do anfitrião. Smoke TCP original passou em Java 8 depois da criação de identidades temporárias. Build/reobfJar e testes Gradle passaram.

O teste forçado revelou que o mapa DERP usado pode omitir códigos regionais: Relay fica vazio mesmo em ligação DERP real. A classificação exige tráfego ativo confirmado no motor/magicsock e ausência de endpoint direto/peer relay; não usa o campo Relay isoladamente. Baseado em populatePeerStatus de magicsock na dependência Tailscale fixada pelo Tailcat v0.7.0.

Pendente no cliente Minecraft: canal Forge entre dois computadores, apresentação no Tab, associação de vários convidados, mudança de DERP para direta, atualização de estado, saída/reentrada, compatibilidade com pares antigos. O transporte real foi testado, mas a interface e a negociação do canal dentro do jogo ainda não.

Para repetir a integração explicitamente, compilar Tunnel/Installer, PeerRoutes e src/test/java/pt/tailcatmod/RouteSmoke.java com JDK 17 e Gson do classpath Minecraft. Executar `pt.tailcatmod.RouteSmoke DIRECT` com `-Dtailcat.executable=/caminho/tailcat`. Repetir `pt.tailcatmod.RouteSmoke DERP` com a variável de ambiente `TS_DEBUG_ALWAYS_USE_DERP=true`. Não são testes automáticos em cada CI porque usam Internet e relays públicos.

## Forge 1.20.1 / mod 0.1.6

Interface baseada em componentes traduzíveis nativos do Minecraft. Progresso do instalador passou a ser estruturado (fase/detalhe/falha), sem frases fixas em português; erros do transporte e convites expõem metadados de apresentação, mantendo os diagnósticos separados dos textos do ecrã. O campo de convite/código não é traduzido. Painel alargado e adaptado à largura da janela para acomodar rótulos maiores.

Verificações executadas: auditoria dos assets oficiais 1.20.1, 24 chaves presentes nos 145 idiomas do seletor, sem argumentos extra; oito testes JUnit (cinco de percursos e três de internacionalização), incluindo mudança de idioma em componentes já criados, texto árabe lógico, caminhos Unicode/percentagens e versão Minecraft em erros traduzidos. Build/reobfJar passou. Smoke de instalação real (Linux e extração Windows) e TCP com identidades temporárias passou em Java 8.

Não foi feita inspeção gráfica dos 145 idiomas. Continua pendente a verificação visual de RTL, fontes, nomes compridos, diferentes escalas de GUI, painel redimensionado e estados do Tab dentro do Minecraft.

## Marca LinceLink / 0.1.6

Nomes públicos, títulos/botões e nomes dos JARs mudados para LinceLink nos nove módulos. Logo original fornecido pelo proprietário: PNG RGBA 1254×1254 em branding/lincelink.png. Incluído como ícone na metadata Fabric/Forge e como textura no ecrã Forge 1.20.1. Mantidos os identificadores técnicos e protocolos para compatibilidade. Builds das outras versões incluem a marca e o logo; instalação automática, ping, percurso e internacionalização continuam específicos do Forge 1.20.1.
