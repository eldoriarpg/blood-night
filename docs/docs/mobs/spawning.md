---
title: When mobs spawn
sidebar_position: 2
---

# When mobs spawn

BloodNight never spawns extra mobs. It only changes mobs your server spawns anyway. That keeps your server fast and
respects your spawn settings.

## How a mob is picked

When a mob spawns:

1. BloodNight finds all special mobs that fit it. The kind must match and the spawn rules must allow it.
2. It rolls the world's `spawnPercentage`. With 50, about half of the fitting spawns change.
3. It picks one special mob. A mob with a bigger `weight` is picked more often.

## Spawn rules of a mob

Each special mob has a `spawn` part in `mobs.yml`:

```yaml
spawn:
  weight: 10
  nightOnly: true
  despawn: NIGHT_END
  reasons: []
  conditions:
    worlds: []
    biomes: []
    minY: null
    maxY: null
    maxLight: null
    surface: ANY
```

| Key | What it does |
|---|---|
| `weight` | How often this mob is picked. 0 means it never spawns by itself. |
| `nightOnly` | `true`: only during blood nights. `false`: at any time. |
| `despawn` | `NIGHT_END`: the mob goes away when the blood night ends. `NEVER`: it stays. |
| `reasons` | Only for these spawn reasons, like `NATURAL` or `SPAWNER`. Empty means any reason. |
| `worlds` | Only in these worlds. Empty means all worlds. |
| `biomes` | Only in these biomes, like `minecraft:desert`. A tag works too: `#minecraft:is_forest`. |
| `minY`, `maxY` | Only between these heights. |
| `maxLight` | Only where the block light is this or lower. |
| `surface` | `ANY`, `SKY_VISIBLE` (under open sky) or `UNDERGROUND` (no sky above). |

Here is a cave spider rider that only shows up deep underground:

```yaml
spawn:
  weight: 20
  conditions:
    maxY: 0
    surface: UNDERGROUND
```

## World settings

Each world can change how many mobs turn special, in `mobSettings.spawning`:

```yaml
mobSettings:
  spawning:
    spawnPercentage: 50
    ignoredReasons: [SPAWNER, TRIAL_SPAWNER]
    mobs:
      speed_zombie: 30
      toxic_slime: 0
```

- `spawnPercentage`: the chance a fitting spawn turns special.
- `ignoredReasons`: mobs from these spawn reasons always stay normal. This is good for mob farms with spawners.
- `mobs`: a different weight for some mobs in this world. 0 turns a mob off here.

## When the night ends

When a blood night ends, every special mob runs its "On Night End" part first. Then mobs with
`despawn: NIGHT_END` go away, a few each tick so the server does not lag.

Mobs you spawn with `/bloodnight spawn` outside a blood night stay.
