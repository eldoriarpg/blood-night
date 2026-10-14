---
title: Commands
sidebar_position: 7
---

# Commands

All commands start with `/bloodnight`. The short form is `/bn`.

| Command | What it does | Permission |
|---|---|---|
| `/bn` | Shows the version and the commands you may use | |
| `/bn night [world]` | Shows how the world picks blood nights, the chance for the coming night and the next blood night | `bloodnight.command.night` |
| `/bn night force [world]` | Makes the coming night a blood night. Starts it now if it is night already. | `bloodnight.command.night.manage` |
| `/bn night cancel [world]` | Keeps the coming night normal. Ends a running blood night. | `bloodnight.command.night.manage` |
| `/bn spawn <mob>` | Spawns a special mob where you stand | `bloodnight.command.spawn` |
| `/bn editor` | Opens the web editor or joins the open session | `bloodnight.command.editor` |
| `/bn editor new` | Starts a fresh editor session | `bloodnight.command.editor` |
| `/bn editor apply [force]` | Applies what you made in the editor | `bloodnight.command.editor` |
| `/bn editor cancel` | Closes the editor session | `bloodnight.command.editor` |
| `/bn item register` | Saves the item in your hand and gives it an ID | `bloodnight.command.item` |
| `/bn item remove <id>` | Removes a saved item | `bloodnight.command.item` |
| `/bn reload` | Reads all files again and builds every mob | `bloodnight.command.reload` |
| `/bn problems [mob]` | Lists problems of mobs | `bloodnight.command.problems` |
| `/bn debug [mob]` | Fires the debug trigger | `bloodnight.command.debug` |
| `/bn source <mob>` | Writes a mob's Java code to the `debug` folder | `bloodnight.command.source` |
| `/bn export` | Writes what the editor gets to the `debug` folder | `bloodnight.command.export` |

Replies come in the player's language. English and German are there so far.

Without `[world]`, the command uses your own world. From the console it uses the first world.

`bloodnight.admin` gives all command permissions at once. All of them are for operators by default. The full list is
on the [permissions page](./reference/permissions.md).
