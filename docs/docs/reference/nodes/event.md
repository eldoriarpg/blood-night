---
title: Triggers and events
sidebar_position: 20
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Triggers and events

## On Damage

`event.damage`

The mob took damage from something other than an entity

| Out | Type | What it is |
|---|---|---|
| Event | EntityDamageEvent | Stores data for damage events |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Cause | Damage Cause | Gets the cause of the damage. |
| Damage | Number | Gets the raw amount of damage caused by the event |
| Entity | Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Final Damage | Number | Gets the amount of damage caused by the event after all damage reduction is applied. |

Goes on with: **Next**

## On Damage By Entity

`event.damage_by_entity`

The mob took damage from an entity that is not a player, or from what such an entity shot or set off

| Out | Type | What it is |
|---|---|---|
| Event | EntityDamageByEntityEvent | Called when an entity is damaged by an entity |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Cause | Damage Cause | Gets the cause of the damage. |
| Critical | Boolean | Shows this damage instance was critical. |
| Damage | Number | Gets the raw amount of damage caused by the event |
| Damager | Entity | Returns the entity that damaged the defender. |
| Entity | Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Final Damage | Number | Gets the amount of damage caused by the event after all damage reduction is applied. |
| Attacker | Entity | The entity behind it: a player who shot an arrow counts, not the arrow; missing when none |

Goes on with: **Next**

## On Damage By Player

`event.damage_by_player`

The mob took damage from a player, or from what a player shot or set off

| Out | Type | What it is |
|---|---|---|
| Event | EntityDamageByEntityEvent | Called when an entity is damaged by an entity |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Cause | Damage Cause | Gets the cause of the damage. |
| Critical | Boolean | Shows this damage instance was critical. |
| Damage | Number | Gets the raw amount of damage caused by the event |
| Damager | Entity | Returns the entity that damaged the defender. |
| Entity | Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Final Damage | Number | Gets the amount of damage caused by the event after all damage reduction is applied. |
| Attacker | Entity | The entity behind it: a player who shot an arrow counts, not the arrow; missing when none |

Goes on with: **Next**

## On Death

`event.death`

The mob died

| Out | Type | What it is |
|---|---|---|
| Event | EntityDeathEvent | Thrown whenever a LivingEntity dies |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Death Sound | Sound | Get the sound that the entity makes when dying |
| Death Sound Category | SoundCategory | Get the sound category that the death sound should play in |
| Death Sound Pitch | Float | Get the pitch that the death sound will play with. |
| Death Sound Volume | Float | Get the volume that the death sound will play at. |
| Dropped Exp | Integer | Gets how much EXP should be dropped from this death. |
| Drops | List&lt;Item&gt; | Gets all the items which will drop when the entity dies |
| Entity | Living Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Revive Health | Number | Get the amount of health that the entity should revive with after cancelling the event. |
| Attacker | Entity | The entity behind it: a player who shot an arrow counts, not the arrow; missing when none |

Goes on with: **Next**

## On Explode

`event.explode`

The mob explodes

| Out | Type | What it is |
|---|---|---|
| Event | EntityExplodeEvent | Called when an entity explodes interacting with blocks. |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Entity | Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Explosion Result | ExplosionResult | Returns the result of the explosion if it is not cancelled. |
| Location | Location | Returns the location where the explosion happened. |
| Yield | Float | Returns the percentage of blocks to drop from this explosion |

Goes on with: **Next**

## On Explosion Prime

`event.explosion_prime`

The mob is about to explode

| Out | Type | What it is |
|---|---|---|
| Event | ExplosionPrimeEvent | Called when an entity has made a decision to explode. |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Entity | Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Fire | Boolean | Gets whether this explosion will create fire or not |
| Radius | Float | Gets the radius of the explosion |

Goes on with: **Next**

## On Extension Damage

`event.extension_damage`

The entity riding or carrying the mob took damage; cancelling it also keeps the damage from being passed on to the mob

