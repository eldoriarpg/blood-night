---
title: Living entities
description: "Living entities: every node of this group, with its inputs and outputs."
sidebar_position: 8
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Living entities

## Apply Effect

`living_entity.apply_effect`

Gives an entity a potion effect for some seconds; visible effects show particles and an icon

| In | Type | What it is |
|---|---|---|
| Living Entity | Living Entity | Who gets the effect |
| Effect | Potion Effect Type | The kind of effect |
| Seconds | Number | How long it lasts |
| Amplifier | Integer | Its strength, 0 for level I |
| Visible (optional) | Boolean | Whether it shows particles and an icon; true when not given |

Goes on with: **Next**

## Attribute Of Entity

`living_entity.attribute`

Gets the specified attribute instance from the object.

| In | Type | What it is |
|---|---|---|
| Living Entity | Living Entity | Represents a living entity, such as a monster or player |
| Attribute | Attribute | the attribute to get |

| Out | Type | What it is |
|---|---|---|
| Result | AttributeInstance | the attribute instance or null if not applicable to this object |

## Damage

`living_entity.damage`

Deals damage to an entity; with a source, the damage counts as dealt by it, and with a type, it is that kind of damage, such as bloodnight:blood_curse

| In | Type | What it is |
|---|---|---|
| Living Entity | Living Entity | Who takes the damage |
| Amount | Number | The damage, in half hearts |
| Source (optional) | Entity | Who deals it; without, the damage has no source |
| Damage Type (optional) | Damage Type | The kind of damage, which decides armor, resistances and the death message; an ordinary attack when not given |

Goes on with: **Next**

## Has Line Of Sight

`living_entity.has_line_of_sight`

Whether a living entity can see another one, with no block in between

| In | Type | What it is |
|---|---|---|
| Living Entity | Living Entity | Who looks |
| Other | Entity | What it looks at |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | Whether it sees it; on Folia false when the other is in another region |

## Launch Projectile

`living_entity.launch_projectile`

Launches a projectile from the shooter straight at a target

| In | Type | What it is |
|---|---|---|
| Shooter | Living Entity | Who launches it |
| Target | Entity | What it flies at |
| Speed | Number | How fast it flies |

| Out | Type | What it is |
|---|---|---|
| Projectile | Projectile | The launched projectile; on Folia missing when the target is in another region and nothing was launched |

Goes on with: **Next**

## Mutate Living Entity

`living_entity.mutate`

