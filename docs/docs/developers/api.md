---
title: Plugin API
sidebar_position: 1
---

# Plugin API

Your own plugin can ask BloodNight what is going on, and react when something happens.

Every class and method is described in the [javadoc](pathname:///javadoc/index.html). This page shows how they fit
together.

## Add the API

The API is published with every release:

```kotlin
repositories {
    maven("https://eldonexus.de/repository/maven-public/")
}

dependencies {
    compileOnly("de.eldoria.bloodnight:api:<version>")
}
```

Every change on `main` is published too, as `<next version>-SNAPSHOT`. Use it to try what is coming before it is
released.

Add BloodNight as a dependency in your `paper-plugin.yml`, so your plugin loads after it.

## Ask BloodNight

```java
BloodNightService bloodNight = BloodNightProvider.get();

if (bloodNight.isRunning(world)) {
    // a blood night runs in this world
}
bloodNight.next(world, 30).ifPresent(next -> {
    // next.ticksUntil() game ticks until the next blood night
});
bloodNight.customMobId(entity).ifPresent(id -> {
    // the entity is the special mob with this id
});
```

| Method | What it gives you |
|---|---|
| `isRunning(world)` | Whether a blood night runs |
| `running(world)` | The running night: its number, ticks left, how much is left, whether a dragon fight holds it |
| `next(world, horizon)` | The next blood night within `horizon` nights |
| `probability(world, nightsAhead)` | The chance of a coming night, 0 to 100 |
| `force(world)`, `cancel(world)` | Make the coming night a blood night, or keep it normal |
| `runningWorlds()` | Every world with a blood night |
| `isCustomMob(entity)`, `customMobId(entity)` | Whether an entity is a special mob, or rides or carries one |
| `customMobIds()` | Every special mob on the server |
| `spawn(mobId, location)` | Spawn a special mob |
| `mob(mobId)`, `mob(entity)`, `mobs()` | A special mob's details: its names, entity type, store and weight |
| `weight(world, mobId)` | How often a mob is picked in a world; 0 when it is switched off there |
| `isEnabled(world)` | Whether the world has blood nights at all |
| `world(world)` | How the world has blood nights: night times, how nights are picked, how strong enemies get |

You can also get the service from Paper's services manager.

On Folia, call `force` and `cancel` on the global region and `spawn` on the region of the location.

### Mobs and worlds

A mob's name comes in every language it has. `name(language)` picks the one to show, the same way BloodNight does:

```java
bloodNight.mob(entity).ifPresent(mob -> {
    String name = mob.name(player.locale().getLanguage());
    // mob.entityType(), mob.store(), mob.weight()
});

WorldInfo info = bloodNight.world(world);
if (info.enabled() && !info.timed()) {
    // blood nights begin at info.beginTick().getAsLong()
}
```

`timed()` is true for worlds whose time stands still, like the Nether and the End. Blood nights there run by the
clock, so they have no begin and end time.

`CompiledBehaviour`, `MobContext` and `MovementPreset` are in the API for the code BloodNight writes from mob graphs.
Don't use them: they can change in any version.

## Events

| Event | When | Can be cancelled |
|---|---|---|
| [`BloodNightBeginEvent`](pathname:///javadoc/de/eldoria/bloodnight/api/event/night/BloodNightBeginEvent.html) | A blood night is about to begin | Yes. The night stays normal. |
| [`BloodNightEndEvent`](pathname:///javadoc/de/eldoria/bloodnight/api/event/night/BloodNightEndEvent.html) | A blood night ended. `reason()` says why. | No |
| [`CustomMobSpawnEvent`](pathname:///javadoc/de/eldoria/bloodnight/api/event/mob/CustomMobSpawnEvent.html) | A spawning mob is about to become a special mob | Yes. The mob stays normal. |
| [`CustomMobDeathEvent`](pathname:///javadoc/de/eldoria/bloodnight/api/event/mob/CustomMobDeathEvent.html) | A special mob died. You can still change its drops. | No |
| [`CustomMobNightEndEvent`](pathname:///javadoc/de/eldoria/bloodnight/api/event/mob/CustomMobNightEndEvent.html) | A special mob's blood night is over and it is about to go or turn ordinary. `action(...)` changes what happens. | Yes. The mob stays as it is. |
| [`BloodNightLootEvent`](pathname:///javadoc/de/eldoria/bloodnight/api/event/mob/BloodNightLootEvent.html) | A player killed a special mob, or an ordinary monster in a blood night, and its [loot tables](../mobs/loot-tables.md) were rolled. `loot()` holds the stacks by table ID; change, add or remove them. `money()` is what the kill pays through Vault; change it with `setMoney`. `mobId()` is null for an ordinary monster. | Yes. Nothing from the tables drops and no money is paid. |
| [`DeathActionEvent`](pathname:///javadoc/de/eldoria/bloodnight/api/event/DeathActionEvent.html) | A death is about to cause lightning, thunder or a shockwave. `action()` says which. | Yes. That one is left out. |
| [`BloodNightSettingsChangeEvent`](pathname:///javadoc/de/eldoria/bloodnight/api/event/BloodNightSettingsChangeEvent.html) | The settings changed and are in use. `cause()` says why: `RELOAD`, `EDITOR`, `COMMAND`, `STORE` or `CONVERT`. | No |

The night events are world events, so `getWorld()` tells you the world. `night()` gives the night's number.

```java
@EventHandler
public void onBegin(BloodNightBeginEvent event) {
    if (event.getWorld().getName().equals("lobby")) {
        event.setCancelled(true);
    }
}

@EventHandler
public void onSpecialMob(CustomMobSpawnEvent event) {
    if (event.getEntity().getLocation().getY() > 200) {
        event.setCancelled(true);
    }
}
```

`BloodNightEndEvent.reason()` is one of:

| Reason | Meaning |
|---|---|
| `TIME` | The night is over |
| `CANCELLED` | Someone cancelled it |
| `DISABLED` | The world no longer has blood nights |
| `RELEASED` | The dragon fight that held it is over |
| `SHUTDOWN` | The server or plugin stopped |
