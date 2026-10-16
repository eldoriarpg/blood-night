---
title: Items
sidebar_position: 6
---

# Items

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

Mobs wear items, and [loot tables](loot-tables.md) drop them. There are three kinds:

1. **Plain items** by name, like `minecraft:golden_apple`.
2. **Items registered in game.** Hold any item and run `/bloodnight item register`. You get an ID. Click it to copy
   it. The item keeps everything: name, enchantments, custom data from other plugins.
   To register many items at once, run the command with an empty hand. A chest opens. Put in all the items you want
   and close it. You get every item back. Each item is registered once, even if you put it in twice, and an item you
   registered before keeps its ID.
3. **Items made in the editor**, with a material, a name, lore and enchantments.

Remove a registered item with `/bloodnight item remove <id>`.

Items you register show up in the editor's **Items** tab the next time you open an editor session.

## The blood night reward

A special mob killed by a player during a blood night also drops the blood night reward. It is a loot table that
comes with BloodNight: `bloodnight:blood_night/reward`. Most of the time it gives nothing. Sometimes it gives
emeralds, gold or a diamond, and rarely an enchanted book. Every item from it says "Blood touched" in red.

Switch it off for a world with `rewardLoot: false` under `loot` in the world file.

To change what the reward gives, make your own data pack with a file at
`data/bloodnight/loot_table/blood_night/reward.json`. It replaces ours. Mobs can also drop it, or any other loot
table, with the node [Drop Loot](../reference/nodes/world.md).

### Blood night enchantments

The reward is the only place to find these books. Enchanting tables, villagers and chests never have them.

| Enchantment | Goes on | What it does during a blood night |
|---|---|---|
| Night Ward (I to V) | Armor | Monsters deal 2.5% less damage for each level, added up over all pieces. Four pieces of Night Ward V take away half. Its pieces also calm the [heartbeat](../nights/atmosphere.md#for-each-player) twice as much. |
| Bloodthirst (I to III) | Weapons | Every kill heals one heart for each level. |
| Blood Walker (I to III) | Boots | You walk 10% faster for each level. |
| Blood Vision | Helmets | You see in the dark, like with night vision. Only you see it. It shows no icon. |
| Steady Heart | Chestplates | No heartbeat, no dark moments and no scares. |
| Hunter's Mark (I to III) | Bows, crossbows | What your arrows hit glows for 4 seconds for each level. |
| Curse of the Blood Moon | Armor | A curse. Monsters nearby come for you. In return, your kills roll the reward twice. |

Outside of blood nights these do nothing. Two more work at any time:

| Enchantment | Goes on | What it does |
|---|---|---|
| Blood Bane (I to IV) | Armor | Protects from [blood curse](#blood-curse) damage, like Feather Falling protects from falls. |
| Dawnbringer (I to V) | Weapons | Hits on special mobs deal 10% more damage for each level. |

## Blood curse

Mobs can deal a special kind of damage, `bloodnight:blood_curse`. Use it as the type of the node
[Damage](../reference/nodes/living-entity.md). Shields don't block it, and witches resist it like magic. A player it
kills "was claimed by the blood night".
