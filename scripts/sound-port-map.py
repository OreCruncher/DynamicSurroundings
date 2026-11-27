#!/usr/bin/env python3
"""Maps the 26.x line's sounds onto this line's, for bringing them over.

Reads both sounds.json files and the sound files they name, pairs each 26.x sound event with this line's (by name with
26.x's prefixes taken off, then a table of renames), and sorts them:

  identical    the same files, byte for byte: nothing to do
  moved        the same sounds, byte for byte, under other file names: nothing to do
  re-encoded   the same file names, but 26.x's files differ: copy 26.x's over this line's, same event name
  re-made      a different set of files: copy 26.x's in and point this line's event at them, same event name
  new          only in 26.x: needs an event here, and config or code that plays it
  keep         only here (this line's footstep acoustics, sounds 26.x dropped): left as they are

Footstep events are listed apart: this line plays its own footstep acoustics (Presence Footsteps), which 26.x dropped
for vanilla's steps with accents, so a 26.x footstep sound replacing one here changes how walking sounds.

A new event whose files are byte for byte some of this line's is noted (26.x kept an old loop under a new name).
An event that plays another event's sounds ("type": "event") is shown as using it.

Licenses come from 26.x's ds_credits and this line's CREDITS.md; a non-commercial one is flagged.

Usage: python scripts/sound-port-map.py [path to the 26.x repository] [-o report.md] [--json map.json]
       (default ../DynamicSurroundingsFabric; with ffprobe on the path, files get their length and channels)
"""

import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)
OURS = os.path.join(REPO, 'src', 'main', 'resources', 'assets', 'dsurround')
THEIRS_IN_REPO = os.path.join('common', 'src', 'main', 'resources', 'assets', 'dsurround')

# 26.x's prefixes, taken off before names are compared
PREFIXES = ('biome.', 'item.', 'player.', 'footsteps.', 'weather.', 'lilypad.', 'netherbrick.', 'soulsand.')

# 26.x event -> this line's, where taking the prefix off isn't enough
RENAMES = {
    'biome.crickets.deep': 'deep_crickets',
    'biome.forest.deep': 'deep_forest',
    'biome.ocean.under': 'underocean',
    'biome.ocean.deep.under': 'underdeepocean',
    'biome.river.under': 'underriver',
    'biome.water.under': 'underwater',
    'biome.wind.desert': 'desert.wind',
    'soulsand.laughter': 'soulsand',
    'weather.rain': 'rain',
    'weather.rain.glass': 'rain.glass',
    'weather.rain.leaves': 'rain.leaves',
    'weather.rain.metal': 'rain.metal',
    'weather.rain.water': 'rain.water',
    'footsteps.floor_squeak': 'floorsqueak.floorsqueak',
    'footsteps.water_through': 'water.water_through',
    'footsteps.leaves_through': 'leaves.leaves_through',
    'footsteps.brush_through': 'brush.brush_through',
    'footsteps.mud': 'mud.mud_walk',
    'footsteps.bluntwood': 'bluntwood.bluntwood_walk',
    'footsteps.dirt': 'dirt.dirt_walk',
    'footsteps.grass': 'grass.grass_walk',
    'footsteps.gravel': 'gravel.gravel_walk',
    'footsteps.metalbar': 'metalbar.metalbar_walk',
    'footsteps.metalbox': 'metalbox.metalbox_walk',
    'footsteps.muffledice': 'muffledice.muffledice_walk',
    'footsteps.rug': 'rug.rug_walk',
    'footsteps.sand': 'sand.sand_walk',
    'footsteps.snow': 'snow.snow_walk',
    'footsteps.stone': 'stone.stone_walk',
    'footsteps.wood': 'wood.wood_walk',
}

NON_COMMERCIAL = re.compile(r'non-?commercial|\bBY-NC\b|\bNC\b', re.IGNORECASE)


def load_json(path):
    with open(path, encoding='utf-8', errors='replace') as f:
        return json.load(f)


def files_of(entry):
    """The sound files an event names, as 'folder/name' paths without the extension"""
    result = []
    for s in entry.get('sounds', []):
        if isinstance(s, str):
            name = s
        elif s.get('type', 'file') == 'event':
            continue
        else:
            name = s['name']
        result.append(name.split(':', 1)[-1])
    return result


def events_used(entry):
    """The other events an event plays the sounds of"""
    return [s['name'].split(':', 1)[-1] for s in entry.get('sounds', [])
            if isinstance(s, dict) and s.get('type') == 'event']


