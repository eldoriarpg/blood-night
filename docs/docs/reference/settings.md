---
title: Settings
description: Every setting of BloodNight with its default value.
sidebar_position: 1
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Settings

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks
what you type. This page shows the files, so you know what the editor writes for you.
:::

Here you find every setting with its default value. The guide pages explain what they do.

Missing keys are added with their default when the plugin loads the file.

## config.yml

Settings for the whole server.

| Key | Type | Default |
|---|---|---|
| `announcements.prefix` | text | `<dark_red>[<red>BN</red>]</dark_red>` |
| `announcements.reach` | `SERVER`, `WORLD`, `NONE` | `SERVER` |
| `announcements.method` | `CHAT`, `TITLE`, `SUBTITLE`, `ACTION_BAR` | `SUBTITLE` |
| `announcements.personalMethod` | `CHAT`, `TITLE`, `SUBTITLE`, `ACTION_BAR` | `ACTION_BAR` |
| `announcements.fallbackLocale` | text | `en_US` |
| `editor.url` | text | `https://bloodnight.eldoria.de` |
| `editor.timeoutSeconds` | whole number | `30` |

The whole file with its defaults:

```yaml
announcements:
  fallbackLocale: en_US
  method: SUBTITLE
  personalMethod: ACTION_BAR
  prefix: "<dark_red>[<red>BN</red>]</dark_red> "
  reach: SERVER
editor:
  timeoutSeconds: 30
  url: https://bloodnight.eldoria.de
```

## worlds/&lt;world id&gt;.yml

Settings for one world. Every world has its own file.

