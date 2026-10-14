---
title: Messages, boss bar and sounds
sidebar_position: 3
---

# Messages, boss bar and sounds

:::tip Use the web editor
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

A blood night should feel scary. These settings tell players that one is coming.

## Messages

Messages are set for the whole server in `config.yml`:

```yaml
announcements:
  prefix: "<dark_red>[<red>BN</red>]</dark_red> "
  reach: SERVER
  method: SUBTITLE
  personalMethod: ACTION_BAR
  fallbackLocale: en_US
```

| Key | What it does |
|---|---|
| `prefix` | Text in front of chat messages. It uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html). |
| `reach` | Who hears that a blood night begins or ends: `SERVER` (everyone), `WORLD` (players in that world) or `NONE`. |
| `method` | How they see it: `CHAT`, `TITLE`, `SUBTITLE` or `ACTION_BAR`. |
| `personalMethod` | How a single player sees messages meant for them, like "you entered a world with a blood night". |
| `fallbackLocale` | The language for players whose language BloodNight does not have. |

### Languages

Every player reads the messages in the language of their game client. BloodNight knows English, German, Spanish,
Malay, Turkish and Chinese.

## Boss bar

While a blood night runs, players in that world see a boss bar. It gets shorter as the night goes by.

```yaml
announcement:
  bossBar:
    enabled: true
    title: "<red><bold><lang:bloodnight.bossbar.title>"
    color: RED
    overlay: PROGRESS
    flags: [CREATE_WORLD_FOG, DARKEN_SCREEN]
```

- `title` uses MiniMessage. The default shows "Blood Night" in the player's language.
- `color`: `PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE` or `WHITE`.
- `overlay`: `PROGRESS` for one bar, or `NOTCHED_6`, `NOTCHED_10`, `NOTCHED_12`, `NOTCHED_20` for a bar with steps.
- `flags` add effects: `CREATE_WORLD_FOG` brings fog, `DARKEN_SCREEN` makes the sky darker, `PLAY_BOSS_MUSIC` plays
  boss music.

## Effect at begin and end

```yaml
announcement:
  effect:
    enabled: true
    effect: minecraft:darkness
    seconds: 5
```

Players in the world get this potion effect when a blood night begins and when it ends. Players who join during a
blood night get it too.

## Join warning

```yaml
announcement:
  joinWarning: true
```

Players get a message when they enter or leave a world where a blood night runs.

## Sounds

```yaml
soundSettings:
  channel: AMBIENT
  minInterval: 10
  maxInterval: 40
  begin:
    - sound: minecraft:entity.lightning_bolt.thunder
      pitches: [0.8, 1.0]
      volumes: [1.0]
  end:
    - sound: minecraft:entity.lightning_bolt.thunder
      pitches: [0.8, 1.0]
      volumes: [1.0]
  ambience:
    - sound: minecraft:ambient.crimson_forest.additions
      pitches: [0.4, 0.8, 1.2, 1.6]
      volumes: [0.2, 0.6, 1.0]
```

- `begin` plays when the blood night begins, `end` when it ends.
- `ambience` sounds play during the night, every `minInterval` to `maxInterval` seconds, for each player.
- Each time a sound plays, BloodNight picks one pitch and one volume from the lists. That makes it sound less
  repetitive.
- `channel` is the sound slider in the player's settings: `AMBIENT`, `HOSTILE`, `MASTER` and so on.

Sounds are named like in the `/playsound` command.