def digests(root, paths):
    return sorted(digest(os.path.join(root, 'sounds', p + '.ogg')) or ('missing:' + p) for p in paths)


def options_of(entry):
    """What an event sets on its sounds besides their names (volume, stream, weight, pitch), in short"""
    seen = set()
    for s in entry.get('sounds', []):
        if isinstance(s, dict):
            for k, v in s.items():
                if k != 'name':
                    seen.add(f'{k}={v}')
    return sorted(seen)


def digest(path):
    if not os.path.exists(path):
        return None
    with open(path, 'rb') as f:
        return hashlib.sha1(f.read()).hexdigest()


def probe(path, ffprobe):
    if not ffprobe or not os.path.exists(path):
        return None
    try:
        out = subprocess.run([ffprobe, '-v', 'error', '-show_entries', 'format=duration:stream=channels', '-of',
                              'csv=p=0', path], capture_output=True, text=True, timeout=20).stdout.split()
        channels = int(out[0]) if out else 0
        duration = float(out[1]) if len(out) > 1 else 0.0
        return channels, duration
    except Exception:
        return None


def describe_files(root, paths, ffprobe, uses=()):
    if not paths:
        return ('uses ' + ', '.join(uses)) if uses else '-'
    info = [probe(os.path.join(root, 'sounds', p + '.ogg'), ffprobe) for p in paths]
    info = [i for i in info if i]
    text = f'{len(paths)} file' + ('s' if len(paths) != 1 else '')
    if info:
        mono = sum(1 for c, _ in info if c == 1)
        lengths = [d for _, d in info]
        text += f', {"mono" if mono == len(info) else "stereo" if mono == 0 else f"{mono} mono"}'
        text += f', {min(lengths):.1f}-{max(lengths):.1f} s' if len(lengths) > 1 else f', {lengths[0]:.1f} s'
    return text


def our_name(theirs_name, ours_events):
    if theirs_name in RENAMES:
        return RENAMES[theirs_name] if RENAMES[theirs_name] in ours_events else None
    if theirs_name in ours_events:
        return theirs_name
    for prefix in PREFIXES:
        if theirs_name.startswith(prefix) and theirs_name[len(prefix):] in ours_events:
            return theirs_name[len(prefix):]
    return None


def their_license(entry):
    credits = entry.get('ds_credits') or []
    return '; '.join(str(c.get('license', '')) for c in credits if isinstance(c, dict))


