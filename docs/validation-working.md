# Release validation

This is an evidence log for the current local candidate. It does not claim that
every game version or operating system has passed an in-game test.

- **Current candidate 0.2.0:** all ten modules compiled locally. The CurseForge
  packaging validator accepted ten JARs, including loader/game metadata,
  licenses, logo assets, Java class versions, and archive contents. This is a
  build/package check, not an in-game compatibility check. The normal-release
  kit is prepared locally and has not been published.
- **NeoForge 1.21.1 / 0.1.9:** on Windows with NeoForge 21.1.266, the mod
  loaded, opened a singleplayer world, installed Tailcat, and created and
  copied an invitation. A second client was not used.
- **Forge 1.20.1 / 0.1.8:** the owner reported successful dedicated-server
  entry and tested `/lincelink start`, `invite`, `status`, and `stop`; the
  invitation output was clickable to copy.
- The local candidate adds on-demand Tailcat installation to the eight
  previously manual-install Fabric and Forge variants. All eight compiled.
  On Windows, local Fabric 1.21.1 and Forge 1.21.1 builds each loaded, opened
  a new world, installed Tailcat, generated an invitation, and copied it.
  Neither used a second Minecraft client.
- A focused Fabric 1.20.1 recheck confirmed singleplayer hosting, Tailcat
  installation, invitation generation, and copy/paste. No second client was
  used.
- **New remote check — Fabric 1.16.5:** the corrected local build loaded with
  Minecraft 1.16.5, Fabric Loader 0.20.1, and Fabric API 0.42.0+1.16. The log
  showed 50 mods including LinceLink 0.1.7; the missing `fabric-api`
  dependency error disappeared. The join screen appeared and a new world was
  created. Computer-use input could not open the pause menu, so hosting,
  Tailcat installation, invitation creation/copy, and the Portuguese UI were
  not verified on this build. This is an uncompleted check, not evidence of a
  LinceLink failure.
- **Fabric 1.18.2 and Forge 1.16.5/1.18.2:** earlier isolated profiles loaded,
  but the pause-menu interactions could not be completed. Their host/invite
  flows remain unverified. Do not repeat flows already confirmed above.
- **Forge 1.12.2:** the test profile declared Forge 14.23.5.2864, but there is
  no effective launch log or crash report for that loader version. The
  launcher emitted JSON-schema errors for its 2864 metadata. The only
  recorded launch used Forge 14.23.5.2859, loaded five mods, and exited with
  code 0. The earlier launch issue's cause is therefore undetermined; the
  available evidence does not establish a LinceLink failure on 2864.
- Java 8 installer smoke checks passed for the official Linux download/run and
  reuse, Windows archive extraction, checksum/path rejection, concurrent
  requests, and retry. A Tailcat TCP round-trip smoke check also passed. These
  checks are not Minecraft two-client tests.
- The candidate's English/Portuguese resources compile for nine variants.
  Those locale changes have not been checked in-game. Other locales on those
  variants fall back to English; Forge 1.20.1 uses Minecraft's native locale
  keys.

The owner will test Minecraft connections with two clients later with friends.
Do not run or require those tests for this release. State all other untested
flows plainly; do not imply success or failure without evidence. Preserve
existing player instances and worlds.
