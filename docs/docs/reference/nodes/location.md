---
title: Locations
description: "Locations: every node of this group, with its inputs and outputs."
sidebar_position: 9
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Locations

## Create Location

`location.create`

A location in a world; yaw and pitch default to 0

| In | Type | What it is |
|---|---|---|
| World | World | The world it lies in |
| X | Number | East-west position |
| Y | Number | Height |
| Z | Number | North-south position |
| Yaw (optional) | Number | Facing around the vertical axis, in degrees; 0 when not given |
| Pitch (optional) | Number | Facing up or down, in degrees; 0 when not given |

| Out | Type | What it is |
|---|---|---|
| Result | Location | The location |

## Distance Between Locations

`location.distance`

Get the distance between this location and another.

| In | Type | What it is |
|---|---|---|
| Location | Location | Represents a 3-dimensional position in a world. |
| Other | Location | The other location |

| Out | Type | What it is |
|---|---|---|
| Result | Number | the distance |

## Mutate Location

`location.mutate`

Changes the properties of a Location that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Location | Location | Represents a 3-dimensional position in a world. |
| Direction (optional) | Vector | Sets the yaw and pitch to point in the direction of the vector. |
| Pitch (optional) | Float | Sets the pitch of this location, measured in degrees. |
| World (optional) | World | Sets the world that this location resides in |
| X (optional) | Number | Sets the x-coordinate of this location |
| Y (optional) | Number | Sets the y-coordinate of this location |
| Yaw (optional) | Float | Sets the yaw of this location, measured in degrees. |
| Z (optional) | Number | Sets the z-coordinate of this location |

| Out | Type | What it is |
|---|---|---|
| Block | Block | Gets the block at the represented location |
| Block X | Integer | Gets the floored value of the X component, indicating the block that this location is contained with. |
| Block Y | Integer | Gets the floored value of the Y component, indicating the block that this location is contained with. |
| Block Z | Integer | Gets the floored value of the Z component, indicating the block that this location is contained with. |
| Chunk Loaded | Boolean |  |
| Direction | Vector | Gets a unit-vector pointing in the direction that this Location is facing. |
| Fine | Boolean | Checks if this position represents a FinePosition. |
| Finite | Boolean | Checks if each component of this position is finite. |
| Generated | Boolean | Checks if a Chunk has been generated at this location. |
| Pitch | Float | Gets the pitch of this location, measured in degrees. |
| World | World | Gets the world that this location resides in |
| World Loaded | Boolean | Checks if world in this location is present and loaded. |
| X | Number | Gets the x-coordinate of this location |
| Y | Number | Gets the y-coordinate of this location |
| Yaw | Float | Gets the yaw of this location, measured in degrees. |
| Z | Number | Gets the z-coordinate of this location |

Goes on with: **Next**

## Read Location

`location.read`

Reads the properties of a Location

| In | Type | What it is |
|---|---|---|
| Location | Location | Represents a 3-dimensional position in a world. |

| Out | Type | What it is |
|---|---|---|
| Block | Block | Gets the block at the represented location |
| Block X | Integer | Gets the floored value of the X component, indicating the block that this location is contained with. |
| Block Y | Integer | Gets the floored value of the Y component, indicating the block that this location is contained with. |
| Block Z | Integer | Gets the floored value of the Z component, indicating the block that this location is contained with. |
| Chunk Loaded | Boolean |  |
| Direction | Vector | Gets a unit-vector pointing in the direction that this Location is facing. |
| Fine | Boolean | Checks if this position represents a FinePosition. |
| Finite | Boolean | Checks if each component of this position is finite. |
| Generated | Boolean | Checks if a Chunk has been generated at this location. |
| Pitch | Float | Gets the pitch of this location, measured in degrees. |
| World | World | Gets the world that this location resides in |
| World Loaded | Boolean | Checks if world in this location is present and loaded. |
| X | Number | Gets the x-coordinate of this location |
| Y | Number | Gets the y-coordinate of this location |
| Yaw | Float | Gets the yaw of this location, measured in degrees. |
| Z | Number | Gets the z-coordinate of this location |
