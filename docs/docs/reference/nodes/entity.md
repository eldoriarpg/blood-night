---
title: Entities
description: "Entities: every node of this group, with its inputs and outputs."
sidebar_position: 4
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Entities

## Carry

`entity.carry`

Puts an entity on the carrier; it cannot get off on its own, e.g. by sneaking, until it is dropped or the carrier dies

| In | Type | What it is |
|---|---|---|
| Carrier | Entity | Who carries |
| Passenger | Entity | Who is carried |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True: the carrier now carries it |

Goes on with: **Next**

## Drop Passengers

`entity.drop_passengers`

Lets go of everything an entity carries

| In | Type | What it is |
|---|---|---|
| Carrier | Entity | Who lets go |

Goes on with: **Next**

## Entities Near Entity

`entity.nearby_entities`

Entities within a box around an entity, without the entity itself

| In | Type | What it is |
|---|---|---|
| Entity | Entity | The centre of the search |
| X | Number | How far to search east and west |
| Y | Number | How far to search up and down |
| Z | Number | How far to search north and south |

| Out | Type | What it is |
|---|---|---|
| Result | List&lt;Entity&gt; | The entities in that box; on Folia empty when the box reaches into another region |

## Equip

`equipment.equip`

Stores the ItemStack at the given equipment slot in the inventory.

| In | Type | What it is |
|---|---|---|
| Equipment | EntityEquipment | An interface to a creatures inventory |
| Slot | EquipmentSlot | the slot to put the ItemStack |
| Item | Item | the ItemStack to set |

Goes on with: **Next**

## Explode

`creeper.explode`

Makes this Creeper explode instantly.

| In | Type | What it is |
|---|---|---|
| Creeper | Creeper | Represents a Creeper |

Goes on with: **Next**

## Ignite

`creeper.ignite`

Ignites this Creeper, beginning its fuse.

| In | Type | What it is |
|---|---|---|
| Creeper | Creeper | Represents a Creeper |

Goes on with: **Next**

## Item In Slot

`equipment.item_in_slot`

Gets the ItemStack at the given equipment slot in the inventory.

| In | Type | What it is |
|---|---|---|
| Equipment | EntityEquipment | An interface to a creatures inventory |
| Slot | EquipmentSlot | the slot to get the ItemStack |

| Out | Type | What it is |
|---|---|---|
| Result | Item | the ItemStack in the given slot |

## Lift

`entity.lift`

Moves an entity straight up with everything it carries, a little each tick; a mob does not steer while it rises. Goes on right away, the rise takes its time

| In | Type | What it is |
|---|---|---|
| Entity | Entity | Who rises |
| Height | Number | How far up, in blocks; it stops early below a block |
| Seconds | Number | How long the rise takes |

Goes on with: **Next**

## Mutate Attribute

`attribute.mutate`

Changes the properties of a Attribute that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Attribute | AttributeInstance | Represents a mutable instance of an attribute and its associated modifiers and values. |
| Base Value (optional) | Number | Set the base value of this instance. |

| Out | Type | What it is |
|---|---|---|
| Attribute | Attribute | The attribute pertaining to this instance. |
| Base Value | Number | Base value of this instance before modifiers are applied. |
| Value | Number | Get the value of this instance after all associated modifiers have been applied. |

Goes on with: **Next**

## Mutate Creeper

`creeper.mutate`

