---
title: Logic
description: "Logic: every node of this group, with its inputs and outputs."
sidebar_position: 10
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Logic

## And

`logic.and`

True when both are true

| In | Type | What it is |
|---|---|---|
| First | Boolean | A condition |
| Second | Boolean | Another condition |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True when both are true |

## As

`cast`

The value as the picked type, missing when it is none; matches tells which

| In | Type | What it is |
|---|---|---|
| Value | Object | Any value |

| Out | Type | What it is |
|---|---|---|
| Result | any type | The value as the picked type; missing when it is none |
| Matches | Boolean | Whether the value is of the picked type |

## Equal

`logic.equal`

True when both values are equal; two missing values are equal

| In | Type | What it is |
|---|---|---|
| First | Object | Any value |
| Second | Object | Any value to compare with |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | Whether both are equal |

## Greater

`logic.greater`

True when the first number is greater

| In | Type | What it is |
|---|---|---|
| First | Number | The first number |
| Second | Number | The second number |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True when the first number is greater |

## Greater Or Equal

`logic.greater_or_equal`

True when the first number is greater or equal

| In | Type | What it is |
|---|---|---|
| First | Number | The first number |
| Second | Number | The second number |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True when the first number is greater or equal |

## Is Null

`logic.is_null`

True when the value is missing

| In | Type | What it is |
|---|---|---|
| Value | Object | Any value |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True when the value is missing |

## Less

`logic.less`

True when the first number is less

| In | Type | What it is |
|---|---|---|
| First | Number | The first number |
| Second | Number | The second number |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True when the first number is less |

## Less Or Equal

`logic.less_or_equal`

True when the first number is less or equal

| In | Type | What it is |
|---|---|---|
| First | Number | The first number |
| Second | Number | The second number |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True when the first number is less or equal |

## Not

`logic.not`

Inverts a boolean

| In | Type | What it is |
|---|---|---|
| Value | Boolean | A condition |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | Inverts a boolean |

## Or

`logic.or`

True when at least one is true

| In | Type | What it is |
|---|---|---|
| First | Boolean | A condition |
| Second | Boolean | Another condition |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True when at least one is true |

## Select

`select`

One of several values, picked by a key; only the picked one is computed

| In | Type | What it is |
|---|---|---|
| Key | any type | Picks the option with the same key |
| Option 1 (optional) | any type |  |
| Option 2 (optional) | any type |  |
| Option 3 (optional) | any type |  |
| Option 4 (optional) | any type |  |
| Option 5 (optional) | any type |  |
| Option 6 (optional) | any type |  |
| Option 7 (optional) | any type |  |
| Option 8 (optional) | any type |  |
| Default (optional) | any type | Given when no option has the key |

| Out | Type | What it is |
|---|---|---|
| Value | any type | The picked option |