Changes the properties of a Living Entity that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Living Entity | Living Entity | Represents a living entity, such as a monster or player |
| Absorption Amount (optional) | Number | Sets the entity's absorption amount. |
| Active Item Remaining Time (optional) | Integer | Sets the number of ticks that remain for getActiveItem()'s usage. |
| Ai (optional) | Boolean | Sets whether an entity will have AI. |
| Arrow Cooldown (optional) | Integer | Sets the time in ticks until the next arrow leaves the entity's body. |
| Arrows In Body (optional) | Integer | Set the amount of arrows in the entity's body. |
| Bee Stinger Cooldown (optional) | Integer | Sets the time in ticks until the next stinger leaves the entity's body. |
| Bee Stingers In Body (optional) | Integer | Set the amount of bee stingers in the entity's body. |
| Body Yaw (optional) | Float | Sets entity body yaw |
| Can Pickup Items (optional) | Boolean | Sets whether or not the living entity can pick up items. |
| Collidable (optional) | Boolean | Set if this entity will be subject to collisions with other entities. |
| Friction State (optional) | TriState | Sets the friction state of this entity. |
| Gliding (optional) | Boolean | Makes entity start or stop gliding. |
| Health (optional) | Number | Sets the entity's health from 0 to getMaxHealth(), where 0 is dead. |
| Jumping (optional) | Boolean | Set entity jump state Setting to true will mark the entity to jump. |
| Killer (optional) | Player | Sets the player identified as the killer of the living entity. |
| Last Damage (optional) | Number | Sets the damage dealt within the current no damage ticks time period. |
| Leash Holder (optional) | Entity | Sets the leash on this entity to be held by the supplied entity. |
| Maximum Air (optional) | Integer | Sets the maximum amount of air the living entity can have, in ticks. |
| Maximum No Damage Ticks (optional) | Integer | Sets the living entity's current maximum no damage ticks. |
| No Action Ticks (optional) | Integer | Set the ticks that this entity has performed no action. |
| No Damage Ticks (optional) | Integer | Sets the living entity's current no damage ticks. |
| Remaining Air (optional) | Integer | Sets the amount of air that the living entity has remaining, in ticks. |
| Remove When Far Away (optional) | Boolean | Sets whether or not the living entity despawns when away from players or not. |
| Riptiding (optional) | Boolean | Makes entity start or stop riptiding. |
| Waypoint Color (optional) | Color | Sets the color of the waypoint icon. |
| Custom Name Visible (optional) | Boolean | Sets whether or not to display the mob's custom name client side. |
| Fall Distance (optional) | Float | Sets the fall distance for this entity |
| Fire Ticks (optional) | Integer | Sets the entity's current fire ticks (ticks before the entity stops being on fire). |
| Freeze Ticks (optional) | Integer | Sets the entity's current freeze ticks (amount of ticks the entity has been in powdered snow). |
| Glowing (optional) | Boolean | Sets whether the entity has a team colored (default: white) glow. |
| Gravity (optional) | Boolean | Sets whether gravity applies to this entity. |
| Invisible (optional) | Boolean | Sets whether the entity is invisible or not. |
| Invulnerable (optional) | Boolean | Sets whether the entity is invulnerable or not. |
| No Physics (optional) | Boolean | Sets this entity no physics status. |
| Op (optional) | Boolean | Sets the operator status of this object |
| Persistent (optional) | Boolean | Sets whether or not the entity gets persisted. |
| Portal Cooldown (optional) | Integer | Sets the period of time (in ticks) before this entity can use a portal. |
| Pose (optional) | Pose | Sets the entity's current Pose. |
| Silent (optional) | Boolean | Sets whether the entity is silent or not. |
| Sneaking (optional) | Boolean | Sets the sneak mode the entity. |
| Ticks Lived (optional) | Integer | Sets the amount of ticks this entity has lived for. |
| Velocity (optional) | Vector | Sets this entity's velocity in meters per tick |
| Visible By Default (optional) | Boolean | Sets whether or not this entity is visible by default. |
| Visual Fire (optional) | TriState | Sets if the entity has visual fire (it will always appear to be on fire). |

