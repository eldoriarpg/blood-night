---
title: The world
description: "The world: every node of this group, with its inputs and outputs."
sidebar_position: 18
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# The world

## Block At

`world.block_at`

The block at a location

| In | Type | What it is |
|---|---|---|
| World | World | The world to look in |
| Location | Location | Where the block is |

| Out | Type | What it is |
|---|---|---|
| Result | Block | The block; on Folia missing when the location is in another region |

## Create Block Data

`block_type.create_block_data`

Creates a new BlockData instance for this block type, with all properties initialized to unspecified defaults.

| In | Type | What it is |
|---|---|---|
| Block Type | Block Type | Represents a block type. |

| Out | Type | What it is |
|---|---|---|
| Result | Block Data | new data instance |

## Create Dust

`dust.create`

Creates a Dust

| In | Type | What it is |
|---|---|---|
| Color | Color | The colour of the dust |
| Size | Float | How big each particle is, 1 for normal |

| Out | Type | What it is |
|---|---|---|
| Dust | Dust | Options which can be applied to dust particles - a particle color and size. |

## Create Dust Transition

`dust_transition.create`

Creates a Dust Transition

| In | Type | What it is |
|---|---|---|
| From | Color | The colour it starts with |
| To | Color | The colour it fades to |
| Size | Float | How big each particle is, 1 for normal |

| Out | Type | What it is |
|---|---|---|
| Dust Transition | Dust Transition | Options which can be applied to a color transitioning dust particles. |

## Create Spell

`spell.create`

Creates a Spell

| In | Type | What it is |
|---|---|---|
| Color | Color | The colour of the spell |
| Power | Float | How strongly it shows |

| Out | Type | What it is |
|---|---|---|
| Spell | Spell | Options which can be applied to effect particles. |

## Create Trail

`trail.create`

Creates a Trail

| In | Type | What it is |
|---|---|---|
| Target | Location | Where the trail leads |
| Color | Color | The colour of the trail |
| Duration | Integer | How long it travels, in ticks |

| Out | Type | What it is |
|---|---|---|
| Trail | Trail | Options which can be applied to trail particles - a location, color and duration. |

## Create Vibration

`vibration.create`

Data for the vibration particle: travels to a location within some ticks

| In | Type | What it is |
|---|---|---|
| Destination | Location | Where the vibration travels to |
| Ticks | Integer | How long it travels |

| Out | Type | What it is |
|---|---|---|
| Vibration | Vibration | The vibration, as data for the vibration particle |

## Entities Near Location

`world.nearby_entities`

Entities within a box around a location; x, y and z are half the box size

| In | Type | What it is |
|---|---|---|
| Location | Location | The centre of the search |
| X | Number | How far to search east and west |
| Y | Number | How far to search up and down |
| Z | Number | How far to search north and south |

| Out | Type | What it is |
|---|---|---|
| Result | Collection&lt;Entity&gt; | The entities in that box; on Folia empty when the box reaches into another region |

## Line Trace

`world.line_trace`

Follows a line from a location and tells what it meets first, ignoring fluids and passable blocks such as grass

| In | Type | What it is |
|---|---|---|
| Start | Location | Where the line starts, e.g. the eyes of the mob |
| Direction | Vector | Where the line goes, e.g. the direction the mob looks in |
| Distance | Number | How far the line goes |
| Ignore (optional) | Entity | An entity the line passes through, e.g. the mob itself |

| Out | Type | What it is |
|---|---|---|
| Hit | Boolean | Whether the line met a block or entity |
| Location | Location | Where it met it; missing without a hit |
| Entity | Entity | The entity it met; missing when it met a block or nothing |
| Block | Block | The block it met; missing when it met an entity or nothing |
| Distance | Number | How far from the start it met something, the full distance without a hit |

Goes on with: **Next**

## Mutate World

`world.mutate`

Changes the properties of a World that are given, the others stay as they are, and gives every property's value after the change