| Out | Type | What it is |
|---|---|---|
| Event | EntityDamageEvent | Stores data for damage events |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Cause | Damage Cause | Gets the cause of the damage. |
| Damage | Number | Gets the raw amount of damage caused by the event |
| Entity | Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Final Damage | Number | Gets the amount of damage caused by the event after all damage reduction is applied. |
| Attacker | Entity | The entity behind it: a player who shot an arrow counts, not the arrow; missing when none |

Goes on with: **Next**

## On Extension Death

`event.extension_death`

The entity riding or carrying the mob died

| Out | Type | What it is |
|---|---|---|
| Event | EntityDeathEvent | Thrown whenever a LivingEntity dies |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Death Sound | Sound | Get the sound that the entity makes when dying |
| Death Sound Category | SoundCategory | Get the sound category that the death sound should play in |
| Death Sound Pitch | Float | Get the pitch that the death sound will play with. |
| Death Sound Volume | Float | Get the volume that the death sound will play at. |
| Dropped Exp | Integer | Gets how much EXP should be dropped from this death. |
| Drops | List&lt;Item&gt; | Gets all the items which will drop when the entity dies |
| Entity | Living Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Revive Health | Number | Get the amount of health that the entity should revive with after cancelling the event. |

Goes on with: **Next**

## On Hit

`event.hit`

The mob damaged another entity, directly or with what it shot or set off

| Out | Type | What it is |
|---|---|---|
| Event | EntityDamageByEntityEvent | Called when an entity is damaged by an entity |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Cause | Damage Cause | Gets the cause of the damage. |
| Critical | Boolean | Shows this damage instance was critical. |
| Damage | Number | Gets the raw amount of damage caused by the event |
| Damager | Entity | Returns the entity that damaged the defender. |
| Entity | Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Final Damage | Number | Gets the amount of damage caused by the event after all damage reduction is applied. |
| Attacker | Entity | The entity behind it: a player who shot an arrow counts, not the arrow; missing when none |

Goes on with: **Next**

## On Kill

`event.kill`

The mob killed an entity or player; Victim narrows it to players or other entities

| Out | Type | What it is |
|---|---|---|
| Event | EntityDeathEvent | Thrown whenever a LivingEntity dies |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Death Sound | Sound | Get the sound that the entity makes when dying |
| Death Sound Category | SoundCategory | Get the sound category that the death sound should play in |
| Death Sound Pitch | Float | Get the pitch that the death sound will play with. |
| Death Sound Volume | Float | Get the volume that the death sound will play at. |
| Dropped Exp | Integer | Gets how much EXP should be dropped from this death. |
| Drops | List&lt;Item&gt; | Gets all the items which will drop when the entity dies |
| Entity | Living Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Revive Health | Number | Get the amount of health that the entity should revive with after cancelling the event. |

Goes on with: **Next**

## On Projectile Hit

`event.projectile_hit`

A projectile launched by the mob hit something

| Out | Type | What it is |
|---|---|---|
| Event | ProjectileHitEvent | Called when a projectile hits an object |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Entity | Projectile | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Hit Block | Block | Gets the block that was hit, if it was a block that was hit. |
| Hit Block Face | BlockFace | Gets the block face that was hit, if it was a block that was hit and the face was provided in the event. |
| Hit Entity | Entity | Gets the entity that was hit, if it was an entity that was hit. |

Goes on with: **Next**

## On Projectile Shoot

`event.projectile_shoot`

The mob launched a projectile

| Out | Type | What it is |
|---|---|---|
| Event | ProjectileLaunchEvent | Called when a projectile is launched. |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Entity | Projectile | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Location | Location | Gets the location at which the entity is spawning. |

Goes on with: **Next**

## On Target Change

`event.target_change`

The mob gets or loses a target

| Out | Type | What it is |
|---|---|---|
| Event | EntityTargetLivingEntityEvent | Called when an Entity targets a LivingEntity and can only target LivingEntity's. |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Entity | Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| Reason | TargetReason | Returns the reason for the targeting |
| Target | Living Entity | Get the entity that this is targeting. |

