---
title: Plugin API
sidebar_position: 1
---

# Plugin API

Your own plugin can ask BloodNight what is going on, and react when something happens.

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

You can also get the service from Paper's services manager.

On Folia, call `force` and `cancel` on the global region and `spawn` on the region of the location.

## Events

| Event | When | Can be cancelled |
|---|---|---|
| `BloodNightBeginEvent` | A blood night is about to begin | Yes. The night stays normal. |
| `BloodNightEndEvent` | A blood night ended. `reason()` says why. | No |
| `CustomMobSpawnEvent` | A spawning mob is about to become a special mob | Yes. The mob stays normal. |
| `CustomMobDeathEvent` | A special mob died. You can still change its drops. | No |

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
