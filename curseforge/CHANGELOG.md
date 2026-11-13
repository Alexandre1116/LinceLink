# LinceLink 0.2.0 — Release

- Added automatic Tailcat v0.7.0 installation to all supported builds on
  Windows and Linux x64/ARM64. Installation runs on demand, verifies the
  downloaded archive and can be cancelled before the tunnel starts.
- Added English and Portuguese interface text to the previously untranslated
  Fabric and Forge builds. Forge 1.20.1 continues to use Minecraft's native
  translations for 145 languages; other locales on the new translations fall
  back to English.
- Corrected the Fabric 1.16.5 Fabric API dependency identifier.
- Added NeoForge 1.21.1 to the supported downloads.
- Retained Forge 1.20.1 commands, Tab latency, Direct/DERP status and native
  language support.

The validation notes list checked configurations and untested flows. A
two-client Minecraft connection was intentionally not tested in this release;
the project owner plans to test it with friends.

# LinceLink 0.1.7 — Alpha

- Added the existing CurseForge project URL, author, source and issue links to the mod metadata.
- Added an explicit MIT license and author/contact metadata to Fabric builds.
- Included the LinceLink MIT license inside every JAR.
- Prepared English-first CurseForge description, per-file loader/game/dependency tags and publication instructions.
- Kept the LinceLink name/logo, world-sharing protocol and runtime behavior from 0.1.6.

Forge 1.20.1 includes automatic Tailcat installation, numerical Tab latency, direct/DERP reporting and native interface translations. Other builds retain basic world sharing and manual Tailcat installation.

These files are alpha builds. Graphical, cross-platform and complete two-client validation remain pending.