| In | Type | What it is |
|---|---|---|
| World | World | Represents a world, which may contain entities, chunks and blocks |
| Allow Monster Spawning (optional) | Boolean | Sets if this world can spawn monsters. |
| Auto Save (optional) | Boolean | Sets whether or not the world will automatically save |
| Clear Weather Duration (optional) | Integer | Set the clear weather duration. |
| Difficulty (optional) | Difficulty | Sets the Difficulty of the world. |
| Full Time (optional) | Long | Sets the in-game time on the server Note that this sets the full time of the world, which may cause adverse effects such as breaking redstone clocks and any scheduled events |
| Hardcore (optional) | Boolean | Sets whether the world is hardcore or not. |
| Send View Distance (optional) | Integer | Sets the sending view distance for this world. |
| Simulation Distance (optional) | Integer | Sets the simulation distance for this world. |
| Spawn Location (optional) | Location | Sets the spawn location of the world. |
| Storm (optional) | Boolean | Set whether there is a storm. |
| Thunder Duration (optional) | Integer | Set the thundering duration. |
| Thundering (optional) | Boolean | Set whether it is thundering. |
| Time (optional) | Long | Sets the relative in-game time on the server. |
| View Distance (optional) | Integer | Sets the view distance for this world. |
| Void Damage Amount (optional) | Float | Sets the damage applied to the entities when they are in the void in this world. |
| Void Damage Enabled (optional) | Boolean | Sets whether void damage is enabled on this world. |
| Void Damage Min Build Height Offset (optional) | Number | Sets the offset applied to getMinHeight() to determine the height at which void damage starts to apply. |
| Weather Duration (optional) | Integer | Set the remaining time in ticks of the current conditions. |

| Out | Type | What it is |
|---|---|---|
| Allow Monsters | Boolean | Gets whether monsters can spawn in this world. |
| Auto Save | Boolean | Gets whether or not the world will automatically save |
| Bed Works | Boolean | Gets if beds work in this world. |
| Chunk Count | Integer | The amount of chunks in this world |
| Clear Weather | Boolean | Returns whether the world has clear weather. |
| Clear Weather Duration | Integer | Get the clear weather duration. |
| Coordinate Scale | Number | Gets the coordinate scaling of this world. |
| Day Time | Boolean | Check if it is currently daytime in this world |
| Difficulty | Difficulty | Gets the Difficulty of the world. |
| Entities | List&lt;Entity&gt; | Get a list of all entities in this World |
| Entity Count | Integer | The amount of entities in this world |
| Environment | Environment | Gets the World.Environment type of this world |
| Fixed Time | Boolean | Checks if this world has a fixed time |
| Full Time | Long | Gets the full in-game time on this world, or 0 if this world does not have a world clock. |
| Game Time | Long | Gets the full in-game time on this world since the world generation |
| Hardcore | Boolean | Gets whether the world is hardcore or not. |
| Infiniburn | Collection&lt;Material&gt; | Gets the collection of materials that burn infinitely in this world. |
| Listening Plugin Channels | Set&lt;Text&gt; | Gets a set containing all the Plugin Channels that this client is listening on. |
| Living Entities | List&lt;Living Entity&gt; | Get a list of all living entities in this World |
| Logical Height | Integer | Gets the maximum height to which chorus fruits and nether portals can bring players within this dimension. |
| Max Height | Integer | Gets the maximum height of this world. |
| Min Height | Integer | Gets the minimum height of this world. |
| Moon Phase | MoonPhase | the current moon phase at the current time in the world |
| Name | Text | Gets the legacy Bukkit name of this world. |
| Piglin Safe | Boolean | Gets if this world allow to piglins to survive without shaking and transforming to zombified piglins. |
| Player Count | Integer | The amount of players in this world |
| Players | List&lt;Player&gt; | Get a list of all players in this World |
| Respawn Anchor Works | Boolean | Gets if this world allows players to charge and use respawn anchors. |
| Sea Level | Integer | Gets the sea level for this world. |
| Seed | Long | Gets the Seed for this world. |
| Send View Distance | Integer | Gets the sending view distance for this world. |
| Simulation Distance | Integer | Returns the simulation distance used for this world. |
| Spawn Location | Location | Gets the default spawn Location of this world |
| Thunder Duration | Integer | Get the thundering duration. |
| Thundering | Boolean | Returns whether there is thunder. |
| Tickable Tile Entity Count | Integer | The amount of tickable block entities in this world |
| Tile Entity Count | Integer | The amount of block entities in this world |
| Time | Long | Gets the relative in-game time of this world, or 0 if this world does not have a world clock. |
| Uid | UUID | Gets the Unique ID of this world |
| View Distance | Integer | Returns the view distance used for this world. |
| Void Damage Amount | Float | Gets the damage applied to the entities when they are in the void in this world. |
| Void Damage Enabled | Boolean | Checks if void damage is enabled on this world. |
| Void Damage Min Build Height Offset | Number | Gets the offset applied to getMinHeight() to determine the height at which void damage starts to apply. |
| Weather Duration | Integer | Get the remaining time in ticks of the current conditions. |

