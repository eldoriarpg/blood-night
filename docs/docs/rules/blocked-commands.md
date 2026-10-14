---
title: Blocked commands
sidebar_position: 2
---

# Blocked commands

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

You can stop players from using some commands during a blood night. No running home!

```yaml
nightSettings:
  blockedCommands:
    - home
    - spawn
    - tpa
    - warp hub
```

- Write the command without `/`.
- BloodNight also blocks other names of the same command. If `home` is blocked, `/essentials:home` and aliases are
  blocked too.
- An entry with more words only blocks that use. `warp hub` blocks `/warp hub`, but `/warp mine` still works.
- It only counts for players in a world where a blood night runs.

Players with the permission `bloodnight.bypass.commands` can still use every command.
