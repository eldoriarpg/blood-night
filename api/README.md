# BloodNight API

`:api`: the plugin's public API. It holds the two types generated mob behaviours share with the plugin, and the
events other plugins can listen to.

## Overview

The module is small on purpose. It contains no logic, only the contract between three parties:

- **Generated behaviour code.** Every custom mob's node graph is compiled at runtime into a class implementing
  `CompiledBehaviour`. Its constructor receives a `MobContext`. These two interfaces are the only types the plugin and
  generated code share.
- **The node platform** in [bukkit-nodes](../bukkit-nodes/README.md), which builds one entry node per
  `CompiledBehaviour` method and reads the mob and its items through `MobContext`.
- **Other plugins**, which can react to blood nights through the events in `api.event`.

`CompiledBehaviour` has one method per trigger, and every method is a no-op by default, so a generated class overrides
only the triggers its graph uses. Each living mob gets its own instance, which is why state such as cooldowns is per
mob.

Both packages are `@NullMarked` (JSpecify): everything is non-null unless annotated `@Nullable`.

## Layout

- `de.eldoria.bloodnight.api.CompiledBehaviour`: behaviour of a custom mob. Lifecycle methods (`onSpawn`,
  `onNightEnd`, `onTick`, `onDebug`) and event methods that receive the Bukkit event (`onDamage`,
  `onDamageByEntity`, `onDamageByPlayer`, `onHit`, `onKill`, `onPlayerKill`, `onEntityKill`, `onDeath`, `onTeleport`,
  `onTargetChange`, `onProjectileShoot`, `onProjectileHit`, `onExplosionPrime`, `onExplode`, `onExtensionDamage`,
  `onExtensionDeath`).
- `de.eldoria.bloodnight.api.MobContext`: what a behaviour knows about its mob: the entity, its extension (the entity
  riding or carrying it, if any) and `item(id)`, a copy of an item from the plugin's item registry.
- `de.eldoria.bloodnight.api.event.BloodNightBeginEvent`: a blood night is about to begin in a world. Cancellable;
  cancelling it keeps the night an ordinary one.
- `de.eldoria.bloodnight.api.event.BloodNightEndEvent`: a blood night has ended in a world, because the night is over
  or because it was cancelled while running.

Both events carry the night's number, `night()`: the day the night begins on, `fullTime / 24000`. They are world
events, so `getWorld()` names the world.

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
