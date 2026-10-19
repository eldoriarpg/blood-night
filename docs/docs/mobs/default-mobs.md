---
title: The ready-made mobs
sidebar_position: 7
---

# The ready-made mobs

These mobs come with BloodNight in its [mob store](store.md). Install the ones you want. You can change them,
copy them or delete them.

Their names show in each player's language, English or German so far. Players without the
[resource pack](../nights/atmosphere.md#the-resource-pack) may see the English names.

The first 33 have double health, only spawn during blood nights and go away when the night ends. The Blood Dragon is
different, see below.

Each mob wears a [particle effect](particle-effects.md) that fits it, plus souls when it spawns and a red burst when it
dies. The invisible ones wear none, so nothing gives them away. Some effects aim at you: dark wings when a mob lifts
you up, magic lines when a wizard casts, lightning bolts, and the Blood Dragon's spear and beam. Change or remove them
in the mob's **Particles** part.

## Zombies

Husks, drowned, zombie villagers and zombified piglins can become these too, when no mob of their own fits.

| ID | What it does |
|---|---|
| `armored_zombie` | Wears full diamond armour. Your hits can knock pieces off. Without armour it gets faster. |
| `knight_zombie` | Wears chainmail and a golden axe. Every 8 seconds it raises a shield for 2 seconds and blocks hits from the front: hit it from behind. Every 10 seconds it growls and dashes at you. |
| `invisible_zombie` | Invisible, but faint dust shows at its feet. When you hit it, it shows up for 10 seconds. |
| `speed_zombie` | Every 6 seconds it growls and lunges at you very fast, then it is slow for a moment. |

## Skeletons

| ID | What it does |
|---|---|
| `invisible_skeleton` | Invisible, but it shows itself for a second after each shot. Its arrows glow. |
| `magic_skeleton` | Its arrows bring slowness, weakness, poison and nausea, always in this order. The dust on it shows what comes next. |

## Creepers

| ID | What it does |
|---|---|
| `speed_creeper` | Fast, with a short fuse. Its explosion sets fire. |
| `toxic_creeper` | Leaves small poison clouds as it walks and a strong poison cloud when it explodes. |
| `unstable_creeper` | Charged. When you hit it, it glows and a red ring grows around it. Run: a second later it explodes as far as the ring. |
| `nervous_powered_creeper` | Charged, fast and explodes almost at once. |
| `ender_creeper` | Teleports close to you when you are far away. A purple pillar shows where, a second before. |
| `ghost_creeper` | Rides an invisible vex, so it flies. Above you it hisses, and a second later it drops on you. |

## Endermen

| ID | What it does |
|---|---|
| `fearful_enderman` | Comes for the nearest player. It screams every 12 seconds: look away within a second, or you go blind. Its hits blind you too. |
| `toxic_enderman` | Leaves a poison cloud where it teleports from. |

## Phantoms

| ID | What it does |
|---|---|
| `fearful_phantom` | Glows. Its hits slow you down and knock you out of elytra flight. |
| `fire_phantom` | Carries a blaze and drops a small fireball on you every 6 seconds. |
| `phantom_soul` | Invisible while it circles. When it dives at you, it shows itself and glows. Its hits blind you. |
| `carrier_phantom` | Grabs you, carries you 20 blocks up and drops you. Hit it on its way in to stop the grab. Press sneak five times to break loose. |

## Witches

| ID | What it does |
|---|---|
| `fire_wizard` | Lava drips around it, and a second later it throws a large fireball, every 5 seconds. Hit the fireball back: it deals triple damage to the wizard. |
| `thunder_wizard` | Marks where you stand with a red ring three times in a row. When a ring is full, lightning strikes there: keep moving. |
| `wither_wizard` | Throws three wither skulls in a row, then rests for 8 seconds. Below half health it heals once. |

## Spider riders

Cave spiders can become these too.

| ID | What it does |
|---|---|
| `blaze_rider` | A spider with a blaze on its back. Players right next to it catch fire. |
| `speed_skeleton_rider` | A fast spider with a skeleton on its back. When you get close, it leaps away so the skeleton can shoot. |
| `wither_skeleton_rider` | A fast spider with a wither skeleton on its back. Its bite webs you in place for a moment. |

## Slimes

| ID | What it does |
|---|---|
| `toxic_slime` | Its hits poison you. Leaves a poison cloud when it dies. |

## Newer mobs

| ID | What it does |
|---|---|
| `storm_breeze` | Fires three wind charges at you every 6 seconds. |
| `spore_bogged` | Its arrows poison you and make you dizzy. Leaves a poison cloud when it dies. |
| `volley_pillager` | Fires two extra arrows every 4 seconds while it sees you. |
| `tide_drowned` | Throws a trident every 3 seconds while you are in water. |
| `swarm_silverfish` | Fast. When you hit it, every silverfish nearby comes for you. |
| `assassin_spider` | Leaps onto you from up to 16 blocks away, even up into trees. Below you, it hops back first to leap from the side. Takes no fall damage. |

## The End

| ID | What it does |
|---|---|
| `launcher_enderman` | Grabs you. Sneak within a second, or it carries you up to 20 blocks into the air and lets go. |
| `grabber_enderman` | Every 10 seconds it grabs mobs around it and puts them down in front of you. |
| `fuse_enderman` | Lays lit TNT next to you every 6 seconds. It hurts but breaks no blocks. |
| `blood_dragon` | The ender dragon of a [dragon fight](../nights/nether-and-end.md#the-dragon-fight). Twice the health, harder hits, dragon fireballs and lightning. Every 8 seconds it throws a blood spear at the nearest player, which hurts when it lands. Its hits wither and draw a red beam. It never spawns by chance. |

Open any of them in the [web editor](../editor/sessions.md) to see how they work. They are good examples for your
own mobs.
