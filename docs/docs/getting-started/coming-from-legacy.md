---
title: Coming from the old BloodNight
sidebar_position: 4
---

# Coming from the old BloodNight

If your server ran the old BloodNight (version 0.12 or older), you can take its settings over. The old plugin does
not need to be installed for this. Only its files are read.

## Steps

1. Remove the old BloodNight jar and put the new one into `plugins`. Leave the folder `plugins/BloodNight` as it is.
2. Start the server. BloodNight finds the old files and moves them to `plugins/BloodNight/legacy`. The console says
   so.
3. Run `/bn convert`. It shows what it would take over and what it cannot. Nothing changes yet.
4. Run `/bn convert apply`. Your current files are backed up first, then the old settings are written and used.
5. Check the result, for example in the [web editor](../editor/sessions.md). When everything looks right, you can
   delete the `legacy` folder.

Did you already delete the old folder? Put a backup of it into `plugins/BloodNight/legacy` and run `/bn convert`.

## What is taken over

| Old setting | Where it goes |
|---|---|
| Prefix, broadcast level and method, language | `announcements` in `config.yml` |
| Update reminder | `updates.notice` |
| Blindness, join warning, blocked commands | Each world, as before they counted for all |
| Spawner settings | Each world's ignored spawn reasons and vanilla loot |
| World on or off, creepers, boss bar | The world file |
| Night selection, with all six types and their values | `nightSelection` |
| Night begin and end, skipping, length, commands | `nightSettings` |
| Damage and health multipliers, phantoms, spawn chance, mob names | `mobSettings` |
| Vanilla drops and experience | `worldDrops` |
| Sounds | `soundSettings` |
| Death actions | `deathActions` |

## Mobs

The old mobs are all in the [mob store](../mobs/store.md). Every old mob that was on in at least one world is
installed from there. In worlds where it was off, it gets the weight 0, so it does not spawn there.

If a world had its own name, health or damage for a mob, that world keeps it as an
[override](../mobs/custom-mobs.md#different-in-one-world).

Drops become registered items. A mob's drops come from the first world that has some for it.

## What is lost

- Where a night selection stood, like the current phase. It starts fresh.
- `mobTick`, `beeFix` and `autoUpdater`. They have no use any more.
- Items Minecraft cannot read any more, from very old versions.

`/bn convert` lists everything that could not be taken over.
