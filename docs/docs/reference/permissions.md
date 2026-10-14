---
title: Permissions
description: Every permission of BloodNight and what it allows.
sidebar_position: 2
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Permissions

Only operators have these permissions at first. Give them to other players with your permissions plugin.

| Permission | What it allows | Includes |
|---|---|---|
| `bloodnight.admin` | Every blood night command | `bloodnight.command.reload`, `bloodnight.command.spawn`, `bloodnight.command.problems`, `bloodnight.command.source`, `bloodnight.command.debug`, `bloodnight.command.export`, `bloodnight.command.night`, `bloodnight.command.editor`, `bloodnight.command.item`, `bloodnight.command.settings`, `bloodnight.command.night.manage` |
| `bloodnight.bypass.commands` | Use commands blocked during blood nights |  |
| `bloodnight.command.debug` | /bloodnight debug, fires the debug trigger of mobs |  |
| `bloodnight.command.editor` | /bloodnight editor, opens, applies or closes a web editor session |  |
| `bloodnight.command.export` | /bloodnight export, writes the editor payload and catalog to the debug folder |  |
| `bloodnight.command.item` | /bloodnight item, registers the held item or removes a registered item |  |
| `bloodnight.command.night` | /bloodnight night, shows the night selection and the next blood night of a world |  |
| `bloodnight.command.night.manage` | /bloodnight night force and cancel, decides the coming night of a world |  |
| `bloodnight.command.problems` | /bloodnight problems, lists compile problems and runtime failures of mobs |  |
| `bloodnight.command.reload` | /bloodnight reload, reads the configuration again and recompiles every mob |  |
| `bloodnight.command.settings` | /bloodnight settings, shows or changes one setting without the web editor |  |
| `bloodnight.command.source` | /bloodnight source, writes the generated code of a mob to the debug folder |  |
| `bloodnight.command.spawn` | /bloodnight spawn, spawns a custom mob |  |
| `bloodnight.updates` | Hear about new BloodNight versions on join |  |
