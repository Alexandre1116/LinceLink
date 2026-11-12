#!/usr/bin/env python3
"""Validate built mod files and prepare a manual upload kit for the existing CurseForge project."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import struct
import zipfile

ROOT = Path(__file__).resolve().parents[1]
JAVA = {'1.12.2': 8, '1.16.5': 8, '1.18.2': 17, '1.20.1': 17, '1.21.1': 21}
FORGE = {'1.12.2': '14.23.5.2864', '1.16.5': '36.2.34', '1.18.2': '40.3.0', '1.20.1': '47.4.28', '1.21.1': '52.1.20'}
FABRIC = {'1.16.5': '0.42.0+1.16', '1.18.2': '0.77.0+1.18.2', '1.20.1': '0.92.6+1.20.1', '1.21.1': '0.116.6+1.21.1'}
PROJECT = 'https://www.curseforge.com/minecraft/mc-mods/lincelink'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--version', required=True)
    args = parser.parse_args()
    if not re.fullmatch(r'\d+\.\d+\.\d+', args.version):
        parser.error('Use a numeric major.minor.patch version')
    license_text = (ROOT / 'LICENSE').read_bytes()
    logo = (ROOT / 'branding/lincelink.png').read_bytes()
    assert logo[:8] == b'\x89PNG\r\n\x1a\n'
    width, height = struct.unpack('>II', logo[16:24])
    assert width == height and width >= 400, 'Use a square PNG of at least 400x400'
    files = []
    for loader, games in [('Forge', FORGE), ('Fabric', FABRIC)]:
        for game, tested in games.items():
            filename = f'lincelink-{loader.lower()}-{game}-{args.version}.jar'
            source = ROOT / f'{loader.lower()}-{game}' / 'build/libs' / filename
            if not source.is_file():
                raise FileNotFoundError(f'Build this module first: {source}')
            with zipfile.ZipFile(source) as jar:
                assert jar.testzip() is None, filename
                assert jar.read('LICENSE-LinceLink.txt') == license_text, filename
                assert jar.read('lincelink.png') == logo, filename
                assert jar.read('assets/tailcatmod/textures/gui/lincelink.png') == logo, filename
                assert not any(name.lower().endswith(('.exe', '.dll', '.so', '.dylib')) for name in jar.namelist()), filename
                assert not any(name.startswith('net/minecraft/') for name in jar.namelist()), filename
                major = struct.unpack('>H', jar.read('pt/tailcatmod/TailcatScreen.class')[6:8])[0]
                assert major == {8: 52, 17: 61, 21: 65}[JAVA[game]], filename
                if loader == 'Fabric':
                    metadata = json.loads(jar.read('fabric.mod.json'))
                    assert metadata['name'] == 'LinceLink' and metadata['version'] == args.version
                    assert metadata['environment'] == 'client' and metadata['license'] == 'MIT'
                    assert metadata['contact']['homepage'] == PROJECT and 'fabric-api' in metadata['depends']
                    assert metadata['depends']['minecraft'] == game and metadata['authors']
                elif game == '1.12.2':
                    metadata = json.loads(jar.read('mcmod.info'))[0]
                    assert metadata['name'] == 'LinceLink' and metadata['version'] == args.version
                    assert metadata['url'] == PROJECT and metadata['mcversion'] == game and metadata['clientSideOnly']
                else:
                    metadata = jar.read('META-INF/mods.toml').decode()
                    assert f'version="{args.version}"' in metadata and 'displayName="LinceLink"' in metadata
                    assert f'displayURL="{PROJECT}"' in metadata and 'license="MIT"' in metadata and 'authors=' in metadata
            dependencies = [] if loader == 'Forge' else [{'project': 'Fabric API', 'slug': 'fabric-api', 'relation': 'Required Dependency', 'testedVersion': tested}]
            files.append({'fileName': filename, 'displayName': f'LinceLink {args.version} - {loader} {game} (Alpha)',
                          'minecraftVersion': game, 'modLoader': loader, 'javaVersion': JAVA[game],
                          'loaderMinimum': tested if loader == 'Forge' else '0.16.10',
                          'releaseType': 'alpha', 'environment': 'client', 'requiredDependencies': dependencies,
                          'extraFeatures': loader == 'Forge' and game == '1.20.1',
                          'changelog': 'CHANGELOG.md', 'sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
                          '_source': source})
    # Validate every input before writing a kit; never emit a partial set of JARs.
    target = ROOT / 'release-artifacts' / f'curseforge-{args.version}'
    target.mkdir(parents=True, exist_ok=True)
    (target / 'files').mkdir(exist_ok=True)
    for entry in files:
        shutil.copy2(entry.pop('_source'), target / 'files' / entry['fileName'])
    for name in ['DESCRIPTION.md', 'CHANGELOG.md', 'UPLOAD.md', 'MODERATOR-NOTES.md', 'GALLERY.md', 'project.json']:
        shutil.copy2(ROOT / 'curseforge' / name, target / name)
    shutil.copy2(ROOT / 'branding/lincelink.png', target / 'logo.png')
    shutil.copy2(ROOT / 'LICENSE', target / 'LICENSE.txt')
    settings = json.loads((ROOT / 'curseforge/project.json').read_text(encoding='utf-8'))
    (target / 'SUMMARY.txt').write_text(settings['summary'] + '\n', encoding='utf-8')
    (target / 'files.json').write_text(json.dumps({'format': 'lincelink-curseforge-upload-plan-v1', 'version': args.version,
                                                'projectUrl': PROJECT, 'files': files}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    rows = ['# Upload de ficheiros', '', 'Cada linha é um upload separado. Tipo: **Alpha**. Ambiente: **Client**.', '',
            '| JAR | Minecraft | Loader | Java | Dependência obrigatória |', '| --- | --- | --- | --- | --- |']
    for entry in files:
        relation = 'Fabric API' if entry['modLoader'] == 'Fabric' else '—'
        rows.append(f"| {entry['fileName']} | {entry['minecraftVersion']} | {entry['modLoader']} | {entry['javaVersion']} | {relation} |")
    rows += ['', 'Só Forge 1.20.1 tem instalação automática, ping/percursos e interface nos 145 idiomas.',
             'Nos restantes ficheiros é necessário instalar o executável Tailcat manualmente.',
             'Não enviar o ZIP completo como mod nem como modpack. Extrair e enviar cada JAR individualmente.', '']
    (target / 'FILES.md').write_text('\n'.join(rows), encoding='utf-8')
    sums = ''.join(f"{entry['sha256']}  files/{entry['fileName']}\n" for entry in files)
    (target / 'SHA256SUMS.txt').write_text(sums, encoding='utf-8')
    archive = ROOT / 'release-artifacts' / f'LinceLink-CurseForge-{args.version}.zip'
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as bundle:
        for path in sorted(target.rglob('*')):
            if path.is_file():
                bundle.write(path, path.relative_to(target).as_posix())
    with zipfile.ZipFile(archive) as bundle:
        assert bundle.testzip() is None
        assert sum(name.endswith('.jar') for name in bundle.namelist()) == 9
    print(f'PASS: nine JARs validated; manual CurseForge update kit: {archive}')


if __name__ == '__main__':
    main()
