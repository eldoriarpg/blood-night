---
title: Drops and items
sidebar_position: 4
---

# Drops and items

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

When a player kills a special mob, it can drop extra items and more experience. Its normal loot drops too.

## Items

Drops and equipment name items. There are three kinds:

1. **Plain items** by name, like `minecraft:golden_apple`.
2. **Items registered in game.** Hold any item and run `/bloodnight item register`. You get an ID. Click it to copy
   it. The item keeps everything: name, enchantments, custom data from other plugins.
3. **Items made in the editor**, with a material, a name, lore and enchantments.

Remove a registered item with `/bloodnight item remove <id>`.

## Drop tables

There are three places for drops:

| Place | Where it lives | Used by |
|---|---|---|
| Global | `global_drops.yml` | every world |
| World | `worldDrops` in a world file | that world |
| Mob | `mobDrops` of a special mob | that mob |

A drop looks like this:

```yaml
dropList:
  - itemId: minecraft:golden_apple
    amount: 1
    weight: 10
  - itemId: 3f9a12c4
    amount: 1
    weight: 1
```

A drop with a bigger `weight` is picked more often.

## How many drops?

`weights` says how likely each number of drops is:

```yaml
weights:
  1: 90
  2: 50
  3: 10
```

Here one drop is most likely, three drops are rare.

## Which tables count?

A mob's `mobDrops` decides where things come from:

```yaml
mobDrops:
  weightsScope: GLOBAL
  dropTables: [GLOBAL, WORLD, MOB]
  xpScope: GLOBAL
  xpModifier: MULTIPLY
  xpValue: 4
```

- `weightsScope`: whose `weights` to use: `GLOBAL`, `WORLD` or `MOB`.
- `dropTables`: which drop lists to pick from. They are mixed together.
- `xpScope`: whose experience setting to use.

## Experience

| `xpModifier` | Experience becomes |
|---|---|
| `DEFAULT` | Normal |
| `MULTIPLY` | Normal times `xpValue` |
| `VALUE` | Exactly `xpValue` |

By default, special mobs give 4 times the normal experience. All drop lists start empty, so add your own items.

## Normal loot of special mobs

`vanillaLoot` in `mobDrops` decides what happens to the normal loot of a special mob:

| `vanillaLoot` | What drops |
|---|---|
| `KEEP` | The normal loot, as always |
| `MULTIPLY` | More normal loot, like ordinary monsters in a blood night (see below) |
| `CLEAR` | No normal loot. Only the drops from the tables. |

Items a mob wears or holds always drop as normal.

## Ordinary monsters

During a blood night, ordinary monsters drop more too. They need to be killed by a player. You set this in
`worldDrops.vanilla` in the world file:

```yaml
worldDrops:
  vanilla:
    enabled: true
    lootMultiplier: 2
    extraDrops: false
    xpModifier: MULTIPLY
    xpValue: 2
    excludedTypes: []
    excludedReasons: [SPAWNER, TRIAL_SPAWNER, NETHER_PORTAL]
```

- `lootMultiplier`: how much loot drops. `2` means twice as much. A stack never grows past its size.
- `extraDrops`: with `true`, ordinary monsters also get drops from the world's tables.
- `xpModifier` and `xpValue`: their experience, like in the table above.
- `excludedTypes`: kinds of mobs that keep their normal loot, like `ZOMBIFIED_PIGLIN`.
- `excludedReasons`: mobs from spawners or nether portals keep their normal loot. This stops mob farms from getting
  more.

Items a monster wears or holds are never multiplied. During a blood night, monsters can't pick up items either. So a
dropped diamond can't come back twice.

Mobs of other plugins keep their loot (see [mobs of other plugins](spawning.md#mobs-of-other-plugins)).
