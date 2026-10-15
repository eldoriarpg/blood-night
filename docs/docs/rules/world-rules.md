---
title: World rules
sidebar_position: 4
---

# World rules

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

A blood night can change the world itself: make it harder, let more monsters spawn, or bring a thunderstorm. At first,
only the creakings wake up. You choose what changes in `nightRules` in the world file:

```yaml
nightRules:
  difficulty: HARD
  monsterSpawnLimit: 140
  monsterTicksPerSpawn: -1
  weather: THUNDER
  gameRules:
    minecraft:spawn_phantoms: "true"
  phantomsOnlyAtBloodNights: false
  monstersDontBurn: false
  creakingsWake: true
  raids: UNCHANGED
```

| Key | What it does |
|---|---|
| `difficulty` | The difficulty during the night: `PEACEFUL`, `EASY`, `NORMAL` or `HARD`. `UNCHANGED` leaves it. |
| `monsterSpawnLimit` | How many monsters may be around each player. The normal value is 70. `-1` leaves it. |
| `monsterTicksPerSpawn` | How often monsters try to spawn, in ticks. `1` is every tick. `-1` leaves it. |
| `weather` | `CLEAR`, `RAIN` or `THUNDER` for the night. `UNCHANGED` leaves it. |
| `gameRules` | Any game rules for the night, by their name. Write the value in quotes. |
| `phantomsOnlyAtBloodNights` | With `true`, phantoms only come during blood nights. |
| `monstersDontBurn` | With `true`, zombies and skeletons don't burn in the sun. This lasts through the dawn after the night. |
| `creakingsWake` | With `true`, creakings hunt during the night. |
| `raids` | `ALLOW` or `DENY` raids during the night. `UNCHANGED` leaves it. |

The last three come from BloodNight's own data pack. It loads by itself.

More monsters is a good way to make a blood night busy. BloodNight does not spawn mobs itself. It lets the game
spawn more, so the server stays fast.

## When the night ends

Everything goes back to how it was. If you changed something yourself during the night, for example with
`/difficulty`, your change stays.

If the server stops during a blood night, the world gets its old values back on the next start.
