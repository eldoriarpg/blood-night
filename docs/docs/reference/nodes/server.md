---
title: server
description: "server: every node of this group, with its inputs and outputs."
sidebar_position: 18
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# server

## Broadcast

`server.broadcast`

Sends a message to the players in the mob's world or on the whole server

| In | Type | What it is |
|---|---|---|
| Text | Text | The message, MiniMessage such as &lt;red&gt;Run! |

Goes on with: **Next**

## Run Command

`server.run_command`

Runs a command as the console, e.g. to use another plugin. \{player\} is the player's name, \{world\}, \{x\}, \{y\} and \{z\} where the mob stands

| In | Type | What it is |
|---|---|---|
| Command | Text | The command, without the slash |
| Player (optional) | Player | The player \{player\} stands for; leave it empty when there is none |

Goes on with: **Next**
