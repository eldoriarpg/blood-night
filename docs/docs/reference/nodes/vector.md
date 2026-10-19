---
title: Vectors
description: "Vectors: every node of this group, with its inputs and outputs."
sidebar_position: 26
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Vectors

## Create Vector

`vector.create`

Construct the vector with provided double components.

| In | Type | What it is |
|---|---|---|
| X | Number | X component |
| Y | Number | Y component |
| Z | Number | Z component |

| Out | Type | What it is |
|---|---|---|
| Vector | Vector | Represents a mutable vector. |

## Direction Between Points

`vector.direction`

The vector pointing from one point to another

| In | Type | What it is |
|---|---|---|
| From | Vector | Where it starts |
| To | Vector | Where it points |

| Out | Type | What it is |
|---|---|---|
| Result | Vector | The vector from start to end |

## Distance Between Points

`vector.distance`

Distance between two points. Uses a square root; prefer Distance Squared for comparisons

| In | Type | What it is |
|---|---|---|
| First | Vector | A point |
| Second | Vector | Another point |

| Out | Type | What it is |
|---|---|---|
| Result | Number | The distance between both |

## Dot Product

`vector.dot`

How much two directions agree: with both of length 1, 1 when they point the same way, 0 when at a right angle, -1 when opposite. Good for checking whether someone looks at something

| In | Type | What it is |
|---|---|---|
| First | Vector | A vector |
| Second | Vector | Another vector |

| Out | Type | What it is |
|---|---|---|
| Result | Number | The dot product of both |

## Mutate Vector

`vector.mutate`

Changes the properties of a Vector that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Vector | Vector | Represents a mutable vector. |
| X (optional) | Number | Set the X component. |
| Y (optional) | Number | Set the Y component. |
| Z (optional) | Number | Set the Z component. |

| Out | Type | What it is |
|---|---|---|
| Block X | Integer | Gets the floored value of the X component, indicating the block that this vector is contained with. |
| Block Y | Integer | Gets the floored value of the Y component, indicating the block that this vector is contained with. |
| Block Z | Integer | Gets the floored value of the Z component, indicating the block that this vector is contained with. |
| Normalized | Boolean | Returns if a vector is normalized |
| X | Number | Gets the X component. |
| Y | Number | Gets the Y component. |
| Z | Number | Gets the Z component. |
| Zero | Boolean | Check whether or not each component of this vector is equal to 0. |

Goes on with: **Next**

## Normalize Vector

`vector.normalize`

A vector of length 1 pointing the same way

| In | Type | What it is |
|---|---|---|
| Value | Vector | A vector |

| Out | Type | What it is |
|---|---|---|
| Result | Vector | The same direction with length 1 |

## Read Vector

`vector.read`

Reads the properties of a Vector

| In | Type | What it is |
|---|---|---|
| Vector | Vector | Represents a mutable vector. |

| Out | Type | What it is |
|---|---|---|
| Block X | Integer | Gets the floored value of the X component, indicating the block that this vector is contained with. |
| Block Y | Integer | Gets the floored value of the Y component, indicating the block that this vector is contained with. |
| Block Z | Integer | Gets the floored value of the Z component, indicating the block that this vector is contained with. |
| Normalized | Boolean | Returns if a vector is normalized |
| X | Number | Gets the X component. |
| Y | Number | Gets the Y component. |
| Z | Number | Gets the Z component. |
| Zero | Boolean | Check whether or not each component of this vector is equal to 0. |

## Squared Distance Between Points

`vector.distance_squared`

Squared distance between two points

| In | Type | What it is |
|---|---|---|
| First | Vector | A point |
| Second | Vector | Another point |

| Out | Type | What it is |
|---|---|---|
| Result | Number | The distance between both, squared; cheaper when only comparing |
