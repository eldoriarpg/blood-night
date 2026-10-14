---
title: Players
sidebar_position: 14
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Players

## Mutate Player

`player.mutate`

Changes the properties of a Player that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| Player | Player | Represents a player, connected or not |
| Affects Spawning (optional) | Boolean | Set whether the player can affect mob spawning |
| Allow Flight (optional) | Boolean | Sets if the Player is allowed to fly via jump key double-tap like in creative mode. |
| Compass Target (optional) | Location | Set the target of the player's compass. |
| Custom Chat Completions (optional) | Collection&lt;Text&gt; | Set the list of chat completion suggestions shown to the player while typing a message. |
| Death Screen Score (optional) | Integer | Set the score that shows in the death screen of the player. |
| Enchantment Seed (optional) | Integer | Sets the player's enchantment seed. |
| Exhaustion (optional) | Float | Sets the players current exhaustion level |
| Exp (optional) | Float | Sets the players current experience points towards the next level This is a percentage value. |
| Exp Cooldown (optional) | Integer | Sets the player's cooldown between picking up experience orbs. |
| Experience Level And Progress (optional) | Integer | Updates the players level and level progress to that what would be reached when the total amount of experience had been collected. |
| Fly Speed (optional) | Float | Sets the speed at which a client will fly. |
| Flying (optional) | Boolean | Makes this player start or stop flying. |
| Flying Fall Damage (optional) | TriState | Allows you to enable fall damage while getAllowFlight() is true |
| Food Level (optional) | Integer | Sets the players current food level |
| Game Mode (optional) | GameMode | Sets this human's current GameMode |
| Has Seen Win Screen (optional) | Boolean | Changes whether this player has seen the win screen before. |
| Health Scale (optional) | Number | Sets the number to scale health to for the client; this will also setHealthScaled(true). |
| Health Scaled (optional) | Boolean | Sets if the client is displayed a 'scaled' health, that is, health on a scale from 0-getHealthScale(). |
| Hurt Direction (optional) | Float | Sets player hurt direction |
| Item On Cursor (optional) | Item | Sets the item to the given ItemStack, this will replace whatever the user was moving. |
| Last Death Location (optional) | Location | Sets the player's last death location. |
| Level (optional) | Integer | Sets the players current experience level |
| Player List Order (optional) | Integer | Sets the relative order that the player is shown on the in-game player list. |
| Player Weather (optional) | WeatherType | Sets the type of weather the player will see. |
| Respawn Location (optional) | Location | Sets the Location where the player will respawn. |
| Saturated Regen Rate (optional) | Integer | Set the regeneration rate (1 health per x ticks) of the HumanEntity when they have saturation and their food level is &gt;= 20. |
| Saturation (optional) | Float | Sets the players current saturation level |
| Send View Distance (optional) | Integer | Sets the sending view distance for this player. |
| Simulation Distance (optional) | Integer | Sets the simulation distance for this player |
| Sleeping Ignored (optional) | Boolean | Sets whether the player is ignored as not sleeping. |
| Sneaking (optional) | Boolean | Sets the sneak mode the player |
| Spectator Target (optional) | Entity | Sets the entity which is followed by the camera when in SPECTATOR. |
| Sprinting (optional) | Boolean | Sets whether the player is sprinting or not. |
| Starvation Rate (optional) | Integer | Set the starvation rate (1 health per x ticks) of the HumanEntity. |
| Total Experience (optional) | Integer | Sets the players current experience points. |
| Unsaturated Regen Rate (optional) | Integer | Get the regeneration rate (1 health per x ticks) of the HumanEntity when they have no saturation and their food level is &gt;= 18. |
| View Distance (optional) | Integer | Sets the view distance for this player |
| Walk Speed (optional) | Float | Sets the speed at which a client will walk. |
| Warden Time Since Last Warning (optional) | Integer | Sets time since last Warden warning in ticks. |
| Warden Warning Cooldown (optional) | Integer | Sets the player's cooldown in ticks until next Warden warning can occur. |
| Warden Warning Level (optional) | Integer | Sets the player's Warden warning level. |
| Whitelisted (optional) | Boolean | Sets if this player is whitelisted or not |
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
| Ticks Lived (optional) | Integer | Sets the amount of ticks this entity has lived for. |
| Velocity (optional) | Vector | Sets this entity's velocity in meters per tick |
| Visible By Default (optional) | Boolean | Sets whether or not this entity is visible by default. |
| Visual Fire (optional) | TriState | Sets if the entity has visual fire (it will always appear to be on fire). |

