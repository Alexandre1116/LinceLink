# LinceLink

**Open your singleplayer world to friends with an invite code.**

LinceLink adds a world-sharing screen to the pause menu and an invite connection screen to Multiplayer. The host opens a world, copies one code and sends it to a friend. The friend pastes the code to connect to that world.

Connections use Tailcat's encrypted transport. Tailcat attempts a direct connection between players and uses a DERP relay when a direct path is unavailable. No Tailscale account or manual router port forwarding is needed. The host must keep the game and world open.

## Getting started

1. Install the LinceLink file matching your exact Minecraft version and loader on both computers. Fabric builds also require Fabric API.
2. Host: open a singleplayer world, press Esc and select **LinceLink**. Start sharing the world and copy the invitation.
3. Guest: open Multiplayer, select **LinceLink**, paste the invitation and join.
4. Keep the host's world open. Use the LinceLink disconnect button or leave the world to stop the tunnel.

Use matching Minecraft and LinceLink versions. Worlds using content mods also need compatible content mods on both computers. Install LinceLink on the players' clients; it is designed for the integrated singleplayer server, not installation on a dedicated server.

## Available versions

- **Forge:** 1.12.2, 1.16.5, 1.18.2, 1.20.1 and 1.21.1.
- **Fabric:** 1.16.5, 1.18.2, 1.20.1 and 1.21.1.

Each download is for one exact game version and one loader. Select the appropriate file under Files.

## Extra features in Forge 1.20.1

- Automatically installs Tailcat v0.7.0 on first launch if it is missing, using the official upstream release and a pinned SHA-256 check.
- Shows each player's Minecraft latency in milliseconds in the Tab list.
- Shows the host-reported **Direct / DERP** route beside latency. Both host and guests need a build with route reporting. Local and unknown states are shown separately.
- Uses Minecraft's native interface translations for all 145 languages in the 1.20.1 language selector.

These extra features are currently specific to the **Forge 1.20.1** build. Other downloads provide basic world sharing and invite connection, and require a manually installed Tailcat executable.

## Tailcat component and operating systems

LinceLink launches the separate **Tailcat v0.7.0** executable for the tunnel. The executable is not bundled in these JARs. Forge 1.20.1 downloads it from the official Tailscale Tailcat GitHub release, when missing, into the instance's `tailcat` directory. Automatic installation supports Windows and Linux on x64 and ARM64. Internet access is required for this initial installation and for connection bootstrap/relay services.

For other builds or operating systems, install the matching Tailcat v0.7.0 executable in the instance's `tailcat` folder, make it executable on Unix, or place it on PATH. The project documentation explains manual installation and configuration. An existing installation is reused rather than silently updated.

The invitation grants access to the world. Share it only with the players you want to invite. Only the world's game port is exposed by this mod; it does not share the rest of your network. Relay availability and connection quality depend on the network and upstream services.

## Alpha status

These downloads are **alpha builds**. Builds, automated tests and real TCP tunnel tests have passed, including direct and forced-DERP transport tests. Complete testing of all game versions, operating systems, graphical layouts and two-client route-reporting sessions remains pending. Report problems with your Minecraft version, loader, LinceLink version and `latest.log`; remove invitation codes before sharing logs or screenshots.

LinceLink is open source under MIT. Its logo was provided by the project owner. Tailcat is a separate project by Tailscale Inc. and contributors, licensed under BSD-3-Clause. LinceLink is not an official Tailscale or Mojang product.

## Português

O LinceLink permite abrir um mundo singleplayer para amigos através de um código de convite. O anfitrião abre o mundo, escolhe **LinceLink** no menu de pausa e envia o código; o convidado cola-o no ecrã LinceLink do Multiplayer.

Instala o JAR correspondente à versão exata do Minecraft e ao loader nos dois computadores. Os builds Fabric precisam também de Fabric API. O anfitrião deve manter o mundo e o jogo abertos.

No **Forge 1.20.1**, inclui instalação automática do Tailcat, ping no Tab, estado de ligação direta/DERP e interface nos 145 idiomas do Minecraft 1.20.1. Os restantes builds oferecem a partilha base e precisam do executável Tailcat instalado manualmente. A instalação automática suporta Windows/Linux x64 e ARM64.

Os ficheiros são **alpha**: ainda há testes dentro do jogo e entre sistemas operativos por concluir. Os convites são privados; partilha-os apenas com os jogadores convidados.

---

[Source code and documentation](https://github.com/Alexandre1116/LinceLink) · [Report an issue](https://github.com/Alexandre1116/LinceLink/issues)
