---
title: The mob
sidebar_position: 17
---

<!-- Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. -->

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

## Extension

`mob.extension`

The entity riding or carrying the mob; missing when it has none

| Out | Type | What it is |
|---|---|---|
| Entity | the extension | The entity riding or carrying the mob, as its own entity type; missing when it has none |

## Self

`mob.self`

The mob this behaviour belongs to, as its own entity type, e.g. a Creeper

| Out | Type | What it is |
|---|---|---|
| Entity | the mob | The mob this behaviour belongs to |
