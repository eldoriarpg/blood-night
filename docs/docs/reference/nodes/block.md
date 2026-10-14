---
title: Blocks
description: "Blocks: every node of this group, with its inputs and outputs."
sidebar_position: 1
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Blocks

## Mutate Block

`block.mutate`

Changes the properties of a Block that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Block | Block | Represents a block. |
| Biome (optional) | Biome | Sets the biome that this block resides in |
| Block Data (optional) | Block Data | Sets the complete data for this block |
| Type (optional) | Material | Sets the type of this block |

| Out | Type | What it is |
|---|---|---|
| Biome | Biome | Returns the biome that this block resides in |
| Block Data | Block Data | Gets the complete block data for this block |
| Block Indirectly Powered | Boolean | Returns true if the block is being indirectly powered by Redstone. |
| Block Power | Integer | Returns the redstone power being provided to this block. |
| Block Powered | Boolean | Returns true if the block is being powered by Redstone. |
| Buildable | Boolean | Check if this block is solid Determined by Minecraft, typically a block a player can use to place a new block to build things. |
| Burnable | Boolean | Check if this block is burnable Determined by Minecraft, typically a block that fire can destroy (Wool, Wood) |
| Collidable | Boolean | Checks if this block is collidable. |
| Computed Biome | Biome | Gets the computed biome at the location of this Block. |
| Drops | Collection&lt;Item&gt; | Returns a list of items which could drop by destroying this block. |
| Empty | Boolean | Checks if this block is empty. |
| Humidity | Number | Gets the humidity of the biome of this block |
| Light From Blocks | byte | Get the amount of light at this block from nearby blocks. |
| Light From Sky | byte | Get the amount of light at this block from the sky. |
| Light Level | byte | Gets the light level between 0-15 |
| Liquid | Boolean | Checks if this block is liquid. |
| Location | Location | Gets the Location of the block |
| Passable | Boolean | Checks if this block is passable. |
| Piston Move Reaction | PistonMoveReaction | Returns the reaction of the block when moved by a piston |
| Replaceable | Boolean | Checks if this block can be immediately replaced by another block, such as placing a new block in air or tall grass. |
| Solid | Boolean | Check if this block is solid Determined by Minecraft, typically a block a player can stand on and can't be passed through. |
| Suffocating | Boolean | Checks if the block can suffocate. |
| Temperature | Number | Gets the temperature of this block. |
| Type | Material | Gets the type of this block |
| World | World | Gets the world which contains this Block |
| X | Integer | Gets the x-coordinate of this block |
| Y | Integer | Gets the y-coordinate of this block |
| Z | Integer | Gets the z-coordinate of this block |

Goes on with: **Next**

## Read Block

`block.read`

Reads the properties of a Block

| In | Type | What it is |
|---|---|---|
| Block | Block | Represents a block. |

| Out | Type | What it is |
|---|---|---|
| Biome | Biome | Returns the biome that this block resides in |
| Block Data | Block Data | Gets the complete block data for this block |
| Block Indirectly Powered | Boolean | Returns true if the block is being indirectly powered by Redstone. |
| Block Power | Integer | Returns the redstone power being provided to this block. |
| Block Powered | Boolean | Returns true if the block is being powered by Redstone. |
| Buildable | Boolean | Check if this block is solid Determined by Minecraft, typically a block a player can use to place a new block to build things. |
| Burnable | Boolean | Check if this block is burnable Determined by Minecraft, typically a block that fire can destroy (Wool, Wood) |
| Collidable | Boolean | Checks if this block is collidable. |
| Computed Biome | Biome | Gets the computed biome at the location of this Block. |
| Drops | Collection&lt;Item&gt; | Returns a list of items which could drop by destroying this block. |
| Empty | Boolean | Checks if this block is empty. |
| Humidity | Number | Gets the humidity of the biome of this block |
| Light From Blocks | byte | Get the amount of light at this block from nearby blocks. |
| Light From Sky | byte | Get the amount of light at this block from the sky. |
| Light Level | byte | Gets the light level between 0-15 |
| Liquid | Boolean | Checks if this block is liquid. |
| Location | Location | Gets the Location of the block |
| Passable | Boolean | Checks if this block is passable. |
| Piston Move Reaction | PistonMoveReaction | Returns the reaction of the block when moved by a piston |
| Replaceable | Boolean | Checks if this block can be immediately replaced by another block, such as placing a new block in air or tall grass. |
| Solid | Boolean | Check if this block is solid Determined by Minecraft, typically a block a player can stand on and can't be passed through. |
| Suffocating | Boolean | Checks if the block can suffocate. |
| Temperature | Number | Gets the temperature of this block. |
| Type | Material | Gets the type of this block |
| World | World | Gets the world which contains this Block |
| X | Integer | Gets the x-coordinate of this block |
| Y | Integer | Gets the y-coordinate of this block |
| Z | Integer | Gets the z-coordinate of this block |
