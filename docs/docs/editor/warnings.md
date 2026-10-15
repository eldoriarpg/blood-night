---
title: Warnings
sidebar_position: 4
---

# Warnings

A good mob shows what it is about to do. Then players can dodge, and a hit feels fair. Three nodes draw warnings in
the world. Every player sees them, even without the resource pack.

## Show Marker

Draws a glowing shape on the ground. It grows from nothing to its full size while it shows, then it goes away.

| Shape | What it looks like | Size means |
|---|---|---|
| `AREA` | A flat square | The radius |
| `PILLAR` | A thin column of light | The height |
| `CROSS` | Two bars crossing on the ground | The radius |
| `CIRCLE` | A flat ring on the ground | The radius |

Pick a colour, a size and how many seconds it shows. Use the seconds your attack waits. Then the marker is full
exactly when the attack lands.

## Show Text

Floating text at a location, like `<red>Run!`. It always faces the player and shows through walls.

## Show Above

Text or an item over an entity. It rides along when the entity moves.

## Only for one player

Show Marker and Show Text have a **For** input. Wire a player into it, and only that player sees the warning. That
is good for scares that only one player should notice.

## Limits

A mob can show at most 8 warnings at the same time. A marker counts once, whatever its shape. More are skipped, so a loop cannot fill the world. Warnings are
never saved. After a restart, none are left over.

## In the ready-made mobs

- The **thunder wizard** draws a red ring where its lightning will strike.
- The **unstable creeper** draws a red ring around itself when it is hit. It explodes one second later.
- The **ender creeper** shows a purple pillar where it will appear, one second before it arrives.