| Out | Type | What it is |
|---|---|---|
| Affects Spawning | Boolean | Get whether the player can affect mob spawning |
| Allow Flight | Boolean | Determines if the Player is allowed to fly via jump key double-tap like in creative mode. |
| Allowing Server Listings | Boolean | Gets whether the player has the "Allow Server Listings" setting enabled. |
| Attack Cooldown | Float | Gets the current cooldown for a player's attack. |
| Banned | Boolean | Checks if this player has had their profile banned. |
| Bed Location | Location | Gets the location of the bed the player is currently sleeping in |
| Blocking | Boolean | Check if the player is currently blocking (ie with a shield). |
| Client Brand Name | Text | Returns player's client brand name. |
| Client View Distance | Integer | Get the player's current client side view distance. |
| Compass Target | Location | Get the previously set compass target. |
| Connected | Boolean | Checks whether the connection to this player is still valid. |
| Conversing | Boolean | Tests to see of a Conversable object is actively engaged in a conversation. |
| Cooldown Period | Float | Returns the amount of ticks the current cooldown lasts |
| Death Screen Score | Integer | Get the score that shows in the death screen of the player. |
| Deeply Sleeping | Boolean | If the player has slept enough to count towards passing the night. |
| Enchantment Seed | Integer | Gets the player's current enchantment seed. |
| Ender Pearls | Collection&lt;EnderPearl&gt; | Gets the ender pearls currently associated with this entity. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Exhaustion | Float | Gets the players current exhaustion level. |
| Exp | Float | Gets the players current experience points towards the next level. |
| Exp Cooldown | Integer | Gets the player's cooldown between picking up experience orbs. |
| Exp To Level | Integer | Get the total amount of experience required for the player to level |
| Experience Points Needed For Next Level | Integer | Gets the total amount of experience points that are needed to reach the next level from zero progress towards it. |
| First Played | Long | Gets the first date and time that this player was witnessed on this server. |
| Fish Hook | FishHook | the player's fishing hook if they are fishing |
| Fly Speed | Float | Gets the current allowed speed that a client can fly. |
| Flying | Boolean | Checks to see if this player is currently flying or not. |
| Food Level | Integer | Gets the players current food level |
| Game Mode | GameMode | Gets this human's current GameMode |
| Health Scale | Number | Gets the number that health is scaled to for the client. |
| Health Scaled | Boolean | Gets if the client is displayed a 'scaled' health, that is, health on a scale from 0-getHealthScale(). |
| Item On Cursor | Item | Returns the ItemStack currently on your cursor, can be empty. |
| Last Death Location | Location | Gets the player's last death location. |
| Last Login | Long | Gets the last date and time that this player logged into the server. |
| Last Seen | Long | Gets the last date and time that this player was seen on the server. |
| Level | Integer | Gets the players current experience level |
| Listening Plugin Channels | Set&lt;Text&gt; | Gets a set containing all the Plugin Channels that this client is listening on. |
| Location | Location | Gets the player's current location. |
| Main Hand | MainHand | Gets the player's selected main hand |
| Name | Text | \{@inheritDoc\} |
| Online | Boolean | Checks if this player is currently online It should be noted that this will return true if any instance of this player is online! |
| Ping | Integer | Gets the player's estimated ping in milliseconds. |
| Player | Player | Gets a Player object that this represents, if there is one If the player is online, this will return that player. |
| Player List Order | Integer | Gets the relative order that the player is shown on the player list. |
| Player Time | Long | Returns the player's current timestamp. |
| Player Time Offset | Long | Returns the player's current time offset relative to server time, or the current player's fixed time if the player's time is absolute. |
| Player Time Relative | Boolean | Returns true if the player's time is relative to the server time, otherwise the player's time is absolute and will not change its current time unless done so with setPlayerTime(). |
| Player Weather | WeatherType | Returns the type of weather the player is currently experiencing. |
| Previous Game Mode | GameMode | Gets this player's previous GameMode |
| Protocol Version | Integer | Returns the protocol version of the client. |
| Resource Pack Status | Status | Gets the most recent resource pack status from the player. |
| Respawn Location | Location | Gets the Location where the player will spawn at, null if they don't have a valid respawn point. |
| Saturated Regen Rate | Integer | Get the regeneration rate (1 health per x ticks) of the HumanEntity when they have saturation and their food level is &gt;= 20. |
| Saturation | Float | Gets the players current saturation level. |
| Send View Distance | Integer | Gets the sending view distance for this player. |
| Sent Chunk Keys | Set&lt;Long&gt; | Gets the set of chunk keys for all chunks that have been sent to the player. |
| Simulation Distance | Integer | Gets the simulation distance for this player |
| Sleep Ticks | Integer | Get the sleep ticks of the player. |
| Sleeping Ignored | Boolean | Returns whether the player is sleeping ignored. |
| Sneaking | Boolean | Returns if the player is in sneak mode |
| Spectator Target | Entity | Gets the entity which is followed by the camera when in SPECTATOR. |
| Sprinting | Boolean | Gets whether the player is sprinting or not. |
| Starvation Rate | Integer | Get the starvation rate (1 health per x ticks) of the HumanEntity. |
| Total Experience | Integer | Gets the players total experience points. |
| Transferred | Boolean | Gets if this connection has been transferred from another server. |
| Unique Id | UUID | Returns the UUID of this player |
| Unsaturated Regen Rate | Integer | Get the regeneration rate (1 health per x ticks) of the HumanEntity when they have no saturation and their food level is &gt;= 18. |
| View Distance | Integer | Gets the view distance for this player |
| Walk Speed | Float | Gets the current allowed speed that a client can walk. |
| Warden Time Since Last Warning | Integer | Returns time since last Warden warning in ticks. |
| Warden Warning Cooldown | Integer | Returns the player's cooldown in ticks until the next Warden warning can occur. |
| Warden Warning Level | Integer | Returns the player's current Warden warning level. |
| Whitelisted | Boolean | Checks if this player is whitelisted or not |
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
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Max Fire Ticks | Integer | Returns the entity's maximum fire ticks. |
| Max Freeze Ticks | Integer | Returns the entity's maximum freeze ticks (amount of ticks before it will be fully frozen) |
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