| Key | Type | Default |
|---|---|---|
| `enabled` | true or false | `true` |
| `nightSelection.type` | `RANDOM`, `MOON_PHASE`, `REAL_MOON_PHASE`, `INTERVAL`, `PHASE`, `CURVE` | `RANDOM` |
| `nightSelection.random.probability` | whole number | `60` |
| `nightSelection.moonPhase.probabilities` | map | `{"FULL_MOON":0,"WANING_GIBBOUS":10,"LAST_QUARTER":20,"WANING_CRESCENT":40,"NEW_MOON":100,"WAXING_CRESCENT":40,"FIRST_QUARTER":20,"WAXING_GIBBOUS":10}` |
| `nightSelection.interval.interval` | whole number | `5` |
| `nightSelection.interval.probability` | whole number | `100` |
| `nightSelection.phase.probabilities` | list of whole number, at most 100 | `[50,50,50]` |
| `nightSelection.curve.min` | whole number | `20` |
| `nightSelection.curve.max` | whole number | `80` |
| `nightSelection.curve.period` | whole number | `10` |
| `nightSettings.begin` | whole number | `13000` |
| `nightSettings.end` | whole number | `23000` |
| `nightSettings.skippable` | true or false | `false` |
| `nightSettings.length.mode` | `NORMAL`, `EXTENDED`, `RANGE` | `NORMAL` |
| `nightSettings.length.seconds` | whole number | `600` |
| `nightSettings.length.maxSeconds` | whole number | `900` |
| `nightSettings.beginCommands` | list of text | `[]` |
| `nightSettings.endCommands` | list of text | `[]` |
| `nightSettings.blockedCommands` | list of text | `[]` |
| `mobSettings.spawning.spawnPercentage` | whole number | `50` |
| `mobSettings.spawning.ignoredReasons` | list of `NATURAL`, `JOCKEY`, `CHUNK_GEN`, `SPAWNER`, `TRIAL_SPAWNER`, `EGG`, `SPAWNER_EGG`, `LIGHTNING`, `BUILD_SNOWMAN`, `BUILD_IRONGOLEM`, `BUILD_COPPERGOLEM`, `BUILD_WITHER`, `VILLAGE_DEFENSE`, `VILLAGE_INVASION`, `BREEDING`, `SLIME_SPLIT`, `REINFORCEMENTS`, `NETHER_PORTAL`, `DISPENSE_EGG`, `INFECTION`, `CURED`, `OCELOT_BABY`, `SILVERFISH_BLOCK`, `MOUNT`, `TRAP`, `ENDER_PEARL`, `SHOULDER_ENTITY`, `DROWNED`, `SHEARED`, `EXPLOSION`, `RAID`, `PATROL`, `BEEHIVE`, `PIGLIN_ZOMBIFIED`, `SPELL`, `FROZEN`, `METAMORPHOSIS`, `DUPLICATION`, `COMMAND`, `ENCHANTMENT`, `OMINOUS_ITEM_SPAWNER`, `BUCKET`, `POTION_EFFECT`, `REANIMATE`, `REHYDRATION`, `CUSTOM`, `DEFAULT` | `[]` |
| `mobSettings.spawning.mobs` | map | `{}` |
| `mobSettings.creepers.blockDamage` | true or false | `false` |
| `mobSettings.creepers.always` | true or false | `false` |
| `mobSettings.forcePhantoms` | true or false | `true` |
| `mobSettings.displayNames` | true or false | `true` |
| `mobSettings.damageModifier` | number | `2` |
| `mobSettings.healthMultiplier` | number | `2` |
| `announcement.bossBar.enabled` | true or false | `true` |
| `announcement.bossBar.title` | text | `<red><bold><lang:bloodnight.bossbar.title>` |
| `announcement.bossBar.color` | `PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE`, `WHITE` | `RED` |
| `announcement.bossBar.overlay` | `PROGRESS`, `NOTCHED_6`, `NOTCHED_10`, `NOTCHED_12`, `NOTCHED_20` | `PROGRESS` |
| `announcement.bossBar.flags` | list of `DARKEN_SCREEN`, `PLAY_BOSS_MUSIC`, `CREATE_WORLD_FOG` | `["DARKEN_SCREEN","CREATE_WORLD_FOG"]` |
| `announcement.effect.enabled` | true or false | `true` |
| `announcement.effect.effect` | text | `minecraft:darkness` |
| `announcement.effect.seconds` | whole number | `5` |
| `announcement.joinWarning` | true or false | `true` |
| `soundSettings.channel` | `MASTER`, `MUSIC`, `RECORD`, `WEATHER`, `BLOCK`, `HOSTILE`, `NEUTRAL`, `PLAYER`, `AMBIENT`, `VOICE`, `UI` | `AMBIENT` |
| `soundSettings.minInterval` | whole number | `10` |
| `soundSettings.maxInterval` | whole number | `40` |
| `soundSettings.begin` | list of map | `[{"pitches":[0.8,1],"sound":"minecraft:entity.lightning_bolt.thunder","volumes":[1]}]` |
| `soundSettings.end` | list of map | `[{"pitches":[0.8,1],"sound":"minecraft:entity.lightning_bolt.thunder","volumes":[1]}]` |
| `soundSettings.ambience` | list of map | `[{"pitches":[0.4,0.6,0.8,1,1.2,1.4,1.6],"sound":"minecraft:ambient.basalt_deltas.additions","volumes":[0.2,0.4,0.6,0.8,1]},{"pitches":[0.4,0.6,0.8,1,1.2,1.4,1.6],"sound":"minecraft:ambient.crimson_forest.additions","volumes":[0.2,0.4,0.6,0.8,1]},{"pitches":[0.4,0.6,0.8,1,1.2,1.4,1.6],"sound":"minecraft:ambient.nether_wastes.additions","volumes":[0.2,0.4,0.6,0.8,1]},{"pitches":[0.4,0.6,0.8,1,1.2,1.4,1.6],"sound":"minecraft:ambient.soul_sand_valley.additions","volumes":[0.2,0.4,0.6,0.8,1]}]` |
| `deathActions.player.lightning.lightning` | true or false | `true` |
| `deathActions.player.lightning.lightningChance` | whole number | `100` |
| `deathActions.player.lightning.thunder` | true or false | `true` |
| `deathActions.player.lightning.thunderChance` | whole number | `100` |
| `deathActions.player.shockwave.chance` | whole number | `10` |
| `deathActions.player.shockwave.power` | number | `10` |
| `deathActions.player.shockwave.range` | number | `10` |
| `deathActions.player.shockwave.minSeconds` | number | `0.1` |
| `deathActions.player.shockwave.effects` | list of map | `[{"effect":"minecraft:nausea","seconds":5}]` |
| `deathActions.player.loseInventoryChance` | whole number | `0` |
| `deathActions.player.loseExperienceChance` | whole number | `0` |
| `deathActions.player.deathCommands` | list of text | `[]` |
| `deathActions.player.respawnCommands` | list of text | `[]` |
| `deathActions.player.respawnEffects` | list of map | `[{"effect":"minecraft:nausea","seconds":5}]` |
| `deathActions.mob.lightning.lightning` | true or false | `true` |
| `deathActions.mob.lightning.lightningChance` | whole number | `100` |
| `deathActions.mob.lightning.thunder` | true or false | `true` |
| `deathActions.mob.lightning.thunderChance` | whole number | `100` |
| `deathActions.mob.shockwave.chance` | whole number | `10` |
| `deathActions.mob.shockwave.power` | number | `10` |
| `deathActions.mob.shockwave.range` | number | `10` |
| `deathActions.mob.shockwave.minSeconds` | number | `0.1` |
| `deathActions.mob.shockwave.effects` | list of map | `[{"effect":"minecraft:nausea","seconds":5}]` |
| `worldDrops.weightsScope` | `GLOBAL`, `WORLD`, `MOB` |  |
| `worldDrops.weights` | map |  |
| `worldDrops.dropTables` | list of `GLOBAL`, `WORLD`, `MOB` |  |
| `worldDrops.dropList` | list of map |  |
| `worldDrops.xpScope` | `GLOBAL`, `WORLD`, `MOB` |  |
| `worldDrops.xpModifier` | `DEFAULT`, `MULTIPLY`, `VALUE` |  |
| `worldDrops.xpValue` | number |  |

