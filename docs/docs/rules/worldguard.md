---
title: WorldGuard regions
sidebar_position: 5
---

# WorldGuard regions

A blood night covers the whole world. With [WorldGuard](https://enginehub.org/worldguard), you can keep parts of it
safe, like your spawn town or a shop.

BloodNight adds four region flags. They all allow at first. Set one to `deny` to switch that part off in a region:

| Flag | When denied |
|---|---|
| `bloodnight-custom-mobs` | No special mobs spawn in the region. |
| `bloodnight-rules` | Monsters hit as normal, have normal health and drop normal loot. No death actions. |
| `bloodnight-blocked-commands` | Blocked commands work in the region. |
| `bloodnight-effects` | No begin effect, no boss bar and no atmosphere for players in the region. |

For example, to keep special mobs out of your spawn:

```
/rg flag spawn bloodnight-custom-mobs deny
```

The flags only exist when WorldGuard is installed. Without it, nothing changes.