| Out | Type | What it is |
|---|---|---|
| Absorption Amount | Number | Gets the entity's absorption amount. |
| Active Item | Item | Gets the item being actively "used" or consumed. |
| Active Item Hand | EquipmentSlot | Get the hand using the active item. |
| Active Item Remaining Time | Integer | Gets the remaining number of ticks for getActiveItem()'s usage. |
| Active Item Used Time | Integer | Get how long the getActiveItem() has been in use for. |
| Active Potion Effects | Collection&lt;Potion Effect&gt; | Returns all currently active PotionEffects on the living entity. |
| Arrow Cooldown | Integer | Gets the time in ticks until the next arrow leaves the entity's body. |
| Arrows In Body | Integer | Gets the amount of arrows in an entity's body. |
| Bee Stinger Cooldown | Integer | Gets the time in ticks until the next bee stinger leaves the entity's body. |
| Bee Stingers In Body | Integer | Gets the amount of bee stingers in an entity's body. |
| Body Yaw | Float | Gets entity body yaw |
| Can Pickup Items | Boolean | Gets if the living entity can pick up items. |
| Climbing | Boolean | Gets if the entity is climbing. |
| Collidable | Boolean | Gets if this entity is subject to collisions with other entities. |
| Collidable Exemptions | Set&lt;UUID&gt; | Gets a mutable set of UUIDs of the entities which are exempt from the entity's collidable rule and which's collision with this entity will behave the opposite of it. |
| Death Sound | Sound | Get the Sound this entity will make on death. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Eye Height | Number | Gets the height of the living entity's eyes above its Location. |
| Eye Location | Location | Get a Location detailing the current eye position of the living entity. |
| Fall Damage Sound Big | Sound | Get the Sound this entity will make when falling from a large height. |
| Fall Damage Sound Small | Sound | Get the Sound this entity will make when falling from a small height. |
| Forwards Movement | Float | Retrieves the forwards movement direction of the entity. |
| Friction State | TriState | Gets the friction state of this entity. |
| Gliding | Boolean | Checks to see if an entity is gliding, such as using an Elytra. |
| Hand Raised | EquipmentSlot | Gets the hand raised by this living entity. |
| Hand Raised Time | Integer | Get how long the entity's hands have been raised (Charging Bow attack, using a potion, etc) |
| Health | Number | Gets the entity's health from 0 to getMaxHealth(), where 0 is dead. |
| Hurt Direction | Float | Gets player hurt direction |
| Hurt Sound | Sound | Get the Sound this entity will make when damaged. |
| Item Use Remaining Time | Integer | Gets remaining time a player needs to keep hands raised with an item to finish using it. |
| Jumping | Boolean | Get entity jump state. |
| Killer | Player | Gets the player identified as the killer of the living entity. |
| Last Damage | Number | Returns the living entity's last damage taken in the current no damage ticks time. |
| Leash Holder | Entity | Gets the entity that is currently leading this entity. |
| Leashed | Boolean | Returns whether the entity is currently leashed. |
| Maximum Air | Integer | Returns the maximum amount of air the living entity can have, in ticks. |
| Maximum No Damage Ticks | Integer | Returns the living entity's current maximum no damage ticks. |
| No Action Ticks | Integer | Get the ticks that this entity has performed no action. |
| No Damage Ticks | Integer | Returns the living entity's current no damage ticks. |
| Remaining Air | Integer | Returns the amount of air that the living entity has remaining, in ticks. |
| Remove When Far Away | Boolean | Returns if the living entity despawns when away from players or not. |
| Riptiding | Boolean | Checks to see if an entity is currently riptiding. |
| Sideways Movement | Float | Retrieves the sideways movement direction of the entity. |
| Sleeping | Boolean | Returns whether this entity is slumbering. |
| Sound Pitch | Float | Get the sound pitch at which this entity plays its sounds with. |
| Sound Volume | Float | Get the sound volume at which this entity plays its sounds with. |
| Swimming | Boolean | Checks to see if an entity is swimming. |
| Upwards Movement | Float | Retrieves the upwards movement direction of the entity. |
| Waypoint Color | Color | Gets the current color of the waypoint icon, if any. |
| Air Drag | Float | Returns the air drag factor applied to this entity. |
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
| Default Gravity | Number | Returns the default acceleration due to gravity (in blocks per tick). |
| Empty | Boolean | Check if a vehicle has passengers. |
| Entity Id | Integer | Returns the network protocol ID for this entity. |
| Entity Spawn Reason | SpawnReason | \{@return the org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason that initially spawned this entity or null if not yet spawned\} |
| Facing | BlockFace | Get the closest cardinal BlockFace direction an entity is currently facing. |
| Fall Distance | Float | Returns the distance this entity has fallen |
| Fire Ticks | Integer | Returns the entity's current fire ticks (ticks before the entity stops being on fire). |
| Freeze Ticking Locked | Boolean | Gets if the entity currently has its freeze ticks locked to a set amount. |
| Freeze Ticks | Integer | Returns the entity's current freeze ticks (amount of ticks the entity has been in powdered snow). |
| Frozen | Boolean | Gets if the entity is fully frozen (it has been in powdered snow for max freeze ticks). |
| Glowing | Boolean | Gets whether the entity is glowing or not. |
| Gravity | Number | Returns the acceleration due to gravity (in blocks per tick). |
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Location | Location | Gets the entity's current position |
| Marked Invulnerable | Boolean | Gets the current invulnerability state of the entity set by setInvulnerable(boolean). |
| Max Fire Ticks | Integer | Returns the entity's maximum fire ticks. |
| Max Freeze Ticks | Integer | Returns the entity's maximum freeze ticks (amount of ticks before it will be fully frozen) |
| Name | Text | Gets the name of this command sender |
| On Ground | Boolean | Returns true if the entity is supported by a block. |
| Op | Boolean | Checks if this object is a server operator |
| Origin | Location | Gets the location where this entity originates from. |
| Passengers | List&lt;Entity&gt; | Gets a list of passengers of this vehicle. |
| Persistent | Boolean | Returns true if the entity gets persisted. |
| Pick Item Stack | Item | Gets the ItemStack that a player would select / create (in creative mode) when using the pick block action on this entity. |
| Piston Move Reaction | PistonMoveReaction | Returns the reaction of the entity when moved by a piston. |
| Pitch | Float | Gets this entity's pitch |
| Portal Cooldown | Integer | Gets the period of time (in ticks) before this entity can use a portal. |
| Pose | Pose | Gets the entity's current pose. |
| Removal Reason | RemovalReason | \{@return the reason this entity was removed\} |
| Remove Event Cause | Cause | Gets the cause used for this entity's remove event. |
| Scoreboard Entry Name | Text | Gets the string name of the entity used to track it in Scoreboards. |
| Scoreboard Tags | Set&lt;Text&gt; | Returns a set of tags for this entity. |
| Silent | Boolean | Gets whether the entity is silent or not. |
| Sneaking | Boolean | Returns if the entity is in sneak mode |
| Sound Category | SoundCategory | Get the SoundCategory this entity will use when playing its sounds. |
| Spawn Category | SpawnCategory | Get the category of spawn to which this entity belongs. |
| Swim High Speed Splash Sound | Sound | Get the Sound this entity makes when splashing in water at high speeds. |
| Swim Sound | Sound | Get the Sound this entity makes while swimming. |
| Swim Splash Sound | Sound | Get the Sound this entity makes when splashing in water. |
| Ticking | Boolean | Check if entity is inside a ticking chunk |
| Ticks Lived | Integer | Gets the amount of ticks this entity has lived for. |
| Tracked By | Set&lt;Player&gt; | Get all players that are currently tracking this entity. |
| Type | Entity Type | Get the type of the entity. |
| Under Water | Boolean | Check if entity is underwater |
| Unique Id | UUID | Returns a unique and persistent id for this entity |
| Valid | Boolean | Returns false if the entity has died, been despawned for some other reason, or has not been added to the world. |
| Vehicle | Entity | Get the vehicle that this entity is inside. |
| Velocity | Vector | Gets this entity's current velocity |
| Visible By Default | Boolean | Gets whether or not this entity is visible by default. |
| Visual Fire | TriState | Retrieves the visual fire state of the entity. |
| Weapon | Item | Gets the item being used as a weapon by this entity. |
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

