---
title: Choosing blood nights
sidebar_position: 1
---

# Choosing blood nights

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

Not every night is a blood night. Each world decides by itself, with the `nightSelection` settings in its world file.

```yaml
nightSelection:
  type: RANDOM
  random:
    probability: 60
```

`type` picks the way. Every number is a chance from 0 to 100. 0 means never, 100 means always.

## The ways to choose

### RANDOM

Every night has the same chance.

```yaml
type: RANDOM
random:
  probability: 60
```

### MOON_PHASE

The chance depends on the moon in your world. Minecraft has 8 moon phases.

```yaml
type: MOON_PHASE
moonPhase:
  probabilities:
    FULL_MOON: 0
    WANING_GIBBOUS: 10
    LAST_QUARTER: 20
    WANING_CRESCENT: 40
    NEW_MOON: 100
    WAXING_CRESCENT: 40
    FIRST_QUARTER: 20
    WAXING_GIBBOUS: 10
```

A phase you leave out has a chance of 0.

### REAL_MOON_PHASE

Like `MOON_PHASE`, but it uses the moon in the real sky. It uses the same `moonPhase` list.

### INTERVAL

A blood night can only happen every few nights.

```yaml
type: INTERVAL
interval:
  interval: 5
  probability: 100
```

This means: every 5th night has a chance of 100. All other nights have no chance.

### PHASE

You make your own cycle. Each entry is one night. After the last one, it starts again.

```yaml
type: PHASE
phase:
  probabilities: [0, 20, 50, 100]
```

Here night 1 has no chance, night 2 has 20, night 3 has 50, night 4 is always a blood night. Then it repeats.

A cycle can have up to 100 nights.

### CURVE

The chance goes up and down like a wave.

```yaml
type: CURVE
curve:
  min: 20
  max: 80
  period: 10
```

It starts at `min`, climbs to `max` in the middle of the `period` and goes back down. Then it starts again.

## Players can't guess it

BloodNight decides each night ahead of time, with a secret number stored in the world. So the result does not change
when you restart the server. Players can't work it out from the world seed.

You can still see what is coming:

```
/bloodnight night
```

It shows the way your world chooses, whether a blood night runs, the chance for the coming night and when the next
blood night will be.

## Force or cancel a night

- `/bloodnight night force [world]` makes the coming night a blood night. If it is night already, it starts now.
- `/bloodnight night cancel [world]` keeps the coming night normal. If a blood night runs, it ends now.

## Turn blood nights off in a world

```yaml
enabled: false
```

The Nether and the End have no night. They get blood nights by the clock instead, see
[The Nether and the End](./nether-and-end.md).
