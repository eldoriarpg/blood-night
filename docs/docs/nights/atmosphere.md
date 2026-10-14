---
title: Atmosphere
sidebar_position: 4
---

# Atmosphere

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks
what you type. This page shows the files, so you know what the editor writes for you.
:::

A blood night should feel different. These settings make the world look and sound scary while one runs. Everything
goes back to normal when the night ends.

They live in `atmosphere` in the world file:

```yaml
atmosphere:
  sky: true
  vignette: true
  darknessPulses: true
  rain: false
  heartbeat: true
  heartbeatRange: 30
  heartbeatLoudestWithin: 5
  screenEffect: true
  particles:
    - particle: minecraft:ash
      perSecond: 12
    - particle: minecraft:crimson_spore
      perSecond: 4
  scares:
    enabled: false
    chancePerMinute: 10
    kinds: [FLINCH, SCREAM, GUARDIAN]
```

## A red sky

`sky: true` turns the sky, the fog, the clouds and the light red. The fog also comes closer and the stars go out. The
clouds hang low and dark, so the night looks overcast without any rain. It fades in over ten seconds when the night
begins and fades out when it ends.

BloodNight brings its own small data pack for this. It loads by itself, you don't need to install anything. It works
in every world of the normal overworld kind. The Nether and the End keep their sky.

## For each player

| Setting | What the player gets |
|---|---|
| `vignette` | The edge of the screen glows red. |
| `darknessPulses` | Every minute or so the screen goes dark for a moment. |
| `rain` | It rains for them, even if the weather is clear. Off by default. |
| `heartbeat` | They hear a heartbeat when a monster is within `heartbeatRange` blocks (30). Far away it is slow and faint. The closer the monster gets, the faster and louder it beats, up to its loudest within `heartbeatLoudestWithin` blocks (5). Good armor keeps players calm: their heart beats slower and quieter. Damaged armor calms less. |
| `particles` | Ash and spores drift around them. Add or remove particles as you like. |

Only the player sees and hears these. The world itself does not change.

## Scares

Scares are off at first. Switch them on with `scares.enabled: true`.

Every minute, each player has a chance of `chancePerMinute` percent to get one of these:

| Kind | What happens |
|---|---|
| `FLINCH` | The camera jerks as if something hit them from behind. |
| `SCREAM` | Something screams behind them. |
| `GUARDIAN` | The face of an elder guardian flashes on the screen. |

## The resource pack

BloodNight offers players a small resource pack when they join. With it, the screen turns a little red during a blood
night and the edges grow dark. Players can say no, and they still get everything else.

The screen effect needs Minecraft 26.3 or newer on the server. On older servers it simply does not show.

You set the pack for the whole server in `config.yml`:

```yaml
resourcePack:
  offer: true
  url: https://bloodnight.docs.eldoria.de/pack/bloodnight.zip
  required: false
  prompt: "<red>BloodNight would like to show its blood nights with a few extra effects."
```

| Key | What it does |
|---|---|
| `offer` | Whether players are asked to load the pack. |
| `url` | Where the pack is downloaded from. The default always has the newest pack. |
| `required` | With `true`, players who say no can't play on the server. |
| `prompt` | The message shown with the question. It uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html). |

`screenEffect: false` in a world turns the effect off there, even for players with the pack.

### With other resource packs

Players can have BloodNight's pack and your server's pack at the same time. They just get asked twice.

Some plugins build one pack for the whole server and send only that: Nexo, Oraxen and ItemsAdder. When one of them
is installed, BloodNight puts its pack into theirs by itself. It then offers no pack of its own, so players are asked
only once. The first time, rebuild their pack: `/nexo reload pack`, `/oraxen reload pack` or `/iazip`.

To stop this, set `mergeIntoPackPlugins: false` under `resourcePack`.

For any other pack, run `/bn pack export`. It writes BloodNight's pack to `plugins/BloodNight/pack`, as
`bloodnight.zip` and as a folder. Copy the `assets` folder into your own pack.
