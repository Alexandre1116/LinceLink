# LinceLink

**Share a Minecraft Java singleplayer world with friends using one invite.**

LinceLink adds a host screen to the pause menu and an invite screen to
Multiplayer. The host opens a world, copies an invitation and sends it to a
friend. The friend pastes the invitation to connect. Both players need the
same Minecraft version, loader and LinceLink file. No Tailscale account or
manual router configuration is required.

## Features

- Uses the separate Tailcat v0.7.0 transport for encrypted connectivity.
- Automatically installs Tailcat on Windows and Linux x64/ARM64 when needed.
- Forge 1.20.1 adds `/lincelink start`, `invite`, `status` and `stop`, plus
  player latency and Direct/DERP status in the Tab list.
- Forge 1.20.1 uses Minecraft's native translations for all 145 languages.
  Other builds include English and Portuguese UI text; other languages fall
  back to English.
- Dedicated-server commands are experimental and available on Forge 1.20.1.

## Supported versions

- **Forge:** 1.12.2, 1.16.5, 1.18.2, 1.20.1, 1.21.1.
- **Fabric:** 1.16.5, 1.18.2, 1.20.1, 1.21.1. Fabric API is required.
- **NeoForge:** 1.21.1.

Choose the file that exactly matches your Minecraft version and loader. There is
no universal JAR. See the [compatibility table](https://github.com/Alexandre1116/LinceLink/blob/main/docs/compatibilidade.md)
for dependency versions and Java requirements.

## Install and play

1. Install the matching Minecraft loader and LinceLink file on both computers.
   Fabric players also need Fabric API.
2. Open a singleplayer world, pause, choose **LinceLink**, start sharing and
   send the copied invitation to your friend.
3. The guest opens **Multiplayer → LinceLink**, pastes the invitation and
   connects. Keep the host's world open.

The invitation grants access to the world. Share it only with people you
intend to invite. LinceLink exposes only the Minecraft world connection, not
the rest of the host's network. The game and world must remain open while
playing.

## Validation

This is the first normal release, not an Alpha or Beta file. Focused in-game
checks covered selected builds; see the [validation notes](https://github.com/Alexandre1116/LinceLink/blob/main/docs/validation-working.md)
for tested configurations and paths that remain unverified. A connection using
two Minecraft clients was not part of this release's testing; the project
owner plans to test it with friends.

Tailcat is a separate project by Tailscale and contributors. LinceLink is not
an official Tailscale or Mojang product. Licensed under MIT.

---

## Português

**Partilha um mundo singleplayer de Minecraft Java com os teus amigos usando
um convite.**

O LinceLink adiciona um ecrã de anfitrião ao menu de pausa e um ecrã para
convites ao Multiplayer. O anfitrião abre um mundo, copia o convite e envia-o
a um amigo. O amigo cola-o para ligar. Ambos precisam da mesma versão do
Minecraft, loader e ficheiro LinceLink. Não é necessária uma conta Tailscale
nem configurar o router.

### Funcionalidades

- Usa o Tailcat v0.7.0, um transporte separado com ligação encriptada.
- Instala automaticamente o Tailcat em Windows e Linux x64/ARM64 se for
  necessário.
- Forge 1.20.1 inclui `/lincelink start`, `invite`, `status` e `stop`, ping dos
  jogadores e estado Direto/DERP na lista Tab.
- Forge 1.20.1 usa as traduções nativas do Minecraft para as 145 línguas. Os
  restantes builds incluem interface em inglês e português; outros idiomas
  usam inglês.
- Os comandos para servidores dedicados são experimentais e estão disponíveis
  em Forge 1.20.1.

### Versões suportadas

- **Forge:** 1.12.2, 1.16.5, 1.18.2, 1.20.1 e 1.21.1.
- **Fabric:** 1.16.5, 1.18.2, 1.20.1 e 1.21.1. Requer Fabric API.
- **NeoForge:** 1.21.1.

Escolhe o ficheiro correspondente à versão exata do Minecraft e ao loader.
Não existe um JAR universal.

### Instalação e utilização

1. Instala o loader correto e o ficheiro LinceLink nos dois computadores. No
   Fabric, instala também Fabric API.
2. Abre um mundo singleplayer, pausa, escolhe **LinceLink**, inicia a partilha
   e envia o convite copiado ao teu amigo.
3. O convidado abre **Multiplayer → LinceLink**, cola o convite e liga. Mantém
   o mundo do anfitrião aberto.

O convite dá acesso ao mundo; partilha-o apenas com quem queres convidar. O
LinceLink não partilha o resto da rede do anfitrião. O jogo e o mundo têm de
ficar abertos durante a sessão.

Esta é a primeira versão normal, não Alpha nem Beta. Foram feitos testes
focados dentro do jogo em builds selecionados; consulta as
[notas de validação](https://github.com/Alexandre1116/LinceLink/blob/main/docs/validation-working.md)
para ver as configurações testadas e os percursos por verificar. A ligação
com dois clientes Minecraft não fez parte dos testes desta versão; o
proprietário planeia fazê-la com amigos.

O Tailcat é um projeto separado da Tailscale e dos seus colaboradores. O
LinceLink não é um produto oficial da Tailscale ou da Mojang. Licença MIT.

---

[Source code](https://github.com/Alexandre1116/LinceLink) ·
[Report an issue](https://github.com/Alexandre1116/LinceLink/issues) ·
[Buy Me a Coffee](https://buymeacoffee.com/alexandre1116)
