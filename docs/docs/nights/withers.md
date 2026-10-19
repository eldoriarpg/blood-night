---
title: Withers
sidebar_position: 7
---

# Withers

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks
what you type. This page shows the files, so you know what the editor writes for you.
:::

Who builds a wither during a blood night gets a boss. The wither becomes one of three blood withers, picked at random.
Outside blood nights, withers stay normal.

Each blood wither has more health and hits harder. It stays when the night ends, and it drops its own loot on top of
the nether star.

## Blink Wither

It does not stay caged.

- Every 6 seconds it blinks behind the player it fights.
- It gets out of every trap. When it sits in a boat or a minecart, chokes in blocks, or does not move for 3 seconds
  while a player is near, it blinks to a spot next to the nearest player.
- Blocks do not choke it.

So wither farms and bedrock cages stop working during blood nights. It starts blinking once it has charged up.

## Blood Wither

It lives on your blood.

- Every hit of its skulls heals it.
- Below half its health it rages: four wither skeletons rise around it.
- From then on it throws a blood spear at every player it can see every 5 seconds.

## Grave Wither

No cage holds it, and it pulls you close.

- Every 4 seconds the blocks around it break, like when a wither is hurt. Bedrock and the other blocks a wither cannot
  break stay.
- Players near it are wrapped in darkness.
- Every 10 seconds it pulls every player it can see toward it.

It breaks nothing while the game rule `mob_griefing` is off, and protection plugins can keep blocks safe.

## Which withers a world gets

Each world lists its blood withers with a weight in `mobSettings`:

```yaml
mobSettings:
  withers:
    blink_wither: 1
    blood_wither: 1
    grave_wither: 1
```

A higher weight makes a wither more likely. Set a weight to `0` to leave that wither out. Leave the list empty to keep
withers normal in this world. You can also put your own [special mobs](../mobs/custom-mobs.md) of type `WITHER` in
the list.
