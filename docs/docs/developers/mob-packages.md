---
title: Mob packages
sidebar_position: 2
---

# Mob packages

BloodNight shares mobs in its own format. A store is a folder with an `index.json` and one JSON file per mob.

## index.json

```json
{
  "format": 1,
  "id": "bloodnight",
  "packs": [
    {"id": "classic", "names": {"en": "Classic"}, "descriptions": {"en": "The mobs BloodNight always had."}}
  ],
  "entries": ["speed_zombie", "toxic_slime"]
}
```

## One mob

`speed_zombie.json` lies next to `index.json`:

```json
{
  "format": 1,
  "id": "speed_zombie",
  "pack": "classic",
  "names": {"en": "Speed Zombie", "de": "Flinker Zombie"},
  "descriptions": {"en": "Very fast.", "de": "Sehr schnell."},
  "requires": [],
  "mob": { "id": "speed_zombie", "attributes": { ... }, "behaviour": { ... }, "loot": { "tables": ["speed_zombie"] } },
  "lootTables": { "speed_zombie": { "name": "Speed Zombie", "entries": [ ... ] } },
  "particleEffects": { "speed_dust": { "name": "Speed dust", "emitters": [ ... ] } }
}
```

- `format` is 1. BloodNight refuses files of a format it does not know.
- `names` and `descriptions` hold one text per language.
- `requires` lists plugins the mob needs, like `MythicMobs`.
- `mob` is the whole mob, as it would stand in `mobs.yml`. Its `id` is the same as the package's.
- `lootTables` holds the [loot tables](../mobs/loot-tables.md) the mob gives itself, as they would stand in
  `loot_tables.yml`. Installing the mob adds the ones the server does not have yet. A table the server already has
  stays as it is, and updates never change tables.
- `particleEffects` holds the [particle effects](../mobs/particle-effects.md) of its own the mob plays or wears, as
  they would stand in `particle_effects.yml`. Built-in effects (`bloodnight:...`) need not be in it. Installing adds the
  ones the server does not have yet. An update with `--particles` writes them all.

The easiest way to make one: build the mob in the web editor, then copy it from `mobs.yml`.

## Versions

A package has no version number. BloodNight hashes the mob's behaviour and extension. When the hash changes, it is a
new version.
