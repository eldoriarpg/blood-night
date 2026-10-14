---
title: Death actions
sidebar_position: 3
---

# Death actions

Things that happen when someone dies during a blood night. They live in `deathActions` in the world file.

- `deathActions.mob` is for every monster that dies.
- `deathActions.player` is for players.

## Lightning

```yaml
lightning:
  lightning: true
  lightningChance: 100
  thunder: true
  thunderChance: 100
```

With `lightningChance` percent, a lightning bolt strikes above the place of death. It looks real but does not hurt
anyone or set fires. If no lightning strikes, players in the world may hear thunder instead, with `thunderChance`
percent.

## Shockwave

```yaml
shockwave:
  chance: 10
  power: 10
  range: 10
  minSeconds: 0.1
  effects:
    - effect: minecraft:nausea
      seconds: 5
```

With `chance` percent, a shockwave pushes everything nearby away. Everyone within `range` blocks gets the `effects`.
Close to the center, effects last longer. Far away, they last shorter, but at least `minSeconds`.

## Only for players

```yaml
player:
  loseInventoryChance: 0
  loseExperienceChance: 0
  deathCommands: []
  respawnCommands: []
  respawnEffects:
    - effect: minecraft:nausea
      seconds: 5
```

| Key | What it does |
|---|---|
| `loseInventoryChance` | Chance in percent that the player's items are gone. They don't drop. |
| `loseExperienceChance` | Chance in percent that the player's experience is gone. |
| `deathCommands` | Commands the console runs when the player dies. |
| `respawnCommands` | Commands the console runs when the player comes back. |
| `respawnEffects` | Potion effects the player gets when they come back, if the blood night still runs. |

In commands, `{player}` becomes the player's name and `{world}` the world's name.

Monsters from [ignored spawn reasons](../mobs/spawning.md#world-settings) don't trigger death actions.