Changes the properties of a Creeper that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Creeper | Creeper | Represents a Creeper |
| Explosion Radius (optional) | Integer | Set the explosion radius in which this Creeper's explosion will affect. |
| Fuse Ticks (optional) | Integer | Set the fuse ticks for this Creeper, where the ticks is the amount of time in which a creeper has been in the primed state. |
| Ignited (optional) | Boolean | Set whether creeper is ignited or not (armed to explode) |
| Max Fuse Ticks (optional) | Integer | Set the maximum fuse ticks for this Creeper, where the maximum ticks is the amount of time in which a creeper is allowed to be in the primed state before exploding. |
| Powered (optional) | Boolean | Sets the Powered status of this Creeper |
| Aggressive (optional) | Boolean | Some mobs will raise their arm(s) when aggressive, see isAggressive() for full list. |
| Aware (optional) | Boolean | Sets whether this mob is aware of its surroundings. |
| Leash Holder (optional) | Entity | Sets the leash on this entity to be held by the supplied entity. |
| Left Handed (optional) | Boolean | Set if Mob is left-handed |
| Seed (optional) | Long | Set the seed used when this Loot Table generates loot. |
| Target (optional) | Living Entity | Instructs this Mob to set the specified LivingEntity as its target. |
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
| Explosion Radius | Integer | Get the explosion radius in which this Creeper's explosion will affect. |
| Fuse Ticks | Integer | Get the maximum fuse ticks for this Creeper, where the ticks is the amount of time in which a creeper has been in the primed state. |
| Ignited | Boolean | Check if creeper is ignited or not (armed to explode) |
| Igniter | Entity | Gets the entity which ignited the creeper, if available. |
| Max Fuse Ticks | Integer | Get the maximum fuse ticks for this Creeper, where the maximum ticks is the amount of time in which a creeper is allowed to be in the primed state before exploding. |
| Powered | Boolean | Checks if this Creeper is powered (Electrocuted) |
| Aggressive | Boolean | Some mobs will raise their arm(s) when aggressive: Drowned Piglin Skeleton Zombie ZombieVillager Illusioner Vindicator Panda Pillager PiglinBrute Note: This doesn't always show the actual aggressive state as set by setAggressive(boolean). |
| Ambient Sound | Sound | Get the Sound this mob makes while ambiently existing. |
| Aware | Boolean | Gets whether this mob is aware of its surroundings. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Head Rotation Speed | Integer | Gets the head rotation speed |
| In Daylight | Boolean | Check if this mob is exposed to daylight |
| Leash Holder | Entity | Gets the entity that is currently leading this entity. |
| Leashed | Boolean | Returns whether the entity is currently leashed. |
| Left Handed | Boolean | Check if Mob is left-handed |
| Max Head Pitch | Integer | Gets the max head pitch rotation |
| Possible Experience Reward | Integer | Gets the amount of experience the mob will possibly drop. |
| Seed | Long | Get the Loot Table's seed. |
| Target | Living Entity | Gets the current target of this Mob |
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
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
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
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Location | Location | Gets the entity's current position |
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
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

Goes on with: **Next**

## Mutate Ender Dragon

`ender_dragon.mutate`

Changes the properties of a Ender Dragon that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Ender Dragon | EnderDragon | Represents an Ender Dragon |
| Phase (optional) | Phase | Sets the next phase for the dragon to perform. |
| Podium (optional) | Location | Sets the location of the podium for the ender dragon. |
| Aggressive (optional) | Boolean | Some mobs will raise their arm(s) when aggressive, see isAggressive() for full list. |
| Aware (optional) | Boolean | Sets whether this mob is aware of its surroundings. |
| Leash Holder (optional) | Entity | Sets the leash on this entity to be held by the supplied entity. |
| Left Handed (optional) | Boolean | Set if Mob is left-handed |
| Seed (optional) | Long | Set the seed used when this Loot Table generates loot. |
| Target (optional) | Living Entity | Instructs this Mob to set the specified LivingEntity as its target. |
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
| Death Animation Ticks | Integer | Get the current time in ticks relative to the start of this dragon's death animation. |
| Parts | Set&lt;ComplexEntityPart&gt; | Gets a list of parts that belong to this complex entity |
| Phase | Phase | Gets the current phase that the dragon is performing. |
| Podium | Location | Get the podium location used by the ender dragon. |
| Aggressive | Boolean | Some mobs will raise their arm(s) when aggressive: Drowned Piglin Skeleton Zombie ZombieVillager Illusioner Vindicator Panda Pillager PiglinBrute Note: This doesn't always show the actual aggressive state as set by setAggressive(boolean). |
| Ambient Sound | Sound | Get the Sound this mob makes while ambiently existing. |
| Aware | Boolean | Gets whether this mob is aware of its surroundings. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Head Rotation Speed | Integer | Gets the head rotation speed |
| In Daylight | Boolean | Check if this mob is exposed to daylight |
| Leash Holder | Entity | Gets the entity that is currently leading this entity. |
| Leashed | Boolean | Returns whether the entity is currently leashed. |
| Left Handed | Boolean | Check if Mob is left-handed |
| Max Head Pitch | Integer | Gets the max head pitch rotation |
| Possible Experience Reward | Integer | Gets the amount of experience the mob will possibly drop. |
| Seed | Long | Get the Loot Table's seed. |
| Target | Living Entity | Gets the current target of this Mob |
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
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
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
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Location | Location | Gets the entity's current position |
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
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

