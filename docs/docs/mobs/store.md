---
title: The mob store
sidebar_position: 6
---

# The mob store

BloodNight comes with ready-made mobs, but it does not put them on your server by itself. You pick them from the
store. New BloodNight versions bring new mobs and better versions of old ones, and the store shows them to you.

## Packs

The mobs come in packs:

| Pack | What is in it |
|---|---|
| `classic` | The mobs BloodNight always had |
| `newer` | Mobs for breezes, bogged, pillagers, drowned, silverfish and a leaping spider |
| `end` | Endermen that launch, grab and blow you up, and the Blood Dragon for [dragon fights](../nights/nether-and-end.md#the-dragon-fight) |

The [list of ready-made mobs](default-mobs.md) says what each mob does.

## Install mobs

The easy way is the [web editor](../editor/sessions.md). In the Mobs tab, click **Add** and then **From the store**.
You see every pack with its mobs. Click **Install** on one mob, or install the whole pack at once. A mob you already
have shows **Installed**. When a new version is waiting, the button says **Update**.

You can also use commands:

```
/bn store                        shows the packs
/bn store list classic           shows the mobs of a pack
/bn store install classic        installs a whole pack
/bn store install speed_zombie   installs one mob
```

Installed mobs land in `mobs.yml`, like your own. If you already have a mob with the same ID, the store skips it.

## Updates

A store mob remembers which version you installed. When a new BloodNight version improves a mob, two things can
happen:

- **You did not change what the mob does.** It takes the new version by itself when the server starts. The console
  tells you.
- **You changed what the mob does**, for example its graph in the editor. Then BloodNight never overwrites your
  work. Admins see a message when they join, and you take the update with `/bn store update` when you want it.

An update only brings the mob's behaviour. Your own numbers stay: its name, health, damage, equipment, drops and
when it spawns.

`/bn store update` takes every waiting update. `/bn store update speed_zombie` takes one.

To stop updates on their own, set this in `config.yml`:

```yaml
store:
  autoUpdate: false
```
