---
title: Graphs
sidebar_position: 2
---

# Graphs

A special mob's behaviour is a **graph**. A graph is made of **nodes** (boxes) and **wires** (lines between them).

Your server turns each graph into Java code. That is why special mobs run fast, even with many of them.

## Three kinds of nodes

**Triggers** start something. They have a red header. "On Hit" runs when the mob hits something. "On Tick" runs
every tick, 20 times a second.

**Actions** do something. "Apply Effect" gives a potion effect. "Strike Lightning" calls lightning.

**Values** give you information. "Self" is the mob itself. "Nearest Player" finds the closest player.

## Two kinds of wires

- **White wires** set the order. They go from one action to the next. A trigger starts the chain.
- **Coloured wires** carry values, like an entity, a number or a location. The colour shows the kind of value.

## A small example

"When the mob hits a player, the player gets slow."

1. Add **On Hit**. Its **Entity** is what the mob hit.
2. Add **As** and set its type to Player. Connect On Hit's **Entity** to it. As gives the player in **Result**, and
   **Matches** says if it really was a player.
3. Add **Branch**. Connect the white wire from On Hit to it, and **Matches** to its **Condition**.
4. Add **Apply Effect**. Connect Branch's **True** to it, and As's **Result** to its **Living Entity**.
5. Set the effect to `minecraft:slowness` and the seconds to 5.

The `fearful_phantom` does exactly this. Open it to compare.

## Using the canvas

| Do this | To |
|---|---|
| Double click the canvas | Add a node there |
| Drag from a pin and let go on empty space | Add a node that fits the wire |
| Double click a node | See its details |
| Double click a wire | Add a reroute point to tidy it |
| Drag on the canvas | Select nodes in a box |
| Space + drag | Move the view |
| Scroll | Zoom |
| Delete | Delete what is selected |

**Arrange nodes** in the toolbar lays out the whole graph neatly.

## Handy nodes

| Node | What it does |
|---|---|
| Branch | Does one thing or another, by a true or false value |
| Chance | Gives true with a given chance, for a Branch |
| Cooldown | Gives true if enough time has passed since the last time, for a Branch |
| Delay | Waits, then goes on |
| Do Once | Goes on only the first time |
| Switch | Picks one of several ways by a number |
| Timers | Remember when something happened, shared by the whole mob |
| Variables | Remember a value, shared by the whole mob |
| Mutate | Reads and changes the properties of something, like a creeper's fuse or a damage amount |
| Run Command | Runs a command as the console. This is how a mob uses other plugins, like giving money. |
| Broadcast | Sends a message to the players in the world or on the server |

All nodes are listed in the [node reference](../reference/nodes/index.md).

:::warning[Commands run as the console]
Run Command can do anything the console can. Only give `bloodnight.command.editor` to people you trust.
:::

## On Folia

Folia splits the world into regions. Each region runs on its own. A mob's graph runs in the mob's region, and it
can only look at things in that region. Most graphs never notice this, because mobs act on things close to them.

What changes on Folia:

| Node | What happens far away |
|---|---|
| Entities Near Location, Entities Near Entity, Nearest Player, Line Trace, Block At | Finds nothing |
| Has Line Of Sight | Gives false |
| Launch Projectile | Launches nothing |
| Strike Lightning, Spawn Particle, Spawn Potion Cloud, Mutate Block | Happen a tick later |
| Mutate World (time, weather, difficulty, ...) | Happens a tick later |
| Read World: entities, players and counts | Always fails on Folia and shows a problem |
| Mutate, Apply Effect, Damage on an entity in another region | Fails and shows a problem |

On Paper all of these work right away, as always.

## Problems

If a graph has a mistake, the editor shows it in the **Problems** panel, next to the node it belongs to. A mob with
problems does not get its behaviour until they are fixed.
