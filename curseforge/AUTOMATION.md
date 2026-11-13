# Automatic CurseForge releases

The GitHub Actions workflow `.github/workflows/curseforge.yml` uploads each
supported build when a GitHub release is published. It needs the repository
secret `CURSEFORGE_TOKEN`, containing a CurseForge author upload API token.
Keep this token out of the repository and release notes.

Create the release from a commit containing the workflow. Use a version tag
such as `v0.2.0` and attach the ten JARs named
`lincelink-<forge|fabric|neoforge>-<minecraft version>-0.2.0.jar`.
The targeted releases v0.1.8 and v0.1.9 contain only their respective
Forge 1.20.1 and NeoForge 1.21.1 JARs. Missing files make
the corresponding job fail; the workflow does not build or attach the JARs.
Use the release description as the changelog. GitHub prereleases are uploaded
as CurseForge Alpha; other releases are uploaded as Release. Fabric files are
marked as requiring Fabric API. CurseForge moderation still applies.

Each matrix job uploads independently. If a job fails, inspect its result
before retrying: rerunning successful jobs can create duplicate files. The
workflow does not rerun for releases published before it was installed.