Goes on with: **Next**

## Nearest Player

`world.nearest_player`

The closest player within a radius, missing when there is none

| In | Type | What it is |
|---|---|---|
| Location | Location | Where to search from |
| Radius | Number | How far to search |
| Survival Only (optional) | Boolean | Whether only players in survival or adventure count |

| Out | Type | What it is |
|---|---|---|
| Player | Player | The closest player; missing when none is in range, and on Folia when the range reaches into another region |

## Particle Delay

`particle.delay`

Data for particles that appear after a delay in ticks, such as shriek

| In | Type | What it is |
|---|---|---|
| Ticks | Integer | How long the particle waits before it shows |

| Out | Type | What it is |
|---|---|---|
| Delay | Integer | The delay, as data for particles such as shriek |

## Particle Power

`particle.power`

Data for particles that take a strength, such as dragon breath and sculk charge

| In | Type | What it is |
|---|---|---|
| Power | Number | The strength the particle shows, e.g. of dragon breath |

| Out | Type | What it is |
|---|---|---|
| Power | Float | The strength the particle shows, e.g. of dragon breath |

## Read Dust

`dust.read`

Reads the properties of a Dust

| In | Type | What it is |
|---|---|---|
| Dust | Dust | Options which can be applied to dust particles - a particle color and size. |

| Out | Type | What it is |
|---|---|---|
| Color | Color | The color of the particles to be displayed. |
| Size | Float | Relative size of the particle. |

## Read Dust Transition

`dust_transition.read`

Reads the properties of a Dust Transition

| In | Type | What it is |
|---|---|---|
| Dust Transition | Dust Transition | Options which can be applied to a color transitioning dust particles. |

| Out | Type | What it is |
|---|---|---|
| Color | Color | The color of the particles to be displayed. |
| Size | Float | Relative size of the particle. |
| To Color | Color | The final of the particles to be displayed. |

## Read Spell

`spell.read`

Reads the properties of a Spell

| In | Type | What it is |
|---|---|---|
| Spell | Spell | Options which can be applied to effect particles. |

| Out | Type | What it is |
|---|---|---|
| Color | Color | The color of the particles to be displayed. |
| Power | Float | The power of the particles to be displayed. |

## Read Trail

`trail.read`

Reads the properties of a Trail

| In | Type | What it is |
|---|---|---|
| Trail | Trail | Options which can be applied to trail particles - a location, color and duration. |

| Out | Type | What it is |
|---|---|---|
| Color | Color | The color of the particles to be displayed. |
| Duration | Integer | The duration of the trail to be displayed. |
| Target | Location | The target of the particles to be displayed. |

## Read World

`world.read`

Reads the properties of a World

| In | Type | What it is |
|---|---|---|
| World | World | Represents a world, which may contain entities, chunks and blocks |

