---
title: How mobs move
sidebar_position: 3
---

# How mobs move

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

A special mob walks like the normal mob it comes from. With `movement` you give it another way to move. Pick one
preset. In the editor it is the **Movement** part of a mob. It only shows the numbers the preset uses.

```yaml
movement:
  preset: KEEP_DISTANCE
  closest: 6
  farthest: 10
```

## The presets

| Preset | What the mob does | Numbers it uses |
|---|---|---|
| `VANILLA` | Walks like the normal mob. | none |
| `AGGRESSIVE` | Hunts the nearest player within `range` blocks. It does not give up. | `range` |
| `KEEP_DISTANCE` | Stays between `closest` and `farthest` blocks from its target. Good for mobs that shoot. | `closest`, `farthest` |
| `HIT_AND_RUN` | Runs in, hits once, then backs off for `seconds` seconds. Then it comes back. | `seconds` |
| `STALKER` | Follows you at a distance. It closes in when you look away, or when your health is below `fleeBelow`. | `closest`, `farthest`, `fleeBelow` |
| `COWARD` | Runs away when its health drops below `fleeBelow`. It heals while it runs and comes back at twice that. | `fleeBelow` |
| `PACK` | Attacks the target of other mobs of the same kind within `range` blocks. | `range` |
| `GUARD` | Stays within `range` blocks of where it spawned. It lets go of targets that run far away. | `range` |

`fleeBelow` is a share of the health, from 0 to 1. `0.3` means 30 %.

Only mobs that walk on paths follow a preset: zombies, skeletons, creepers, spiders, endermen, witches and the like.
Slimes, phantoms and ghasts keep moving the way they always do.

## Change it during a fight

The **Change Movement** node switches the preset while the mob lives. For example, a mob can turn into a coward
once it is hurt. Its numbers stay the same.

Three more nodes let a graph move a mob by itself:

- **Move To** walks the mob to a location. It tells you whether it found a path.
- **Stop Moving** makes it stand still for a moment.
- **Is Moving** tells you whether it is walking right now.

## The ready-made mobs

Some mobs in the [store](store.md) use a preset already. The knight zombie hits and runs. Skeletons and wizards keep
their distance. The invisible zombie, the ender creeper and the fearful enderman stalk you. Speed zombies hunt as a
pack. The speed creeper is aggressive.
