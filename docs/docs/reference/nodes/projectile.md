---
title: Projectiles
sidebar_position: 15
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Projectiles

## Mutate Projectile

`projectile.mutate`

Changes the properties of a Projectile that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Projectile | Projectile | Represents a shootable entity. |
| Has Been Shot (optional) | Boolean | Sets whether the projectile has been shot into the world and has sent a projectile shot game event in the next tick. |
| Has Left Shooter (optional) | Boolean | Sets whether the projectile has left the hitbox of their shooter and can now hit entities. |
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
| Owner Unique Id | UUID | Gets the owner's UUID |
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
| Weapon | Item | Gets the item being used as a weapon by this entity. |
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |

Goes on with: **Next**

## Read Projectile

`projectile.read`

Reads the properties of a Projectile

| In | Type | What it is |
|---|---|---|
| Projectile | Projectile | Represents a shootable entity. |

| Out | Type | What it is |
|---|---|---|
| Owner Unique Id | UUID | Gets the owner's UUID |
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
| Weapon | Item | Gets the item being used as a weapon by this entity. |
| Width | Number | Gets the entity's width |
| World | World | Gets the current world this entity resides in |
| X | Number | Gets the x-coordinate of this entity |
| Y | Number | Gets the y-coordinate of this entity |
| Yaw | Float | Gets this entity's yaw |
| Z | Number | Gets the z-coordinate of this entity |
