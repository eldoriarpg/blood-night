---
title: The Nether and the End
sidebar_position: 5
---

# The Nether and the End

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks
what you type. This page shows the files, so you know what the editor writes for you.
:::

The Nether and the End have no day and no night. So they get blood nights in their own way: by the clock, and in the
End also during a dragon fight.

They also have their own names:

| World | Name |
|---|---|
| The Nether | **Blood Tide** |
| The End | **Blood Eclipse** |

Players read these names in the messages and on the boss bar, in their own language.

## By the clock

Every hour, a new chance comes. If it hits, a blood night starts and lasts ten minutes.

These settings are in `nightSettings.timed` in the world file of the Nether or the End:

```yaml
nightSettings:
  timed:
    everyMinutes: 60
    chance: 30
    lengthMinutes: 10
    dragonFight: true
    dragon: blood_dragon
```

| Key | What it does |
|---|---|
| `everyMinutes` | How often a new chance comes |
| `chance` | The chance in percent that a blood night starts |
| `lengthMinutes` | How long the blood night lasts |

Minutes here are game minutes. They follow the server's tick rate.

The fog and the light turn red, just like in the overworld. Players also get the red screen edge, the darkness pulses,
the particles and the heartbeat from the [atmosphere](./atmosphere.md) settings. Rain does nothing here.

`/bloodnight night` shows when the next blood night comes, and `force` and `cancel` work too. These worlds have no
night to wait for, so `force` starts a blood night right away. It lasts as long as the world's blood nights.

## The dragon fight

When the ender dragon appears in the End, a Blood Eclipse begins. Players read "The Blood Dragon awakens". It lasts
until the dragon dies, no matter what the clock says.

While the dragon alone keeps the Blood Eclipse going, its boss bar shows the dragon's health. When a Blood Eclipse
was already running, by the clock or [forced](#by-the-clock), it goes on after the dragon dies until its own time is
up, and its bar shows that time.

The dragon also becomes a **Blood Dragon**:

- It has twice the health.
- It hits harder.
- Every six seconds it shoots a dragon fireball at the nearest player.
- Every ten seconds lightning strikes that player.
- Its hits wither.

| Key | What it does |
|---|---|
| `dragonFight` | `false` keeps dragon fights normal. |
| `dragon` | The special mob the dragon becomes. Leave it empty to keep the normal dragon. |

You can change the Blood Dragon in the [web editor](../editor/sessions.md), like any other special mob.

## Turn it off

To have no blood nights in the Nether or the End at all, set `enabled: false` in their world file.