Goes on with: **Next**

## Mutate Entity

`entity.mutate`

Changes the properties of a Entity that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Entity | Entity | Represents a base entity in the world Not all methods are guaranteed to work/may have side effects when isInWorld() is false. |
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
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
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
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Location | Location | Gets the entity's current position |
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
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

Goes on with: **Next**

## Mutate Mob

`mob.mutate`

Changes the properties of a Mob that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Mob | Mob | Represents a Mob. |
| Aggressive (optional) | Boolean | Some mobs will raise their arm(s) when aggressive, see isAggressive() for full list. |
| Aware (optional) | Boolean | Sets whether this mob is aware of its surroundings. |
| Leash Holder (optional) | Entity | Sets the leash on this entity to be held by the supplied entity. |
| Left Handed (optional) | Boolean | Set if Mob is left-handed |
| Seed (optional) | Long | Set the seed used when this Loot Table generates loot. |
| Target (optional) | Living Entity | Instructs this Mob to set the specified LivingEntity as its target. |
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
| Aggressive | Boolean | Some mobs will raise their arm(s) when aggressive: Drowned Piglin Skeleton Zombie ZombieVillager Illusioner Vindicator Panda Pillager PiglinBrute Note: This doesn't always show the actual aggressive state as set by setAggressive(boolean). |
| Ambient Sound | Sound | Get the Sound this mob makes while ambiently existing. |
| Aware | Boolean | Gets whether this mob is aware of its surroundings. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Head Rotation Speed | Integer | Gets the head rotation speed |
| In Daylight | Boolean | Check if this mob is exposed to daylight |
| Leash Holder | Entity | Gets the entity that is currently leading this entity. |
| Leashed | Boolean | Returns whether the entity is currently leashed. |
| Left Handed | Boolean | Check if Mob is left-handed |
| Max Head Pitch | Integer | Gets the max head pitch rotation |
| Possible Experience Reward | Integer | Gets the amount of experience the mob will possibly drop. |
| Seed | Long | Get the Loot Table's seed. |
| Target | Living Entity | Gets the current target of this Mob |
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
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
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
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Location | Location | Gets the entity's current position |
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
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

Goes on with: **Next**

## Read Attribute

`attribute.read`

Reads the properties of a Attribute

| In | Type | What it is |
|---|---|---|
| Attribute | AttributeInstance | Represents a mutable instance of an attribute and its associated modifiers and values. |

| Out | Type | What it is |
|---|---|---|
| Attribute | Attribute | The attribute pertaining to this instance. |
| Base Value | Number | Base value of this instance before modifiers are applied. |
| Value | Number | Get the value of this instance after all associated modifiers have been applied. |

## Read Creeper

`creeper.read`

Reads the properties of a Creeper

| In | Type | What it is |
|---|---|---|
| Creeper | Creeper | Represents a Creeper |

