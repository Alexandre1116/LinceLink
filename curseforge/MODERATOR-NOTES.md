# LinceLink — technical review notes

LinceLink is an MIT-licensed client mod for exposing Minecraft's integrated singleplayer server through Tailcat v0.7.0. It is not intended for a dedicated server install.

The JARs contain LinceLink code, its owner-provided logo, metadata and the LinceLink license. They do not bundle Minecraft, Fabric API, a native executable, or the Tailcat Go library.

Forge 1.20.1 checks for the Tailcat executable in the instance's tailcat directory, a configured path or PATH. When it is missing, it downloads a pinned official Tailscale Tailcat v0.7.0 archive from GitHub, verifies its hardcoded SHA-256, extracts only allowlisted files, stores the upstream license/README beside the executable, and launches it without a shell. The download/installation and its supported operating systems are disclosed in the public description. Other builds use a manually installed executable.

Relevant source: common/src/main/java/pt/tailcatmod/transport/TailcatInstaller.java and Tunnel.java in the public repository. The download is a runtime dependency, not an alternative distribution link for LinceLink. These notes disclose that behavior for review; they do not claim CurseForge has approved it or that a specific policy exception applies.

Hosting exposes the selected Minecraft world port through Tailcat. It does not run Tailcat exit-node, SSH, exec or file-sharing services. Invite strings contain transport connection material and should be treated as secrets. LinceLink sends route-reporting peers' public keys only to the world host, and distributes UUID/route states to mod clients. It does not include a third-party analytics or advertising SDK.

Tailcat is a separate BSD-3-Clause project by Tailscale Inc. and contributors; its upstream license is included with the downloaded component. LinceLink has no official affiliation with Tailscale or Mojang.

Files are prepared as Alpha because graphical, full cross-platform and complete in-game route-reporting validation remains pending. The project description deliberately distinguishes Forge 1.20.1 extras from the basic builds for other versions.
