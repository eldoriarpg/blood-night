---
title: Messages, boss bar and sounds
sidebar_position: 3
---

# Messages, boss bar and sounds

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

A blood night should feel scary. These settings tell players that one is coming.

## Messages

Messages are set for the whole server in `config.yml`:

```yaml
announcements:
  prefix: "<dark_red>[<red>BN</red>]</dark_red> "
  method: TITLE
  elsewhereMethod: CHAT
  personalMethod: ACTION_BAR
  fallbackLocale: en_US
  money: true
```

| Key | What it does |
|---|---|
| `prefix` | Text in front of every chat message of BloodNight, command answers included. It uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html). Titles and the action bar have no prefix. |
| `method` | How players in the world of the blood night see that it begins or ends: `CHAT`, `TITLE`, `SUBTITLE`, `ACTION_BAR` or `NONE`. |
| `elsewhereMethod` | How players in all other worlds see it. `NONE` keeps it to the world of the night. |
| `personalMethod` | How a single player sees messages meant for them, like "you entered a world with a blood night". |
| `fallbackLocale` | The language for players whose language BloodNight does not have: `en_US`, `de_DE`, `es_ES`, `fr_FR`, `ms_MY`, `tr_TR` or `zh_CN`. |
| `money` | Whether players see how much [money](../mobs/loot-tables.md#money) a kill paid them. They see it the `personalMethod` way. |

### Languages

Every player reads the messages in the language of their game client. BloodNight knows English, German, Spanish,
French, Malay, Turkish and Chinese. A player with Austrian German gets German, a player with Canadian French gets
French. Players with any other language get the `fallbackLocale`.

### Changing the texts

BloodNight writes its texts into `plugins/BloodNight/messages/`, one file per language:
`messages_en_US.properties`, `messages_de_DE.properties` and so on. Change a line there and run `/bloodnight reload`.

When BloodNight gets new texts in an update, it adds them to these files. Your changes stay.

Each line is a key and a text in [MiniMessage](https://docs.advntr.dev/minimessage/format.html):

```properties
bloodnight.night.begin=<error>A blood night has begun in <accent><world></accent>.
```

Words in angle brackets like `<world>` are filled in by BloodNight. Keep them. The texts use BloodNight's own styles,
so every message looks alike:

| Style | Looks like |
|---|---|
| `<accent>` | Gold. For names and numbers. |
| `<muted>` | Dark gray. For side notes. |
| `<success>` | Green. Something worked. |
| `<warn>` | Yellow. Something to look at. |
| `<error>` | Red. Something went wrong. |
| `<cmd:'/bloodnight setup'>` | A command. A click puts it into the chat. |
| `<url:'https://example.com'>a link</url>` | A link. A click opens it. |

You can use every other MiniMessage tag too, like `<red>` or `<bold>`.

## Boss bar

While a blood night runs, players in that world see a boss bar. It gets shorter as the night goes by.

```yaml
announcement:
  bossBar:
    enabled: true
    title: "<red><bold><name>"
    color: RED
    overlay: PROGRESS
    flags: [CREATE_WORLD_FOG]
```

- `title` uses MiniMessage. `<name>` is the night's name in the player's language: Blood Night, Blood Tide in the
  Nether, Blood Eclipse in the End.
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

## A first-night guide

```yaml
announcement:
  introDialog: true
```

On their first blood night, each player sees a short window. It tells them what happens and what helps: find
shelter, make light, stay together, wear good armor. A button closes it. Every player sees it only once, on any
world. It shows in the player's own language, like BloodNight's other messages.

Set `introDialog: false` to switch it off in a world.

## Sounds

In the web editor you pick a sound by what it sounds like, like "Thunder roars" or "Zombie groans", and you can play
it right there. In the file, sounds look like this:

```yaml
soundSettings:
  channel: AMBIENT
  minInterval: 10
  maxInterval: 40
  begin:
    - sound: minecraft:entity.lightning_bolt.thunder
      pitch: {min: 0.8, max: 1.0}
      volume: {min: 1.0, max: 1.0}
  end:
    - sound: minecraft:entity.lightning_bolt.thunder
      pitch: {min: 0.8, max: 1.0}
      volume: {min: 1.0, max: 1.0}
  ambience:
    - sound: minecraft:ambient.crimson_forest.additions
      pitch: {min: 0.5, max: 1.6}
      volume: {min: 0.2, max: 1.0}
```

- `begin` plays when the blood night begins, `end` when it ends.
- `ambience` sounds play during the night, every `minInterval` to `maxInterval` seconds, for each player.
- `pitch` makes a sound deeper or higher: `1` is normal, `0.5` is the deepest, `2` the highest. `volume` goes from
  `0` (silent) to `1` (normal). Each time a sound plays, BloodNight picks a pitch and a volume between `min` and
  `max`. That makes it sound less repetitive. Set `min` and `max` to the same number to keep it the same every time.
- `channel` is the sound slider in the player's settings: `AMBIENT`, `HOSTILE`, `MASTER` and so on.

Sounds are named like in the `/playsound` command.

## World names

Messages name the world with `<world>`. At first that is the folder name, like `world_nether`. You can give a world a
nicer name with `displayName` in its world file. It uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html):

```yaml
displayName: "<red>The Nether"
```

Without a `displayName`, BloodNight uses the world's alias from [Multiverse-Core](https://modrinth.com/plugin/multiverse-core)
or Hyperverse, if you have one of them.

In the web editor, set it as **Name players see** on a world's **General** tab. The editor then lists the world by that
name, with the folder name below it. `/bloodnight night` uses it too. Commands you set up keep the folder name for
`{world}`, since other plugins need the real name.
