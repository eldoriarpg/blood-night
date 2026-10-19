---
title: States
sidebar_position: 4
---

# States

A boss often does different things at different times. It fights, then it gets angry, then it casts a big spell. States
let a mob be in one mode at a time. Each mode has its own nodes.

## What a state is

The graph of a mob has a part that **always runs** and one part per **state**. A trigger like **Mob Tick** or **On
Hit** inside a state only fires while the mob is in that state.

Each mob is in exactly one state at a time. When it changes state, the old state's nodes stop and the new state's
nodes start.

## States inside states

A state can lie inside another one. A state inside another is only active together with it. The triggers of the outer
state keep firing while the mob is in any state inside it.

This is how the Blood Dragon is built:

| State | Inside | What it does |
|---|---|---|
| `fighting` | | Shoots fireballs, calls lightning and throws blood spears |
| `calm` | `fighting` | Goes to `raging` when its health drops below 70% |
| `raging` | `fighting` | Goes to `casting` once a minute |
| `casting` | | Rises, hovers and draws the arcane seal, then goes back to `raging` |

While the dragon is `calm` or `raging`, it still fights, because both lie inside `fighting`. While it is `casting`, it
does not fight.

## The first state

One state at the top is marked as **entered first**. Each state with states inside it also marks one of them. A mob
starts in these states when it spawns, and again after a restart.

## Nodes

| Node | What it does |
|---|---|
| **On Enter State** | Runs when the mob enters the state the node lies in |
| **On Exit State** | Runs when the mob leaves the state, however it leaves |
| **Can Enter State** | Says if the mob may enter the state. Without it, the mob always may. |
| **Go To State** | Leaves the current state and enters another |
| **Go To One Of** | Goes to one of up to four states, picked by weight |
| **Current State** | The name of the state the mob is in |
| **In State** | Whether the mob is in a state, or in a state inside it |
| **Time In State** | Seconds since the mob last changed state |

**Go To One Of** skips states that say no in **Can Enter State**. Each weight can be a number or a value from another
node, for example the distance to the nearest player.

## Good to know

- A **Delay** inside a state stops when the mob leaves the state. What it held back never runs.
- A node in a state can use values from the part that always runs and from the states it lies in.
- Routines belong to the part that always runs. You can call them from any state.

## In the editor

Above the canvas, a tab bar lists **Always** and every state. States inside another are shown indented below it. The
star marks the state entered first. Pick a tab to see its nodes. New nodes go into the open tab.

Nodes from the part that always runs, or from an outer state, show up faded when a node of the open tab uses their
value.

The menu next to the tabs adds, renames and removes states. A state can only be removed when it is empty. The
**Overview** draws all states as boxes, with arrows for every **Go To State**. Click a box to open its tab.