def our_noncommercial_events():
    """Events this line's CREDITS.md lists under a non-commercial license"""
    path = os.path.join(REPO, 'CREDITS.md')
    result = set()
    if not os.path.exists(path):
        return result
    current = []
    for line in open(path, encoding='utf-8', errors='replace'):
        heading = re.match(r'^\*(.+)\*\s*$', line.strip())
        if heading:
            current = re.findall(r'dsurround:([\w.]+)', heading.group(1))
        elif NON_COMMERCIAL.search(line):
            result.update(current)
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('theirs', nargs='?', default=os.path.join(os.path.dirname(REPO), 'DynamicSurroundingsFabric'))
    parser.add_argument('-o', '--output', help='write the report here (default: stdout)')
    parser.add_argument('--json', help='also write the map as JSON here')
    args = parser.parse_args()

    theirs = os.path.join(args.theirs, THEIRS_IN_REPO)
    if not os.path.isdir(theirs):
        sys.exit(f'No 26.x assets at {theirs}')
    ffprobe = shutil.which('ffprobe') or os.environ.get('FFPROBE')

    ours_events = load_json(os.path.join(OURS, 'sounds.json'))
    their_events = load_json(os.path.join(theirs, 'sounds.json'))
    ours_nc = our_noncommercial_events()
    # This line's files by content, for spotting a new 26.x event that holds one of them
    ours_by_digest = {}
    for name in ours_events:
        for p in files_of(ours_events[name]):
            d = digest(os.path.join(OURS, 'sounds', p + '.ogg'))
            if d:
                ours_by_digest.setdefault(d, set()).add(name)

    rows = []
    matched = set()
    for name in sorted(their_events):
        t = their_events[name]
        o_name = our_name(name, ours_events)
        t_files = files_of(t)
        row = {'theirs': name, 'ours': o_name, 'theirFiles': t_files, 'license': their_license(t),
               'footstep': name.startswith('footsteps.'), 'theirOptions': options_of(t)}
        if o_name is None:
            row['kind'] = 'new'
            holders = set()
            for d in digests(theirs, t_files):
                holders |= ours_by_digest.get(d, set())
            if holders:
                row['holds'] = sorted(holders)
        else:
            matched.add(o_name)
            o_files = files_of(ours_events[o_name])
            row['ourFiles'] = o_files
            row['ourOptions'] = options_of(ours_events[o_name])
            same_content = bool(t_files) and digests(OURS, o_files) == digests(theirs, t_files)
            if sorted(o_files) == sorted(t_files):
                row['kind'] = 'identical' if same_content else 're-encoded'
            else:
                row['kind'] = 'moved' if same_content else 're-made'
        row['theirDesc'] = describe_files(theirs, t_files, ffprobe, events_used(t))
        row['ourDesc'] = describe_files(OURS, row.get('ourFiles', []), ffprobe)
        row['ourNonCommercial'] = o_name in ours_nc if o_name else False
        row['theirNonCommercial'] = bool(NON_COMMERCIAL.search(row['license']))
        rows.append(row)

    keep = []
    for name in sorted(ours_events):
        if name not in matched:
            o_files = files_of(ours_events[name])
            keep.append({'ours': name, 'kind': 'keep', 'ourFiles': o_files, 'ourNonCommercial': name in ours_nc,
                         'ourDesc': describe_files(OURS, o_files, ffprobe)})

    out = open(args.output, 'w', encoding='utf-8') if args.output else sys.stdout

    def table(title, items, columns):
        out.write(f'\n## {title} ({len(items)})\n\n')
        if not items:
            out.write('None.\n')
            return
        out.write('| ' + ' | '.join(c[0] for c in columns) + ' |\n')
        out.write('|' + '|'.join('---' for _ in columns) + '|\n')
        for r in items:
            out.write('| ' + ' | '.join(str(c[1](r)).replace('|', '/') for c in columns) + ' |\n')

    def flags(r):
        f = []
        if r.get('ourNonCommercial'):
            f.append('**ours non-commercial**')
        if r.get('theirNonCommercial'):
            f.append('**theirs non-commercial**')
        if r.get('holds'):
            f.append('holds our ' + ', '.join(r['holds']))
        if r.get('ourOptions') is not None and r.get('ourOptions') != r.get('theirOptions'):
            f.append('options: ' + (', '.join(r.get('theirOptions') or []) or 'none') + ' (ours: ' +
                     (', '.join(r.get('ourOptions') or []) or 'none') + ')')
        return '; '.join(f)

    counts = {}
    for r in rows + keep:
        counts[r['kind']] = counts.get(r['kind'], 0) + 1
    out.write('# Sounds: the 26.x line mapped onto this one\n\n')
    out.write(f'26.x: {len(their_events)} events; this line: {len(ours_events)}. ')
    out.write(', '.join(f'{k} {v}' for k, v in sorted(counts.items())) + '.\n')
    nc = [r.get('ours') for r in rows + keep if r.get('ourNonCommercial')]
    if nc:
        out.write(f'\nNon-commercial here: {", ".join(sorted(set(nc)))}.\n')

    sounds = [r for r in rows if not r['footstep']]
    steps = [r for r in rows if r['footstep']]
    common = [('26.x', lambda r: r['theirs']), ('here', lambda r: r.get('ours') or '-'),
              ('26.x files', lambda r: r['theirDesc']), ('our files', lambda r: r['ourDesc']),
              ('license (26.x)', lambda r: r['license'] or '-'), ('notes', flags)]
    for kind, title in (('re-made', 'Re-made: point our event at 26.x\'s files'),
                        ('re-encoded', 'Re-encoded: copy 26.x\'s files over ours'),
                        ('new', 'New: only in 26.x'),
                        ('moved', 'Moved: the same sounds under other file names'),
                        ('identical', 'Identical')):
        table(title, [r for r in sounds if r['kind'] == kind], common)
    table('Footsteps (this line keeps its own acoustics: replacing changes how walking sounds)', steps,
          [('kind', lambda r: r['kind'])] + common)
    table('Keep: only here', keep, [('here', lambda r: r['ours']), ('files', lambda r: r['ourDesc']),
                                    ('notes', lambda r: '**non-commercial**' if r['ourNonCommercial'] else '')])
    if args.output:
        out.close()

    if args.json:
        with open(args.json, 'w', encoding='utf-8') as f:
            json.dump({'mapped': rows, 'keep': keep}, f, indent=2)


if __name__ == '__main__':
    main()
