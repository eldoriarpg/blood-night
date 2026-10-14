# BloodNight API

`:api`: the plugin's public API. It holds the two types generated mob behaviours share with the plugin, the service
other plugins ask BloodNight through, and the events they can listen to.

## Overview

The module is small on purpose. It contains no logic, only the contract between three parties:

- **Generated behaviour code.** Every custom mob's node graph is compiled at runtime into a class implementing
  `CompiledBehaviour`. Its constructor receives a `MobContext`. These two interfaces are the only types the plugin and
  generated code share.
- **The node platform** in [bukkit-nodes](../bukkit-nodes/README.md), which builds one entry node per
  `CompiledBehaviour` method and reads the mob and its items through `MobContext`.
- **Other plugins**, which ask BloodNight through `BloodNightService` and react to it through the events in
  `api.event`.

`CompiledBehaviour` has one method per trigger, and every method is a no-op by default, so a generated class overrides
only the triggers its graph uses. Each living mob gets its own instance, which is why state such as cooldowns is per
mob.

Both packages are `@NullMarked` (JSpecify): everything is non-null unless annotated `@Nullable`.

## Layout

- `de.eldoria.bloodnight.api.CompiledBehaviour`: behaviour of a custom mob. Lifecycle methods (`onSpawn`,
  `onNightEnd`, `onTick`, `onDebug`) and event methods that receive the Bukkit event (`onDamage`,
  `onDamageByEntity`, `onDamageByPlayer`, `onHit`, `onKill`, `onDeath`, `onTeleport`,
  `onTargetChange`, `onProjectileShoot`, `onProjectileHit`, `onExplosionPrime`, `onExplode`, `onExtensionDamage`,
  `onExtensionDeath`).
- `de.eldoria.bloodnight.api.MobContext`: what a behaviour knows about its mob: the entity, its extension (the entity
  riding or carrying it, if any) and `item(id)`, a copy of an item from the plugin's item registry.
- `de.eldoria.bloodnight.api.BloodNightService`: whether and which blood nights run (`RunningNight`), the next one
  (`NightForecast`), the chance of a coming night, force and cancel; whether an entity is a BloodNight mob, its id,
  and spawning a mob. Registered with Paper's services manager; `BloodNightProvider.get()` finds it from anywhere.
- `de.eldoria.bloodnight.api.event.BloodNightBeginEvent`: a blood night is about to begin in a world. Cancellable;
  cancelling it keeps the night an ordinary one.
- `de.eldoria.bloodnight.api.event.BloodNightEndEvent`: a blood night has ended in a world; `reason()` tells why
  (`TIME`, `CANCELLED`, `DISABLED`, `RELEASED` after a dragon fight, `SHUTDOWN`).
- `de.eldoria.bloodnight.api.event.CustomMobSpawnEvent`: a spawning entity is about to become a BloodNight mob.
  Cancellable; cancelling it keeps the entity as it is.
- `de.eldoria.bloodnight.api.event.CustomMobDeathEvent`: a BloodNight mob died; its drops can still be changed.

Nights carry their number, `night()`: the day the night begins on, or the period for worlds without a day cycle (the
Nether and the End have timed blood nights). The night events are world events, so `getWorld()` names the world.

## Asking BloodNight

```java
BloodNightService bloodNight = BloodNightProvider.get();
if (bloodNight.isRunning(world)) { ... }
bloodNight.next(world, 30).ifPresent(next -> ...);
bloodNight.customMobId(entity).ifPresent(id -> ...);
```

On Folia, `force` and `cancel` belong on the global region and `spawn` on the region owning the location; the queries
can be called from anywhere.

## Using it

The module is published to EldoNexus with every release:

```kotlin
repositories { maven("https://eldonexus.de/repository/maven-public/") }
dependencies { compileOnly("de.eldoria.bloodnight:api:<version>") }
```

Depend on BloodNight in your `paper-plugin.yml` so it loads first.

## Listening to blood nights

```java
@EventHandler
public void onBegin(BloodNightBeginEvent event) {
    if (event.getWorld().getName().equals("lobby")) event.setCancelled(true);
}
```

The plugin fires `BloodNightBeginEvent` from its regular night check (and when an admin forces a night that has
already begun). `BloodNightEndEvent` fires when the world's time leaves the night, when an admin cancels a running
blood night, when the world is disabled while one runs, and for every running blood night when the plugin disables.

## Dependencies

Depends on: the Paper API (`compileOnly`, through the `bloodnight.paper` convention plugin) and JSpecify.

Used by: [bukkit-nodes](../bukkit-nodes/README.md) and [core](../core/README.md) (both as `api` dependencies, so it
reaches [catalog](../catalog/README.md) through bukkit-nodes).

## Building and testing

The module has no tests of its own; it is built with everything else:

```sh
./toolchain.sh be-build     # formatting check, tests, SpotBugs, jars
./toolchain.sh be-format    # apply formatting and license headers
```