| Out | Type | What it is |
|---|---|---|
| Allow Monsters | Boolean | Gets whether monsters can spawn in this world. |
| Auto Save | Boolean | Gets whether or not the world will automatically save |
| Bed Works | Boolean | Gets if beds work in this world. |
| Chunk Count | Integer | The amount of chunks in this world |
| Clear Weather | Boolean | Returns whether the world has clear weather. |
| Clear Weather Duration | Integer | Get the clear weather duration. |
| Coordinate Scale | Number | Gets the coordinate scaling of this world. |
| Day Time | Boolean | Check if it is currently daytime in this world |
| Difficulty | Difficulty | Gets the Difficulty of the world. |
| Entities | List&lt;Entity&gt; | Get a list of all entities in this World |
| Entity Count | Integer | The amount of entities in this world |
| Environment | Environment | Gets the World.Environment type of this world |
| Fixed Time | Boolean | Checks if this world has a fixed time |
| Full Time | Long | Gets the full in-game time on this world, or 0 if this world does not have a world clock. |
| Game Time | Long | Gets the full in-game time on this world since the world generation |
| Hardcore | Boolean | Gets whether the world is hardcore or not. |
| Infiniburn | Collection&lt;Material&gt; | Gets the collection of materials that burn infinitely in this world. |
| Listening Plugin Channels | Set&lt;Text&gt; | Gets a set containing all the Plugin Channels that this client is listening on. |
| Living Entities | List&lt;Living Entity&gt; | Get a list of all living entities in this World |
| Logical Height | Integer | Gets the maximum height to which chorus fruits and nether portals can bring players within this dimension. |
| Max Height | Integer | Gets the maximum height of this world. |
| Min Height | Integer | Gets the minimum height of this world. |
| Moon Phase | MoonPhase | the current moon phase at the current time in the world |
| Name | Text | Gets the legacy Bukkit name of this world. |
| Piglin Safe | Boolean | Gets if this world allow to piglins to survive without shaking and transforming to zombified piglins. |
| Player Count | Integer | The amount of players in this world |
| Players | List&lt;Player&gt; | Get a list of all players in this World |
| Respawn Anchor Works | Boolean | Gets if this world allows players to charge and use respawn anchors. |
| Sea Level | Integer | Gets the sea level for this world. |
| Seed | Long | Gets the Seed for this world. |
| Send View Distance | Integer | Gets the sending view distance for this world. |
| Simulation Distance | Integer | Returns the simulation distance used for this world. |
| Spawn Location | Location | Gets the default spawn Location of this world |
| Thunder Duration | Integer | Get the thundering duration. |
| Thundering | Boolean | Returns whether there is thunder. |
| Tickable Tile Entity Count | Integer | The amount of tickable block entities in this world |
| Tile Entity Count | Integer | The amount of block entities in this world |
| Time | Long | Gets the relative in-game time of this world, or 0 if this world does not have a world clock. |
| Uid | UUID | Gets the Unique ID of this world |
| View Distance | Integer | Returns the view distance used for this world. |
| Void Damage Amount | Float | Gets the damage applied to the entities when they are in the void in this world. |
| Void Damage Enabled | Boolean | Checks if void damage is enabled on this world. |
| Void Damage Min Build Height Offset | Number | Gets the offset applied to getMinHeight() to determine the height at which void damage starts to apply. |
| Weather Duration | Integer | Get the remaining time in ticks of the current conditions. |

## Spawn Particle

`world.spawn_particle`

Spawns particles around a location; particles such as dust need data, connected from a node like Dust

| In | Type | What it is |
|---|---|---|
| World | World | The world to spawn them in |
| Particle | Particle | The kind of particle |
| Location | Location | Where they appear |
| Count | Integer | How many |
| Offset X | Number | Spread east and west |
| Offset Y | Number | Spread up and down |
| Offset Z | Number | Spread north and south |
| Data (optional) | Object | What particles such as dust need, from a node like Dust; leave empty for others |

Goes on with: **Next**

## Spawn Potion Cloud

`world.spawn_potion_cloud`

Spawns a lingering cloud that applies a potion to everything inside

| In | Type | What it is |
|---|---|---|
| Location | Location | Where the cloud appears |
| Potion | Potion | The effect it gives |
| Seconds | Integer | How long it lasts |
| Radius | Number | Its size when it appears |
| Radius Per Tick (optional) | Number | How much it grows per tick; negative to shrink |
| Color (optional) | Color | Its colour |

Goes on with: **Next**

## Strike Lightning

`world.strike_lightning`

Strikes lightning at a location; an effect only strike looks the same but deals no damage and sets nothing on fire

| In | Type | What it is |
|---|---|---|
| World | World | The world to strike in |
| Location | Location | Where it strikes |
| Effect Only (optional) | Boolean | Only the look and sound, no damage and no fire; false when not given |

Goes on with: **Next**
