---
title: Drops and items
sidebar_position: 4
---

# Drops and items

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
