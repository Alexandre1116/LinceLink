#!/usr/bin/env python3
"""Audit UI keys against official Minecraft 1.20.1 assets; do not redistribute translations."""
import argparse
import concurrent.futures
import hashlib
import json
from pathlib import Path
import re
import tempfile
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]
PREFIXES = ('gui.', 'mco.', 'menu.', 'connect.', 'disconnect.', 'selectServer.',
            'selectWorld.', 'commands.publish.', 'pack.source.', 'lanServer.', 'addServer.', 'chat.')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--cache', type=Path, default=Path(tempfile.gettempdir()) / 'tailcat-language-audit')
    parser.add_argument('--client-jar', type=Path)
    parser.add_argument('--report', type=Path, default=ROOT / 'docs/idiomas.json')
    args = parser.parse_args()
    args.cache.mkdir(parents=True, exist_ok=True)

    def fetch(url):
        request = urllib.request.Request(url, headers={'User-Agent': 'Tailcat-Minecraft-Language-Audit'})
        with urllib.request.urlopen(request, timeout=45) as response:
            return response.read()

    manifest = json.loads(fetch('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'))
    version = json.loads(fetch(next(v['url'] for v in manifest['versions'] if v['id'] == '1.20.1')))
    index = json.loads(fetch(version['assetIndex']['url']))['objects']

    def asset(entry):
        sha = entry['hash']
        target = args.cache / sha
        if not target.exists():
            target.write_bytes(fetch(f'https://resources.download.minecraft.net/{sha[:2]}/{sha}'))
        data = target.read_bytes()
        if hashlib.sha1(data).hexdigest() != sha:
            raise ValueError(f'Asset hash mismatch: {sha}')
        return json.loads(data)

    metadata = asset(index['pack.mcmeta'])['language']
    client = args.client_jar or args.cache / 'client-1.20.1.jar'
    if not client.exists():
        client.write_bytes(fetch(version['downloads']['client']['url']))
    if hashlib.sha1(client.read_bytes()).hexdigest() != version['downloads']['client']['sha1']:
        raise ValueError('Client JAR hash mismatch')
    with zipfile.ZipFile(client) as archive:
        english = json.loads(archive.read('assets/minecraft/lang/en_us.json'))
    sources = list((ROOT / 'forge-1.20.1/src/main/java').rglob('*.java'))
    sources += list((ROOT / 'common/src/main/java').rglob('*.java'))
    keys = set()
    for source in sources:
        for text in re.findall(r'"([^"\n]+)"', source.read_text()):
            if text.startswith(PREFIXES) and re.fullmatch(r'[A-Za-z0-9_.]+', text):
                keys.add(text)
    if not keys or keys - english.keys():
        raise ValueError(f'Unknown Minecraft keys: {sorted(keys - english.keys())}')

    def arguments(text):
        text = text.replace('%%', '')
        matches = re.findall(r'%(?:(\d+)\$)?[sdf]', text)
        return max([int(value) for value in matches if value] + [sum(not value for value in matches)])

    def inspect(item):
        locale, properties = item
        native = english if locale == 'en_us' else asset(index[f'minecraft/lang/{locale}.json'])
        missing = sorted(keys - native.keys())
        bad_formats = [key for key in keys if key in native and arguments(native[key]) > arguments(english[key])]
        if bad_formats:
            raise ValueError(f'{locale}: extra translation placeholders: {bad_formats}')
        # The game's English fallback must always resolve every UI key.
        assert all(key in native or key in english for key in keys)
        return locale, {'name': properties['name'], 'region': properties['region'],
                        'bidirectional': properties.get('bidirectional', False),
                        'native_keys': len(keys) - len(missing), 'english_fallback_keys': missing}

    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
        languages = dict(sorted(pool.map(inspect, metadata.items())))
    report = {'minecraft': '1.20.1', 'asset_index': version['assetIndex']['url'],
              'language_count': len(languages), 'keys': sorted(keys), 'languages': languages}
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n')
    complete = sum(not value['english_fallback_keys'] for value in languages.values())
    print(f'PASS: {len(keys)} UI keys resolve in all {len(languages)} game languages.')
    print(f'Complete native keys and valid parameter counts: {complete}/{len(languages)} locales.')


if __name__ == '__main__':
    main()
