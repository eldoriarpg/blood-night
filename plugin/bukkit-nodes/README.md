# Bukkit Nodes

`:bukkit-nodes`: the Paper side of the node platform. It turns the generic graph compiler into the BloodNight
platform by adding Bukkit nodes, Bukkit types and the triggers that call a mob's behaviour.

## Overview

The graph modules ([graph-model](../../graph/model/README.md), [graph-compiler](../../graph/compiler/README.md),
[graph-std](../../graph/std/README.md)) know nothing about Minecraft. This module supplies everything Bukkit-specific
and assembles it in `BloodNightPlatform.create(classpath)`, a `GraphPlatform` that compiles graphs into classes
implementing `CompiledBehaviour` from [api](../api/README.md), each constructed with a `MobContext`.

Nodes come from three sources, all built in code (there is no Gradle code generation step in this module):

- **Hand-written nodes** in `BukkitNodes`: each is a descriptor plus a function that emits JavaPoet code, e.g.
  creating a location, vector math, nearby entities, the mob itself and its extension, applying an effect, launching
  a projectile, spawning a potion cloud, damage with an optional source, particles with optional data, lightning,
  a line trace (`world.line_trace`: what a line from a location meets first), particle data, and items from the
  registry.
- **Reflected nodes**: `BloodNightPlatform` lists Bukkit types (`Entity`, `LivingEntity`, `Player`, `Mob`, `Creeper`,
  `World`, `Location`, `Vector`, `ItemStack`, the particle data types, event classes and more) and derives getter,
  setter, action, query and constructor nodes from them by reflection. `BukkitRules` decides which methods and port
  types qualify; descriptions come from a `Documentation` source, which the [catalog](../catalog/README.md) fills from
  the Paper javadoc. Each reflected type has a read node (`<prefix>.read`) and a change node (`<prefix>.mutate`, its
  values after the change as outputs), inherited properties included; the editor shows them as one "Mutate" node
  (`object.mutate`) that turns into the variant of whatever is wired into it. Their ports are grouped by the entity
  type that brings each property (Entity, Living Entity, Mob, Creeper, Player, Projectile), the node's own type first.
- **Self and Extension** are typed per mob (`MobBindings`): a graph is compiled with `SELF` bound to the mob's entity
  type and `EXTENSION` to its extension's, so Self of a creeper mob is a Creeper; macros keep Living Entity and
  Entity.
- **Entry nodes**, one per `CompiledBehaviour` method: the event triggers of `Triggers` plus `mob.spawn`,
  `mob.night_end`, `mob.tick` and `debug.trigger`.

At runtime, `TriggerDispatcher` listens to the Bukkit events of the trigger table and calls the behaviour of the
custom mob each event concerns. It registers one executor per combination of event class, priority and
cancelled-handling, and only for triggers some compiled graph uses. A behaviour that throws is reported through a
callback and does not stop the event.

Colors are typed in as `#rrggbb` (`ColorCodec`), which the editor offers a color picker for. The platform's clock is
`CompiledBehaviour#onTick`, so delays hand their chains back on the server tick.

Values of registry-backed types (entity types, potion effects, items, blocks, particles, sounds, enchantments,
attributes, biomes, potions) are written as namespaced keys such as `minecraft:zombie` and resolved through Paper's
registries when the generated code runs.

## Layout

All classes are in `de.eldoria.bloodnight.graph`, runtime helpers in `de.eldoria.bloodnight.graph.runtime`.

- `BloodNightPlatform`: assembles the platform and the full node list (standard, Bukkit, the typed cast and value nodes, reflected,
  entries); the kill entry gets a victim picker (`KillVictim`).
- `BloodNightNodes`: node type id constants (extends `StdNodes`), for building graphs in code.
- `BukkitNodes`: the hand-written Bukkit nodes.
- `BukkitTypes`: editor names, literal codecs and selectable macro input types of the Bukkit types.
- `BukkitRules`: reflection rules: skips event plumbing, accepts only port types the editor can handle.
- `ColorCodec`: colors as `#rrggbb`.
- `RegistryCodec`: reads and emits namespaced keys of a registry; knows the valid keys from the live server, or
  without one from Paper's generated key classes or enum constants.
- `ParticleData`: compile-time checks that a typed-in particle gets the data it needs.
- `Trigger`, `Triggers`: the trigger table: event class, which entity is "this mob", filter, priority.
- `TriggerDispatcher`: registers listeners for used triggers and dispatches to behaviours.
- `CompileClasspath`: finds the jars generated code compiles against inside a running server.
- `runtime.BukkitRuntime`: helpers generated code calls (`launchAt`, `potionCloud`, `nearestPlayer`, `attacker`,
  `killer`).
- `runtime.Registries`: registry lookup used by emitted code; `runtime.ProjectileKind`: projectiles a mob can launch;
  `runtime.Trace` and `runtime.TraceHits`: line traces and what they stop at.

## Extending

- **A hand-written node**: add an `ExpressionDefinition` (pure or value) or `StatementDefinition` (action) to
  `BukkitNodes`, list it in `BukkitNodes.all()`, and add its id to `BloodNightNodes` if graphs built in code use it.
  Code that needs more than one expression goes into `BukkitRuntime`.
- **Methods of a Bukkit type**: add `.action(...)`, `.query(...)`, `.constructor(...)` or `.factory(...)` to its
  `ReflectedType` in `BloodNightPlatform`. A new type a port may carry belongs in `BukkitTypes.KNOWN` and, with an
  editor name, in `BukkitTypes.mapping()`.
- **A trigger**: add a default method to `CompiledBehaviour` in [api](../api/README.md), a `Trigger` row to
  `Triggers.EVENTS` and an id constant to `BloodNightNodes`.

Node ids are stable: saved graphs and macros refer to them. The editor catalog picks up node changes on the next
build, see [catalog](../catalog/README.md).

## Dependencies

Depends on: [api](../api/README.md) and [graph-std](../../graph/std/README.md) (both exposed as `api`), the Paper API
(`compileOnly`); tests use MockBukkit.

Used by: [catalog](../catalog/README.md) and [core](../core/README.md).

## Building and testing

```sh
./toolchain.sh be-test1 bukkit-nodes '*BukkitNodes*'   # tests matching a pattern
./toolchain.sh be-build                                # everything Java
```

Tests run against MockBukkit. A MockBukkit method that is not implemented throws, which JUnit reports as a skip, and
the build fails on any skipped test.