Goes on with: **Next**

## Read Living Entity

`living_entity.read`

Reads the properties of a Living Entity

| In | Type | What it is |
|---|---|---|
| Living Entity | Living Entity | Represents a living entity, such as a monster or player |

| Out | Type | What it is |
|---|---|---|
| Absorption Amount | Number | Gets the entity's absorption amount. |
| Active Item | Item | Gets the item being actively "used" or consumed. |
| Active Item Hand | EquipmentSlot | Get the hand using the active item. |
| Active Item Remaining Time | Integer | Gets the remaining number of ticks for getActiveItem()'s usage. |
| Active Item Used Time | Integer | Get how long the getActiveItem() has been in use for. |
| Active Potion Effects | Collection&lt;Potion Effect&gt; | Returns all currently active PotionEffects on the living entity. |
| Arrow Cooldown | Integer | Gets the time in ticks until the next arrow leaves the entity's body. |
| Arrows In Body | Integer | Gets the amount of arrows in an entity's body. |
| Bee Stinger Cooldown | Integer | Gets the time in ticks until the next bee stinger leaves the entity's body. |
| Bee Stingers In Body | Integer | Gets the amount of bee stingers in an entity's body. |
| Body Yaw | Float | Gets entity body yaw |
| Can Pickup Items | Boolean | Gets if the living entity can pick up items. |
| Climbing | Boolean | Gets if the entity is climbing. |
| Collidable | Boolean | Gets if this entity is subject to collisions with other entities. |
| Collidable Exemptions | Set&lt;UUID&gt; | Gets a mutable set of UUIDs of the entities which are exempt from the entity's collidable rule and which's collision with this entity will behave the opposite of it. |
| Death Sound | Sound | Get the Sound this entity will make on death. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Eye Height | Number | Gets the height of the living entity's eyes above its Location. |
| Eye Location | Location | Get a Location detailing the current eye position of the living entity. |
| Fall Damage Sound Big | Sound | Get the Sound this entity will make when falling from a large height. |
| Fall Damage Sound Small | Sound | Get the Sound this entity will make when falling from a small height. |
| Forwards Movement | Float | Retrieves the forwards movement direction of the entity. |
| Friction State | TriState | Gets the friction state of this entity. |
| Gliding | Boolean | Checks to see if an entity is gliding, such as using an Elytra. |
| Hand Raised | EquipmentSlot | Gets the hand raised by this living entity. |
| Hand Raised Time | Integer | Get how long the entity's hands have been raised (Charging Bow attack, using a potion, etc) |
| Health | Number | Gets the entity's health from 0 to getMaxHealth(), where 0 is dead. |
| Hurt Direction | Float | Gets player hurt direction |
| Hurt Sound | Sound | Get the Sound this entity will make when damaged. |
| Item Use Remaining Time | Integer | Gets remaining time a player needs to keep hands raised with an item to finish using it. |
| Jumping | Boolean | Get entity jump state. |
| Killer | Player | Gets the player identified as the killer of the living entity. |
| Last Damage | Number | Returns the living entity's last damage taken in the current no damage ticks time. |
| Leash Holder | Entity | Gets the entity that is currently leading this entity. |
| Leashed | Boolean | Returns whether the entity is currently leashed. |
| Maximum Air | Integer | Returns the maximum amount of air the living entity can have, in ticks. |
| Maximum No Damage Ticks | Integer | Returns the living entity's current maximum no damage ticks. |
| No Action Ticks | Integer | Get the ticks that this entity has performed no action. |
| No Damage Ticks | Integer | Returns the living entity's current no damage ticks. |
| Remaining Air | Integer | Returns the amount of air that the living entity has remaining, in ticks. |
| Remove When Far Away | Boolean | Returns if the living entity despawns when away from players or not. |
| Riptiding | Boolean | Checks to see if an entity is currently riptiding. |
| Sideways Movement | Float | Retrieves the sideways movement direction of the entity. |
| Sleeping | Boolean | Returns whether this entity is slumbering. |
| Sound Pitch | Float | Get the sound pitch at which this entity plays its sounds with. |
| Sound Volume | Float | Get the sound volume at which this entity plays its sounds with. |
| Swimming | Boolean | Checks to see if an entity is swimming. |
| Upwards Movement | Float | Retrieves the upwards movement direction of the entity. |
| Waypoint Color | Color | Gets the current color of the waypoint icon, if any. |
| Air Drag | Float | Returns the air drag factor applied to this entity. |
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
| Default Gravity | Number | Returns the default acceleration due to gravity (in blocks per tick). |
| Empty | Boolean | Check if a vehicle has passengers. |
| Entity Id | Integer | Returns the network protocol ID for this entity. |
| Entity Spawn Reason | SpawnReason | \{@return the org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason that initially spawned this entity or null if not yet spawned\} |
| Facing | BlockFace | Get the closest cardinal BlockFace direction an entity is currently facing. |
| Fall Distance | Float | Returns the distance this entity has fallen |
| Fire Ticks | Integer | Returns the entity's current fire ticks (ticks before the entity stops being on fire). |
| Freeze Ticking Locked | Boolean | Gets if the entity currently has its freeze ticks locked to a set amount. |
| Freeze Ticks | Integer | Returns the entity's current freeze ticks (amount of ticks the entity has been in powdered snow). |
| Frozen | Boolean | Gets if the entity is fully frozen (it has been in powdered snow for max freeze ticks). |
| Glowing | Boolean | Gets whether the entity is glowing or not. |
| Gravity | Number | Returns the acceleration due to gravity (in blocks per tick). |
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Location | Location | Gets the entity's current position |
| Marked Invulnerable | Boolean | Gets the current invulnerability state of the entity set by setInvulnerable(boolean). |
| Max Fire Ticks | Integer | Returns the entity's maximum fire ticks. |
| Max Freeze Ticks | Integer | Returns the entity's maximum freeze ticks (amount of ticks before it will be fully frozen) |
| Name | Text | Gets the name of this command sender |
| On Ground | Boolean | Returns true if the entity is supported by a block. |
| Op | Boolean | Checks if this object is a server operator |
| Origin | Location | Gets the location where this entity originates from. |
| Passengers | List&lt;Entity&gt; | Gets a list of passengers of this vehicle. |
| Persistent | Boolean | Returns true if the entity gets persisted. |
| Pick Item Stack | Item | Gets the ItemStack that a player would select / create (in creative mode) when using the pick block action on this entity. |
| Piston Move Reaction | PistonMoveReaction | Returns the reaction of the entity when moved by a piston. |
| Pitch | Float | Gets this entity's pitch |
| Portal Cooldown | Integer | Gets the period of time (in ticks) before this entity can use a portal. |
| Pose | Pose | Gets the entity's current pose. |
| Removal Reason | RemovalReason | \{@return the reason this entity was removed\} |
| Remove Event Cause | Cause | Gets the cause used for this entity's remove event. |
| Scoreboard Entry Name | Text | Gets the string name of the entity used to track it in Scoreboards. |
| Scoreboard Tags | Set&lt;Text&gt; | Returns a set of tags for this entity. |
| Silent | Boolean | Gets whether the entity is silent or not. |
| Sneaking | Boolean | Returns if the entity is in sneak mode |
| Sound Category | SoundCategory | Get the SoundCategory this entity will use when playing its sounds. |
| Spawn Category | SpawnCategory | Get the category of spawn to which this entity belongs. |
| Swim High Speed Splash Sound | Sound | Get the Sound this entity makes when splashing in water at high speeds. |
| Swim Sound | Sound | Get the Sound this entity makes while swimming. |
| Swim Splash Sound | Sound | Get the Sound this entity makes when splashing in water. |
| Ticking | Boolean | Check if entity is inside a ticking chunk |
| Ticks Lived | Integer | Gets the amount of ticks this entity has lived for. |
| Tracked By | Set&lt;Player&gt; | Get all players that are currently tracking this entity. |
| Type | Entity Type | Get the type of the entity. |
| Under Water | Boolean | Check if entity is underwater |
| Unique Id | UUID | Returns a unique and persistent id for this entity |
| Valid | Boolean | Returns false if the entity has died, been despawned for some other reason, or has not been added to the world. |
| Vehicle | Entity | Get the vehicle that this entity is inside. |
| Velocity | Vector | Gets this entity's current velocity |
| Visible By Default | Boolean | Gets whether or not this entity is visible by default. |
| Visual Fire | TriState | Retrieves the visual fire state of the entity. |
| Weapon | Item | Gets the item being used as a weapon by this entity. |
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

## Remove Potion Effect

`living_entity.remove_potion_effect`

Removes any effects present of the given PotionEffectType.

| In | Type | What it is |
|---|---|---|
| Living Entity | Living Entity | Represents a living entity, such as a monster or player |
| Type | Potion Effect Type | the potion type to remove |

Goes on with: **Next**