The whole file with its defaults:

```yaml
enabled: true
nightSelection:
  curve:
    max: 80
    min: 20
    period: 10
  interval:
    interval: 5
    probability: 100
  moonPhase:
    probabilities:
      FULL_MOON: 0
      WANING_GIBBOUS: 10
      LAST_QUARTER: 20
      WANING_CRESCENT: 40
      NEW_MOON: 100
      WAXING_CRESCENT: 40
      FIRST_QUARTER: 20
      WAXING_GIBBOUS: 10
  phase:
    probabilities:
      - 50
      - 50
      - 50
  random:
    probability: 60
  type: RANDOM
nightSettings:
  begin: 13000
  beginCommands: []
  blockedCommands: []
  end: 23000
  endCommands: []
  length:
    maxSeconds: 900
    mode: NORMAL
    seconds: 600
  skippable: false
mobSettings:
  creepers:
    always: false
    blockDamage: false
  damageModifier: 2
  displayNames: true
  forcePhantoms: true
  healthMultiplier: 2
  spawning:
    ignoredReasons: []
    mobs: {}
    spawnPercentage: 50
announcement:
  bossBar:
    color: RED
    enabled: true
    flags:
      - DARKEN_SCREEN
      - CREATE_WORLD_FOG
    overlay: PROGRESS
    title: <red><bold><lang:bloodnight.bossbar.title>
  effect:
    effect: minecraft:darkness
    enabled: true
    seconds: 5
  joinWarning: true
soundSettings:
  ambience:
    - pitches:
        - 0.4
        - 0.6
        - 0.8
        - 1
        - 1.2
        - 1.4
        - 1.6
      sound: minecraft:ambient.basalt_deltas.additions
      volumes:
        - 0.2
        - 0.4
        - 0.6
        - 0.8
        - 1
    - pitches:
        - 0.4
        - 0.6
        - 0.8
        - 1
        - 1.2
        - 1.4
        - 1.6
      sound: minecraft:ambient.crimson_forest.additions
      volumes:
        - 0.2
        - 0.4
        - 0.6
        - 0.8
        - 1
    - pitches:
        - 0.4
        - 0.6
        - 0.8
        - 1
        - 1.2
        - 1.4
        - 1.6
      sound: minecraft:ambient.nether_wastes.additions
      volumes:
        - 0.2
        - 0.4
        - 0.6
        - 0.8
        - 1
    - pitches:
        - 0.4
        - 0.6
        - 0.8
        - 1
        - 1.2
        - 1.4
        - 1.6
      sound: minecraft:ambient.soul_sand_valley.additions
      volumes:
        - 0.2
        - 0.4
        - 0.6
        - 0.8
        - 1
  begin:
    - pitches:
        - 0.8
        - 1
      sound: minecraft:entity.lightning_bolt.thunder
      volumes:
        - 1
  channel: AMBIENT
  end:
    - pitches:
        - 0.8
        - 1
      sound: minecraft:entity.lightning_bolt.thunder
      volumes:
        - 1
  maxInterval: 40
  minInterval: 10
deathActions:
  mob:
    lightning:
      lightning: true
      lightningChance: 100
      thunder: true
      thunderChance: 100
    shockwave:
      chance: 10
      effects:
        - effect: minecraft:nausea
          seconds: 5
      minSeconds: 0.1
      power: 10
      range: 10
  player:
    deathCommands: []
    lightning:
      lightning: true
      lightningChance: 100
      thunder: true
      thunderChance: 100
    loseExperienceChance: 0
    loseInventoryChance: 0
    respawnCommands: []
    respawnEffects:
      - effect: minecraft:nausea
        seconds: 5
    shockwave:
      chance: 10
      effects:
        - effect: minecraft:nausea
          seconds: 5
      minSeconds: 0.1
      power: 10
      range: 10
worldDrops:
  weightsScope: GLOBAL
  weights:
    "1": 90
    "2": 50
    "3": 10
  dropTables:
    - GLOBAL
    - WORLD
  dropList: []
  xpScope: GLOBAL
  xpModifier: MULTIPLY
  xpValue: 4
```

## global_drops.yml

Drops every world can use.

| Key | Type | Default |
|---|---|---|
| `weights` | map |  |
| `drops` | list of map |  |
| `xpModifier` | `DEFAULT`, `MULTIPLY`, `VALUE` |  |
| `xpValue` | number |  |

The whole file with its defaults:

```yaml
{}
```
