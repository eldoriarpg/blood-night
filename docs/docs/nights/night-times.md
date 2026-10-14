---
title: Night times and length
sidebar_position: 2
---

# Night times and length

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

All of these live in `nightSettings` in the world file.

```yaml
nightSettings:
  begin: 13000
  end: 23000
  skippable: false
  length:
    mode: NORMAL
    seconds: 600
    maxSeconds: 900
  beginCommands: []
  endCommands: []
  blockedCommands: []
```

## When is it night?

Minecraft counts the time of day in ticks, from 0 to 24000. `begin` and `end` say when a night starts and stops.

- `13000` is when monsters start to spawn.
- `23000` is just before sunrise.

A night may go past midnight. For example `begin: 18000` and `end: 2000` works.

## Can players sleep through it?

`skippable: false` means players can't skip a blood night by sleeping. They can still use beds to set their spawn
point. When they lie down, they get a short message.

This also stops other ways to skip the night by sleeping, like the "players sleeping percentage" game rule.

## How long is a blood night?

`length.mode` has three choices:

| Mode | What happens |
|---|---|
| `NORMAL` | The night is as long as any other night. |
| `EXTENDED` | The night lasts `seconds` seconds. |
| `RANGE` | Each night picks a length between `seconds` and `maxSeconds`. |

A normal Minecraft night lasts about 8 minutes. With `EXTENDED` and `seconds: 1200`, the blood night lasts
20 minutes. The sun and moon move slower, but they keep moving.

If someone changes the time with `/time`, BloodNight stops stretching that night.

## Run commands at begin and end

```yaml
beginCommands:
  - "say A blood night begins in {world}!"
  - "give {player} minecraft:torch 4"
endCommands:
  - "say The sun is up again."
```

The server console runs these commands.

- `{world}` becomes the world's name.
- A command with `{player}` runs once for each player in the world.

## Block commands

See [Blocked commands](../rules/blocked-commands.md).
