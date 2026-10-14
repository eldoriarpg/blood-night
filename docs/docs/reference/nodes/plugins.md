---
title: plugins
description: "plugins: every node of this group, with its inputs and outputs."
sidebar_position: 15
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# plugins

## Balance

`economy.balance`

How much money a player has in Vault's economy. Needs Vault

| In | Type | What it is |
|---|---|---|
| Player | Player | The player |

| Out | Type | What it is |
|---|---|---|
| Result | Number | Their money; 0 without Vault |

## Cast Mythic Skill

`mythic.cast_skill`

Lets an entity cast a MythicMobs skill by its name. Needs MythicMobs

| In | Type | What it is |
|---|---|---|
| Caster | Entity | Who casts it, usually the mob itself |
| Skill | Text | The skill's name in MythicMobs |

Goes on with: **Next**

## Fill In Placeholders

`placeholder.parse`

Fills PlaceholderAPI placeholders such as %player_name% into a text. Needs PlaceholderAPI

| In | Type | What it is |
|---|---|---|
| Text | Text | The text with placeholders |
| Player (optional) | Player | The player the placeholders are about; leave it empty when there is none |

| Out | Type | What it is |
|---|---|---|
| Result | Text | The text with the placeholders filled in |

## Give Money

`economy.deposit`

Gives a player money through Vault. Needs Vault and an economy plugin

| In | Type | What it is |
|---|---|---|
| Player | Player | The player |
| Amount | Number | How much money |

Goes on with: **Next**

## Spawn Mythic Mob

`mythic.spawn`

Spawns a MythicMobs mob by its name. Needs MythicMobs

| In | Type | What it is |
|---|---|---|
| Type | Text | The mob's name in MythicMobs |
| Location | Location | Where it spawns |
| Level (optional) | Integer | Its level; 1 when not given |

Goes on with: **Next**

## Take Money

`economy.withdraw`

Takes money from a player through Vault. Needs Vault and an economy plugin

| In | Type | What it is |
|---|---|---|
| Player | Player | The player |
| Amount | Number | How much money |

Goes on with: **Next**