| Out | Type | What it is |
|---|---|---|
| Explosion Radius | Integer | Get the explosion radius in which this Creeper's explosion will affect. |
| Fuse Ticks | Integer | Get the maximum fuse ticks for this Creeper, where the ticks is the amount of time in which a creeper has been in the primed state. |
| Ignited | Boolean | Check if creeper is ignited or not (armed to explode) |
| Igniter | Entity | Gets the entity which ignited the creeper, if available. |
| Max Fuse Ticks | Integer | Get the maximum fuse ticks for this Creeper, where the maximum ticks is the amount of time in which a creeper is allowed to be in the primed state before exploding. |
| Powered | Boolean | Checks if this Creeper is powered (Electrocuted) |
| Aggressive | Boolean | Some mobs will raise their arm(s) when aggressive: Drowned Piglin Skeleton Zombie ZombieVillager Illusioner Vindicator Panda Pillager PiglinBrute Note: This doesn't always show the actual aggressive state as set by setAggressive(boolean). |
| Ambient Sound | Sound | Get the Sound this mob makes while ambiently existing. |
| Aware | Boolean | Gets whether this mob is aware of its surroundings. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Head Rotation Speed | Integer | Gets the head rotation speed |
| In Daylight | Boolean | Check if this mob is exposed to daylight |
| Leash Holder | Entity | Gets the entity that is currently leading this entity. |
| Leashed | Boolean | Returns whether the entity is currently leashed. |
| Left Handed | Boolean | Check if Mob is left-handed |
| Max Head Pitch | Integer | Gets the max head pitch rotation |
| Possible Experience Reward | Integer | Gets the amount of experience the mob will possibly drop. |
| Seed | Long | Get the Loot Table's seed. |
| Target | Living Entity | Gets the current target of this Mob |
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
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
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
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Location | Location | Gets the entity's current position |
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
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

## Read Ender Dragon

`ender_dragon.read`

Reads the properties of a Ender Dragon

| In | Type | What it is |
|---|---|---|
| Ender Dragon | EnderDragon | Represents an Ender Dragon |

| Out | Type | What it is |
|---|---|---|
| Death Animation Ticks | Integer | Get the current time in ticks relative to the start of this dragon's death animation. |
| Parts | Set&lt;ComplexEntityPart&gt; | Gets a list of parts that belong to this complex entity |
| Phase | Phase | Gets the current phase that the dragon is performing. |
| Podium | Location | Get the podium location used by the ender dragon. |
| Aggressive | Boolean | Some mobs will raise their arm(s) when aggressive: Drowned Piglin Skeleton Zombie ZombieVillager Illusioner Vindicator Panda Pillager PiglinBrute Note: This doesn't always show the actual aggressive state as set by setAggressive(boolean). |
| Ambient Sound | Sound | Get the Sound this mob makes while ambiently existing. |
| Aware | Boolean | Gets whether this mob is aware of its surroundings. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Head Rotation Speed | Integer | Gets the head rotation speed |
| In Daylight | Boolean | Check if this mob is exposed to daylight |
| Leash Holder | Entity | Gets the entity that is currently leading this entity. |
| Leashed | Boolean | Returns whether the entity is currently leashed. |
| Left Handed | Boolean | Check if Mob is left-handed |
| Max Head Pitch | Integer | Gets the max head pitch rotation |
| Possible Experience Reward | Integer | Gets the amount of experience the mob will possibly drop. |
| Seed | Long | Get the Loot Table's seed. |
| Target | Living Entity | Gets the current target of this Mob |
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
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
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
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Location | Location | Gets the entity's current position |
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
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

## Read Entity

`entity.read`

Reads the properties of a Entity

| In | Type | What it is |
|---|---|---|
| Entity | Entity | Represents a base entity in the world Not all methods are guaranteed to work/may have side effects when isInWorld() is false. |

| Out | Type | What it is |
|---|---|---|
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
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
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Location | Location | Gets the entity's current position |
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
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

## Read Equipment

`equipment.read`

Reads the properties of a Equipment

| In | Type | What it is |
|---|---|---|
| Equipment | EntityEquipment | An interface to a creatures inventory |

| Out | Type | What it is |
|---|---|---|
| Boots | Item | Gets the boots currently being worn by the entity This returns a copy if this equipment instance is from a non-player. |
| Boots Drop Chance | Float | Gets the chance of the boots being dropped upon this creature's death. |
| Chestplate | Item | Gets the chest plate currently being worn by the entity This returns a copy if this equipment instance is from a non-player. |
| Chestplate Drop Chance | Float | Gets the chance of the chest plate being dropped upon this creature's death. |
| Helmet | Item | Gets the helmet currently being worn by the entity This returns a copy if this equipment instance is from a non-player. |
| Helmet Drop Chance | Float | Gets the chance of the helmet being dropped upon this creature's death. |
| Holder | Entity | Get the entity this EntityEquipment belongs to |
| Item In Main Hand | Item | Gets the item the entity is currently holding in their main hand. |
| Item In Main Hand Drop Chance | Float | Gets the chance of the main hand item being dropped upon this creature's death. |
| Item In Off Hand | Item | Gets the item the entity is currently holding in their off hand. |
| Item In Off Hand Drop Chance | Float | Gets the chance of the off hand item being dropped upon this creature's death. |
| Leggings | Item | Gets the leggings currently being worn by the entity This returns a copy if this equipment instance is from a non-player. |
| Leggings Drop Chance | Float | Gets the chance of the leggings being dropped upon this creature's death. |

