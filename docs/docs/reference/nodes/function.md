---
title: Chance and cooldown
description: "Chance and cooldown: every node of this group, with its inputs and outputs."
sidebar_position: 2
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Chance and cooldown

## Chance

`function.chance`

True with the given probability in percent

| In | Type | What it is |
|---|---|---|
| Percent | Number | How likely it is true, from 0 to 100 |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True with the given chance |

## Cooldown

`function.cooldown`

True at most once per duration; the first evaluation is true

| In | Type | What it is |
|---|---|---|
| Duration | Number | How long to wait after passing, in the picked unit |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True once the time has passed since it was last true |

## Random Integer

`function.random_integer`

A random integer between lower and upper, both inclusive

| In | Type | What it is |
|---|---|---|
| Lower | Integer | The smallest possible integer |
| Upper | Integer | The largest possible integer |

| Out | Type | What it is |
|---|---|---|
| Result | Integer | A random integer between both, inclusive |

## Random Number

`function.random_number`

A random number from lower, inclusive, to upper, exclusive

| In | Type | What it is |
|---|---|---|
| Lower | Number | The smallest possible number |
| Upper | Number | The largest possible number |

| Out | Type | What it is |
|---|---|---|
| Result | Number | A random number between both |
