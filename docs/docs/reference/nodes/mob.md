---
title: The mob
description: "The mob: every node of this group, with its inputs and outputs."
sidebar_position: 19
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# The mob

## On Night End

`mob.night_end`

Runs once when the night ends in the mob's world

Goes on with: **Next**

## On Spawn

`mob.spawn`

Runs once when the mob has spawned, after its equipment and extension are set up

Goes on with: **Next**

## On Tick

`mob.tick`

Runs every server tick

Goes on with: **Next**

## Change Movement

`mob.movement`

Switches how this mob moves, e.g. to flee once it is hurt; its movement numbers stay

Goes on with: **Next**

## Extension

`mob.extension`

The entity riding or carrying the mob; missing when it has none

| Out | Type | What it is |
|---|---|---|
| Entity | the extension | The entity riding or carrying the mob, as its own entity type; missing when it has none |

## Is Moving

`mob.has_path`

Whether a mob is walking along a path right now

| In | Type | What it is |
|---|---|---|
| Mob | Mob | The mob |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | Whether it has a path it follows |

## Move To

`mob.move_to`

Lets a mob walk to a location along a path it finds; nothing happens without one

| In | Type | What it is |
|---|---|---|
| Mob | Mob | The mob that walks |
| Location | Location | Where it walks to |
| Speed (optional) | Number | How fast, 1 for its own speed; 1 when not given |

| Out | Type | What it is |
|---|---|---|
| Found | Boolean | Whether it found a path |

Goes on with: **Next**

## Self

`mob.self`

The mob this behaviour belongs to, as its own entity type, e.g. a Creeper

| Out | Type | What it is |
|---|---|---|
| Entity | the mob | The mob this behaviour belongs to |

## Stop Moving

`mob.stop_moving`

Lets a mob stop walking where it stands; its AI may soon walk on

| In | Type | What it is |
|---|---|---|
| Mob | Mob | The mob that stops |

Goes on with: **Next**
