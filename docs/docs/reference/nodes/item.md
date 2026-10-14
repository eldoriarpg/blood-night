---
title: Items
description: "Items: every node of this group, with its inputs and outputs."
sidebar_position: 6
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Items

## Create Item Stack

`item_type.create_item_stack`

Constructs a new itemstack with this item type that has the amount 1.

| In | Type | What it is |
|---|---|---|
| Item Type | Item Type | Represents an item type. |

| Out | Type | What it is |
|---|---|---|
| Result | Item | the constructed item stack. |

## Mutate Item

`item.mutate`

Changes the properties of a Item that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Item | Item | Represents a stack of items. |
| Amount (optional) | Integer | Sets the amount of items in this stack |

| Out | Type | What it is |
|---|---|---|
| Amount | Integer | Gets the amount of items in this stack |
| Empty | Boolean | Returns whether this item stack is empty and contains no item. |
| Item Flags | Set&lt;ItemFlag&gt; | Get current set itemFlags. |
| Max Stack Size | Integer | Get the maximum stack size for this item. |
| Type | Material | Gets the type of this item |

Goes on with: **Next**

## Read Item

`item.read`

Reads the properties of a Item

| In | Type | What it is |
|---|---|---|
| Item | Item | Represents a stack of items. |

| Out | Type | What it is |
|---|---|---|
| Amount | Integer | Gets the amount of items in this stack |
| Empty | Boolean | Returns whether this item stack is empty and contains no item. |
| Item Flags | Set&lt;ItemFlag&gt; | Get current set itemFlags. |
| Max Stack Size | Integer | Get the maximum stack size for this item. |
| Type | Material | Gets the type of this item |

## Read Item Type

`item_type.read`

Reads the properties of a Item Type

| In | Type | What it is |
|---|---|---|
| Item Type | Item Type | Represents an item type. |

| Out | Type | What it is |
|---|---|---|
| Block Type | Block Type | Returns the corresponding BlockType for the given ItemType. |
| Burn Duration | Integer | Retrieve the item's burn duration in a Furnace |
| Compost Chance | Float | Get the chance that this item type will successfully compost. |
| Compostable | Boolean | Checks whether this item type is compostable (can be inserted into a composter). |
| Crafting Remaining Item | Item Type | Determines the remaining item in a crafting grid after crafting with this ingredient. |
| Edible | Boolean | Checks if this item type provides the FOOD and CONSUMABLE and, thereby, is edible by a player. |
| Fuel | Boolean | Checks if this item type can be used as fuel in a Furnace |
| Item Rarity | ItemRarity | Returns the item rarity for the item. |
| Max Durability | short | Gets the maximum durability of this item type |
| Max Stack Size | Integer | Gets the maximum amount of this item type that can be held in a stack |
| Record | Boolean | True if this item type represents a playable music disk. |
