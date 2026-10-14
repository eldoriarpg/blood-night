---
title: Values
sidebar_position: 21
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Values

## Item By Id

`value.item`

A copy of an item from the item registry; missing when the id is unknown

| Out | Type | What it is |
|---|---|---|
| Value | Item | A copy of the item; missing when the id is unknown |

## Mutate Color

`color.mutate`

Changes the properties of a Color that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Color | Color | A container for a color palette. |
| Alpha (optional) | Integer | Creates a new Color object with specified component |
| Blue (optional) | Integer | Creates a new Color object with specified component |
| Green (optional) | Integer | Creates a new Color object with specified component |
| Red (optional) | Integer | Creates a new Color object with specified component |

| Out | Type | What it is |
|---|---|---|
| Alpha | Integer | Gets the alpha component |
| Blue | Integer | Gets the blue component |
| Green | Integer | Gets the green component |
| Red | Integer | Gets the red component |

Goes on with: **Next**

## Read Color

`color.read`

Reads the properties of a Color

| In | Type | What it is |
|---|---|---|
| Color | Color | A container for a color palette. |

| Out | Type | What it is |
|---|---|---|
| Alpha | Integer | Gets the alpha component |
| Blue | Integer | Gets the blue component |
| Green | Integer | Gets the green component |
| Red | Integer | Gets the red component |

## Rgb Color

`color.rgb`

Creates a new Color object from a red, green, and blue

| In | Type | What it is |
|---|---|---|
| Red | Integer | integer from 0-255 |
| Green | Integer | integer from 0-255 |
| Blue | Integer | integer from 0-255 |

| Out | Type | What it is |
|---|---|---|
| Color | Color | a new Color object for the red, green, blue |

## Value

`value`

A fixed value of the picked type

| Out | Type | What it is |
|---|---|---|
| Value | any type | The value typed in |
