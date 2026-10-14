---
title: Plugin API
sidebar_position: 1
---

# Plugin API

Your own plugin can react to blood nights. BloodNight fires two Bukkit events.

| Event | When | Can be cancelled |
|---|---|---|
| `BloodNightBeginEvent` | A blood night is about to begin in a world | Yes. The night stays normal. |
| `BloodNightEndEvent` | A blood night ended: at sunrise, when cancelled, when the world was turned off, or when the plugin stops | No |

Both are world events, so `getWorld()` tells you the world. `night()` gives the night's number.

```java
@EventHandler
public void onBegin(BloodNightBeginEvent event) {
    if (event.getWorld().getName().equals("lobby")) {
        event.setCancelled(true);
    }
}
```

Add BloodNight as a dependency in your `paper-plugin.yml`, so your plugin loads after it.

More is coming: asking whether a blood night runs, when the next one is, and whether an entity is a special mob.