## Play Sound

`player.play_sound`

Play a sound for a player at the location.

| In | Type | What it is |
|---|---|---|
| Player | Player | Represents a player, connected or not |
| Location | Location | The location to play the sound |
| Sound | Sound | The sound to play |
| Volume | Float | The volume of the sound |
| Pitch | Float | The pitch of the sound |

Goes on with: **Next**

## Read Player

`player.read`

Reads the properties of a Player

| In | Type | What it is |
|---|---|---|
| Player | Player | Represents a player, connected or not |

| Out | Type | What it is |
|---|---|---|
| Affects Spawning | Boolean | Get whether the player can affect mob spawning |
| Allow Flight | Boolean | Determines if the Player is allowed to fly via jump key double-tap like in creative mode. |
| Allowing Server Listings | Boolean | Gets whether the player has the "Allow Server Listings" setting enabled. |
| Attack Cooldown | Float | Gets the current cooldown for a player's attack. |
| Banned | Boolean | Checks if this player has had their profile banned. |
| Bed Location | Location | Gets the location of the bed the player is currently sleeping in |
| Blocking | Boolean | Check if the player is currently blocking (ie with a shield). |
| Client Brand Name | Text | Returns player's client brand name. |
| Client View Distance | Integer | Get the player's current client side view distance. |
| Compass Target | Location | Get the previously set compass target. |
| Connected | Boolean | Checks whether the connection to this player is still valid. |
| Conversing | Boolean | Tests to see of a Conversable object is actively engaged in a conversation. |
| Cooldown Period | Float | Returns the amount of ticks the current cooldown lasts |
| Death Screen Score | Integer | Get the score that shows in the death screen of the player. |
| Deeply Sleeping | Boolean | If the player has slept enough to count towards passing the night. |
| Enchantment Seed | Integer | Gets the player's current enchantment seed. |
| Ender Pearls | Collection&lt;EnderPearl&gt; | Gets the ender pearls currently associated with this entity. |
| Equipment | EntityEquipment | Gets the inventory with the equipment worn by the living entity. |
| Exhaustion | Float | Gets the players current exhaustion level. |
| Exp | Float | Gets the players current experience points towards the next level. |
| Exp Cooldown | Integer | Gets the player's cooldown between picking up experience orbs. |
| Exp To Level | Integer | Get the total amount of experience required for the player to level |
| Experience Points Needed For Next Level | Integer | Gets the total amount of experience points that are needed to reach the next level from zero progress towards it. |
| First Played | Long | Gets the first date and time that this player was witnessed on this server. |
| Fish Hook | FishHook | the player's fishing hook if they are fishing |
| Fly Speed | Float | Gets the current allowed speed that a client can fly. |
| Flying | Boolean | Checks to see if this player is currently flying or not. |
| Food Level | Integer | Gets the players current food level |
| Game Mode | GameMode | Gets this human's current GameMode |
| Health Scale | Number | Gets the number that health is scaled to for the client. |
| Health Scaled | Boolean | Gets if the client is displayed a 'scaled' health, that is, health on a scale from 0-getHealthScale(). |
| Item On Cursor | Item | Returns the ItemStack currently on your cursor, can be empty. |
| Last Death Location | Location | Gets the player's last death location. |
| Last Login | Long | Gets the last date and time that this player logged into the server. |
| Last Seen | Long | Gets the last date and time that this player was seen on the server. |
| Level | Integer | Gets the players current experience level |
| Listening Plugin Channels | Set&lt;Text&gt; | Gets a set containing all the Plugin Channels that this client is listening on. |
| Location | Location | Gets the player's current location. |
| Main Hand | MainHand | Gets the player's selected main hand |
| Name | Text | \{@inheritDoc\} |
| Online | Boolean | Checks if this player is currently online It should be noted that this will return true if any instance of this player is online! |
| Ping | Integer | Gets the player's estimated ping in milliseconds. |
| Player | Player | Gets a Player object that this represents, if there is one If the player is online, this will return that player. |
| Player List Order | Integer | Gets the relative order that the player is shown on the player list. |
| Player Time | Long | Returns the player's current timestamp. |
| Player Time Offset | Long | Returns the player's current time offset relative to server time, or the current player's fixed time if the player's time is absolute. |
| Player Time Relative | Boolean | Returns true if the player's time is relative to the server time, otherwise the player's time is absolute and will not change its current time unless done so with setPlayerTime(). |
| Player Weather | WeatherType | Returns the type of weather the player is currently experiencing. |
| Previous Game Mode | GameMode | Gets this player's previous GameMode |
| Protocol Version | Integer | Returns the protocol version of the client. |
| Resource Pack Status | Status | Gets the most recent resource pack status from the player. |
| Respawn Location | Location | Gets the Location where the player will spawn at, null if they don't have a valid respawn point. |
| Saturated Regen Rate | Integer | Get the regeneration rate (1 health per x ticks) of the HumanEntity when they have saturation and their food level is &gt;= 20. |
| Saturation | Float | Gets the players current saturation level. |
| Send View Distance | Integer | Gets the sending view distance for this player. |
| Sent Chunk Keys | Set&lt;Long&gt; | Gets the set of chunk keys for all chunks that have been sent to the player. |
| Simulation Distance | Integer | Gets the simulation distance for this player |
| Sleep Ticks | Integer | Get the sleep ticks of the player. |
| Sleeping Ignored | Boolean | Returns whether the player is sleeping ignored. |
| Sneaking | Boolean | Returns if the player is in sneak mode |
| Spectator Target | Entity | Gets the entity which is followed by the camera when in SPECTATOR. |
| Sprinting | Boolean | Gets whether the player is sprinting or not. |
| Starvation Rate | Integer | Get the starvation rate (1 health per x ticks) of the HumanEntity. |
| Total Experience | Integer | Gets the players total experience points. |
| Transferred | Boolean | Gets if this connection has been transferred from another server. |
| Unique Id | UUID | Returns the UUID of this player |
| Unsaturated Regen Rate | Integer | Get the regeneration rate (1 health per x ticks) of the HumanEntity when they have no saturation and their food level is &gt;= 18. |
| View Distance | Integer | Gets the view distance for this player |
| Walk Speed | Float | Gets the current allowed speed that a client can walk. |
| Warden Time Since Last Warning | Integer | Returns time since last Warden warning in ticks. |
| Warden Warning Cooldown | Integer | Returns the player's cooldown in ticks until the next Warden warning can occur. |
| Warden Warning Level | Integer | Returns the player's current Warden warning level. |
| Whitelisted | Boolean | Checks if this player is whitelisted or not |
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
| Invulnerable | Boolean | Gets whether the entity is invulnerable or not. |
| Max Fire Ticks | Integer | Returns the entity's maximum fire ticks. |
| Max Freeze Ticks | Integer | Returns the entity's maximum freeze ticks (amount of ticks before it will be fully frozen) |
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