## Read Mob

`mob.read`

Reads the properties of a Mob

| In | Type | What it is |
|---|---|---|
| Mob | Mob | Represents a Mob. |

| Out | Type | What it is |
|---|---|---|
| Aggressive | Boolean | Some mobs will raise their arm(s) when aggressive: Drowned Piglin Skeleton Zombie ZombieVillager Illusioner Vindicator Panda Pillager PiglinBrute Note: This doesn't always show the actual aggressive state as set by setAggressive(boolean). |
| Ambient Sound | Sound | Get the Sound this mob makes while ambiently existing. |
| Aware | Boolean | Gets whether this mob is aware of its surroundings. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Head Rotation Speed | Integer | Gets the head rotation speed |
| In Daylight | Boolean | Check if this mob is exposed to daylight |
| Leash Holder | Entity | Gets the entity that is currently leading this entity. |
| Leashed | Boolean | Returns whether the entity is currently leashed. |
| Left Handed | Boolean | Check if Mob is left-handed |
| Max Head Pitch | Integer | Gets the max head pitch rotation |
| Possible Experience Reward | Integer | Gets the amount of experience the mob will possibly drop. |
| Seed | Long | Get the Loot Table's seed. |
| Target | Living Entity | Gets the current target of this Mob |
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
| As String | Text | Get this entity as an NBT string. |
| Custom Name Visible | Boolean | Gets whether or not the mob's custom name is displayed client side. |
| Dead | Boolean | Returns true if this entity has been marked for removal. |
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
| Height | Number | Gets the entity's height |
| In Lava | Boolean | Check if entity is in lava |
| In Powdered Snow | Boolean | Check if entity is inside powdered snow. |
| In Rain | Boolean | Check if entity is in rain |
| In Water | Boolean | Returns true if the entity is in water. |
| In World | Boolean | Checks if this entity has been spawned in a world. |
| Inside Vehicle | Boolean | Returns whether this entity is inside a vehicle. |
| Invisible | Boolean | Gets whether the entity is invisible or not. |
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Location | Location | Gets the entity's current position |
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
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

## Remove Entity

`entity.remove`

Mark the entity's removal.

| In | Type | What it is |
|---|---|---|
| Entity | Entity | Represents a base entity in the world Not all methods are guaranteed to work/may have side effects when isInWorld() is false. |

Goes on with: **Next**

## Set Slot Drop Chance

`equipment.set_slot_drop_chance`

Sets the drop chance of the specified slot.

| In | Type | What it is |
|---|---|---|
| Equipment | EntityEquipment | An interface to a creatures inventory |
| Slot | EquipmentSlot | the slot to set the drop chance of |
| Chance | Float | the drop chance for the slot |

Goes on with: **Next**

## Show Above

`entity.show_above`

Shows text or an item over an entity for a while; it follows the entity

| In | Type | What it is |
|---|---|---|
| Entity | Entity | Who it floats over |
| Text (optional) | Text | What it says, MiniMessage like &lt;red&gt;! |
| Item (optional) | Item | An item to show when there is no text |
| Seconds | Number | How long it shows |

Goes on with: **Next**

## Teleport

`entity.teleport`

Moves an entity to a location; the move finishes once the place is loaded

| In | Type | What it is |
|---|---|---|
| Entity | Entity | Who moves |
| Location | Location | Where to |

| Out | Type | What it is |
|---|---|---|
| Result | Boolean | True: the move has started |

Goes on with: **Next**