Goes on with: **Next**

## On Teleport

`event.teleport`

The mob teleports

| Out | Type | What it is |
|---|---|---|
| Event | EntityTeleportEvent | Thrown when a non-player entity is teleported from one location to another. |
| Cancelled | Boolean | Gets the cancellation state of this event. |
| Entity | Entity | Returns the Entity involved in this event |
| Entity Type | Entity Type | Gets the EntityType of the Entity involved in this event. |
| From | Location | Gets the location that this entity moved from |
| To | Location | Gets the location that this entity moved to |

Goes on with: **Next**

## Mutate EntityDamage

`event_data.entity_damage.mutate`

Changes the properties of a EntityDamage that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| EntityDamage | EntityDamageEvent | Stores data for damage events |
| Damage (optional) | Number | Sets the raw amount of damage caused by the event. |

Goes on with: **Next**

## Mutate EntityDeath

`event_data.entity_death.mutate`

Changes the properties of a EntityDeath that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| EntityDeath | EntityDeathEvent | Thrown whenever a LivingEntity dies |
| Death Sound (optional) | Sound | Set the sound that the entity makes when dying |
| Death Sound Category (optional) | SoundCategory | Set the sound category that the death sound should play in. |
| Death Sound Pitch (optional) | Float | Set the pitch that the death sound should play with. |
| Death Sound Volume (optional) | Float | Set the volume the death sound should play at. |
| Dropped Exp (optional) | Integer | Sets how much EXP should be dropped from this death. |
| Revive Health (optional) | Number | Set the amount of health that the entity should revive with after cancelling the event. |
| Should Play Death Sound (optional) | Boolean | Set whether the death sound should play when the entity dies. |

Goes on with: **Next**

## Mutate EntityExplode

`event_data.entity_explode.mutate`

Changes the properties of a EntityExplode that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| EntityExplode | EntityExplodeEvent | Called when an entity explodes interacting with blocks. |
| Yield (optional) | Float | Sets the percentage of blocks to drop from this explosion |

Goes on with: **Next**

## Mutate EntityTarget

`event_data.entity_target.mutate`

Changes the properties of a EntityTarget that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| EntityTarget | EntityTargetEvent | Called when a creature targets or untargets another entity |
| Target (optional) | Entity | Set the entity that you want the mob to target instead. |

Goes on with: **Next**

## Mutate EntityTeleport

`event_data.entity_teleport.mutate`

Changes the properties of a EntityTeleport that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| EntityTeleport | EntityTeleportEvent | Thrown when a non-player entity is teleported from one location to another. |
| From (optional) | Location | Sets the location that this entity moved from |
| To (optional) | Location | Sets the location that this entity moved to |

Goes on with: **Next**

## Mutate Event

`cancellable.mutate`

Changes the properties of a Event that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Event | Cancellable | A type characterizing events that may be cancelled by a plugin or the server. |
| Cancelled (optional) | Boolean | Sets the cancellation state of this event. |

| Out | Type | What it is |
|---|---|---|
| Cancelled | Boolean | Gets the cancellation state of this event. |

Goes on with: **Next**

## Mutate ExplosionPrime

`event_data.explosion_prime.mutate`

Changes the properties of a ExplosionPrime that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| ExplosionPrime | ExplosionPrimeEvent | Called when an entity has made a decision to explode. |
| Fire (optional) | Boolean | Sets whether this explosion will create fire or not |
| Radius (optional) | Float | Sets the radius of the explosion |

Goes on with: **Next**

## Read Event

`cancellable.read`

Reads the properties of a Event

| In | Type | What it is |
|---|---|---|
| Event | Cancellable | A type characterizing events that may be cancelled by a plugin or the server. |

| Out | Type | What it is |
|---|---|---|
| Cancelled | Boolean | Gets the cancellation state of this event. |
