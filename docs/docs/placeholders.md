---
title: Placeholders
sidebar_position: 8
---

# Placeholders

Show blood nights on your scoreboard, in the tab list or in chat. BloodNight works with
[PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) and
[MiniPlaceholders](https://modrinth.com/plugin/miniplaceholders). Install one of them, and the placeholders are there.

## The placeholders

| PlaceholderAPI | MiniPlaceholders | What it shows |
|---|---|---|
| `%bloodnight_active%` | `<bloodnight_active>` | `true` while a blood night runs |
| `%bloodnight_probability%` | `<bloodnight_probability>` | The chance of the coming night, 0 to 100 |
| `%bloodnight_probability_2%` | `<bloodnight_probability:2>` | The chance two nights after the coming one |
| `%bloodnight_time_left%` | `<bloodnight_time_left>` | Time left of the running blood night, like `0:05:12` |
| `%bloodnight_seconds_left%` | `<bloodnight_seconds_left>` | The same in seconds |
| `%bloodnight_percent_left%` | `<bloodnight_percent_left>` | How much of the night is left, like `42.5` |
| `%bloodnight_next_nights%` | `<bloodnight_next_nights>` | Nights until the next blood night, `0` for the coming one |
| `%bloodnight_next_time%` | `<bloodnight_next_time>` | Time until the next blood night |

Without a world, a placeholder uses the world of the player who sees it. To ask about another world, add its name at
the end:

- PlaceholderAPI: `%bloodnight_active_world_nether%`, `%bloodnight_probability_2_world_nether%`
- MiniPlaceholders: `<bloodnight_active:world_nether>`, `<bloodnight_probability:2:world_nether>`

## Keep the next blood night a secret

BloodNight decides blood nights ahead of time. So it could tell your players exactly when the next one comes. Many
servers want a surprise, so `next_nights` and `next_time` stay empty at first. They only show nights an admin
forced.

To show them, set this in `config.yml`:

```yaml
placeholders:
  revealNext: true
```

## Placeholders in commands

With PlaceholderAPI, you can also use its placeholders in BloodNight's commands: the begin and end commands of a
night, and the death and respawn commands. In a command with `{player}`, the placeholders are filled in for that
player.

```yaml
nightSettings:
  beginCommands:
    - "broadcast %bloodnight_probability% percent of the nights here are bloody"
```
