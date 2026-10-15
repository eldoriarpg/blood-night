---
title: Special mobs
sidebar_position: 1
---

# Special mobs

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

A special mob starts as a normal mob. When it spawns during a blood night, BloodNight may turn it into a special
mob. It keeps its body, but gets a new name, new stats, new equipment and a new **behaviour**.

All special mobs live in `mobs.yml`. The easiest way to change them is the [web editor](../editor/sessions.md). This
page explains what each part means.

## The parts of a special mob

```yaml
mobs:
  armored_zombie:
    id: armored_zombie
    equipment:
      head: minecraft:diamond_helmet
      chest: minecraft:diamond_chestplate
      legs: minecraft:diamond_leggings
      feet: minecraft:diamond_boots
    attributes:
      name:
        style: ""
        texts:
          en: Armored Zombie
          de: Gepanzerter Zombie
      entityType: ZOMBIE
      exactType: true
      healthModifier: DEFAULT
      health: 2
      damageModifier: DEFAULT
      damage: 2
      changes: []
    behaviour: ...
    mobDrops: ...
    extension: ...
    spawn: ...
```

- `id` is the mob's name in commands. It must be in small letters. Numbers and `_` are fine.
- `behaviour` is the graph you build in the editor. See [Graphs](../editor/graphs.md).
- `mobDrops` is explained in [Drops and items](./drops-and-items.md).
- `extension` is explained in [Riders and mounts](./extensions.md).
- `spawn` is explained in [When mobs spawn](./spawning.md).

## Which mob it turns

`entityType` is the kind of mob, like `ZOMBIE` or `CREEPER`.

`exactType` decides about relatives. A husk is a kind of zombie.

- `exactType: true`: only zombies become this mob.
- `exactType: false`: zombies, husks, drowned, zombie villagers and zombified piglins can become this mob.

The closest fit wins. A husk takes a husk mob when there is one. Only when there is none, it takes a zombie mob.

## MythicMobs mobs

With [MythicMobs](https://mythiccraft.io/index.php?pages/official-mythicmobs/) installed, a special mob can be a
MythicMobs mob. Set `mythicType` to the MythicMobs name and `mythicLevel` to its level:

```yaml
attributes:
  entityType: ZOMBIE
  mythicType: SkeletalKnight
  mythicLevel: 3
```

When this mob is picked for a spawning zombie, the zombie goes away and the MythicMobs mob spawns in its place.
MythicMobs decides its health, damage, equipment and how it fights. BloodNight still decides when it spawns, its
drops, and what happens when the night ends. The web editor lists your MythicMobs mobs to pick from.

Without MythicMobs, or with a name MythicMobs does not know, the zombie stays a normal zombie.

## Name

A mob's name is shown above its head, in each player's own language. Give it a text for every language you want:

```yaml
name:
  style: "<dark_red>"
  texts:
    en: Blood Dragon
    de: Blutdrache
```

- `texts` holds the name by language code: `en`, `de`, `fr` and so on. A code with a region works too, like `pt_br`.
- A player whose language has no text sees the one in your server's fallback language
  (`announcements.fallbackLocale` in `config.yml`). If that is missing too, English. Then any of the others.
- `style` is put in front of the name in every language. It uses
  [MiniMessage](https://docs.advntr.dev/minimessage/format.html), so `"<red>"` or `"<gradient:red:gold>"` work. The
  texts themselves are plain text.
- No texts at all means the mob shows no name.

At first a name only shows when a player looks right at the mob. With `mobSettings.displayNames: true`, a world shows
the names above the mobs all the time.

Names above the head come from the players' game, so players need BloodNight's resource pack to see them in their
language. Without it they see the name in the fallback language. Chat and boss bars show the right language either
way. See [the resource pack](../nights/atmosphere.md#the-resource-pack).

Want one name for everyone, without the pack? Set `resourcePack.translateMobNames: false` in `config.yml`. Then every
player sees the name in your server's fallback language.

## Health and damage

Each one has a modifier and a value.

| Modifier | Health becomes |
|---|---|
| `DEFAULT` | The mob's normal health times the world's `healthMultiplier` (2 by default) |
| `MULTIPLY` | The mob's normal health times `health` |
| `VALUE` | Exactly `health` |

Damage works the same way. With `DEFAULT`, the damage stays normal here. During a blood night, the world's
[damage modifier](../rules/mob-rules.md) makes every monster hit harder anyway.

### Different in one world

A world can give a mob its own name, health or damage. Put it under `mobSettings.overrides` in the world file:

```yaml
mobSettings:
  overrides:
    speed_zombie:
      name:
        style: "<green>"
        texts:
          en: Slow Zombie
      healthModifier: VALUE
      health: 10
```

Leave out what should stay as the mob has it. A world's name texts replace the mob's per language, and its `style`
replaces the mob's when it has one.

## More attribute changes

`changes` can change any attribute of the mob:

```yaml
changes:
  - attribute: minecraft:movement_speed
    operation: ADD_SCALAR
    amount: 0.5
```

| Operation | What it does |
|---|---|
| `ADD_NUMBER` | Adds `amount` |
| `ADD_SCALAR` | Adds `amount` times the normal value. `0.5` means 50% more. |
| `MULTIPLY_SCALAR_1` | Multiplies everything by `1 + amount` |

## Equipment

Each slot (`head`, `chest`, `legs`, `feet`, `mainHand`, `offHand`) takes an item:

- an item name like `minecraft:diamond_sword`, or
- the ID of an item from your [item list](./drops-and-items.md#items).

Leave a slot empty (or the whole `equipment` empty) and the mob keeps the gear it spawned with. A skeleton keeps its
bow this way.
