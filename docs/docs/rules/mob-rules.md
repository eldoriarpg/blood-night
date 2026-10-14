---
title: Harder monsters
sidebar_position: 1
---

# Harder monsters

:::tip Use the web editor
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

During a blood night, all monsters get tougher, not only the special ones. These settings are in `mobSettings` in
the world file.

```yaml
mobSettings:
  damageModifier: 2
  healthMultiplier: 2
  forcePhantoms: true
  creepers:
    blockDamage: false
    always: false
```

## Damage

`damageModifier: 2` means monsters hit players twice as hard. This counts for normal and special monsters, and for
their arrows and fireballs too. Set it to 1 to keep damage normal.

## Health

`healthMultiplier: 2` makes normal monsters feel twice as strong. Players deal half damage to them.

Special mobs with the `DEFAULT` health setting get twice their normal health instead. See
[Special mobs](../mobs/custom-mobs.md#health-and-damage).

## Phantoms

`forcePhantoms: true` makes phantoms come for every player, even players who slept recently. When the blood night
ends, or the player leaves the world, everything goes back to how it was.

## Creepers

- `blockDamage: false` means creeper explosions don't break blocks. They still hurt players.
- `always: false` means this only counts during blood nights. With `true`, creepers never break blocks in this
  world.
