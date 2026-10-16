---
title: Loot tables
sidebar_position: 5
---

# Loot tables

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

When a player kills a special mob, it can drop extra items and more experience. Its normal loot drops too.

The extra items come from loot tables. You make a table once, in the **Loot** tab of the editor. Then you give it to
mobs, to worlds or to the whole server.

## A loot table

A loot table is a list of entries. Each entry is one [item](items.md), how likely it is, and how many drop.

```yaml
tables:
  zombie_scraps:
    name: Zombie scraps
    entries:
      - item: minecraft:iron_nugget
        weight: 10
        amount: {mode: RANGE, min: 1, max: 4}
        repeatable: true
      - item: minecraft:diamond
        weight: 1
        amount: {mode: CHOICES, choices: {1: 9, 2: 1}}
        repeatable: false
```

- `weight`: an entry with a bigger weight is picked more often. Here the nuggets are picked 10 times as often as the
  diamond.
- `amount`: how many drop when the entry is picked.
  - `RANGE` drops any number from `min` to `max`. All are equally likely.
  - `CHOICES` drops one of a few numbers. Each has its own weight. Here one diamond drops 9 times as often as two.
- `repeatable`: whether the entry can be picked again in the same kill. With `false`, it drops at most once.

An amount fits in one stack: at most 64 iron nuggets, 16 ender pearls, and always one sword, since swords do not stack.
The editor keeps amounts within that.

The tables live in `loot_tables.yml`. The editor shows each entry's chance and usual amount. A **Roll** button shows
one pick.

## How many drop

A kill does not drop the whole table. It drops a few entries. `slots` says how likely each number is:

```yaml
slots:
  0: 50
  1: 40
  2: 10
```

Here half of the kills drop nothing from the tables. Most others drop one entry, and a few drop two.

For each slot, BloodNight picks one entry by its weight. Then it rolls that entry's amount.

## Mobs, worlds and the server

You give tables to three places:

| Place | Where it lives | Counts for |
|---|---|---|
| Mob | `loot` of a special mob in `mobs.yml` | that mob |
| World | `loot` in the world file | every special mob in that world |
| Server | `loot` in `config.yml` | every special mob on the server |

A mob looks like this:

```yaml
loot:
  tables: [zombie_scraps]
  slots: {1: 2, 2: 1}
  inherit: true
  replaceSame: true
  vanillaLoot: KEEP
  experience: {modifier: MULTIPLY, value: 4}
```

### Worlds and the server

A world and the server have the same keys: `tables`, `slots` and `experience`. A world also has `inherit` and
`replaceSame`.

By default the server gives every special mob a chance of 1, 2 or 3 drops, but no tables. So nothing drops until you
add a table somewhere.

### All tables in one pool

When a mob dies, BloodNight puts the entries of all its tables into one pool:

1. The mob's own tables.
2. The world's tables, unless the mob says `inherit: false`.
3. The server's tables, unless the mob or the world says `inherit: false`.

A table given twice, for example to the mob and to the server, counts only once.

### How many: the closest one wins

`slots` comes from the closest place that sets it: the mob, else the world, else the server. Leave it out on the mob
to use the world's or the server's.

The same goes for `experience`.

### Same items

With `replaceSame: true`, a mob's items replace the same items from the world and the server. Say the server drops
diamonds now and then, and a boss mob drops many diamonds. With `replaceSame: true`, the server's diamond entry is
left out for the boss. So only the boss's chance and amount count. All other server entries stay in the pool.

Same item means the same item ID, like `minecraft:diamond` or the ID of a [registered item](items.md).

A world can replace the server's items the same way. On a mob, `replaceSame` is on by default. On a world it is off.

The mob's **Loot** part in the editor shows the whole pool for a world you pick. It shows each item's chance per kill,
where it comes from, and how many drop. A **Roll** button shows one kill's loot.

## Experience

| `modifier` | Experience becomes |
|---|---|
| `DEFAULT` | Normal |
| `MULTIPLY` | Normal times `value` |
| `VALUE` | Exactly `value` |

By default, special mobs give 4 times the normal experience. That is the server's setting.

## Normal loot of special mobs

`vanillaLoot` in a mob's `loot` decides what happens to its normal loot:

| `vanillaLoot` | What drops |
|---|---|
| `KEEP` | The normal loot, as always |
| `MULTIPLY` | More normal loot, like ordinary monsters in a blood night (see below) |
| `CLEAR` | No normal loot. Only what the tables drop. |

Items a mob wears or holds always drop as normal.

These loot tables are not Minecraft's loot tables. To drop one of Minecraft's, like the loot of a chest, use the node
[Drop Loot](../reference/nodes/world.md). The [blood night reward](items.md#the-blood-night-reward) is one of
Minecraft's too.

## Ordinary monsters

During a blood night, ordinary monsters drop more too. They need to be killed by a player. You set this in
`loot.ordinary` in the world file:

```yaml
loot:
  ordinary:
    enabled: true
    lootMultiplier: 2
    tables: []
    slots: null
    xpModifier: MULTIPLY
    xpValue: 2
    excludedTypes: []
    excludedReasons: [SPAWNER, TRIAL_SPAWNER, NETHER_PORTAL]
```

- `lootMultiplier`: how much normal loot drops. `2` means twice as much. A stack never grows past its size.
- `tables` and `slots`: loot tables ordinary monsters drop from as well. They work like the ones above. The server has
  its own list under `loot.ordinary` in `config.yml`. A world's tables join the server's, unless the world says
  `inherit: false`.
- `xpModifier` and `xpValue`: their experience, like in the table above.
- `excludedTypes`: kinds of mobs that keep their normal loot, like `ZOMBIFIED_PIGLIN`.
- `excludedReasons`: mobs from spawners or nether portals keep their normal loot. This stops mob farms from getting
  more.

Items a monster wears or holds are never multiplied. During a blood night, monsters can't pick up items either. So a
dropped diamond can't come back twice.

Mobs of other plugins keep their loot (see [mobs of other plugins](spawning.md#mobs-of-other-plugins)).

## Mobs from the store

Mobs from the [store](store.md) bring their own tables. When you install one, its tables are added. Updates never
change a table you already have, so your changes stay.

## For plugin developers

Other plugins can change the loot before it drops with `BloodNightLootEvent`. See
[the developer pages](../developers/api.md).
