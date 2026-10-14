---
title: Macros and routines
sidebar_position: 3
---

# Macros and routines

When graphs grow, you may build the same thing twice. Macros and routines help you build it once.

## Routines: reuse inside one mob

A routine is a chain of actions with a name. Any other chain of the same mob can call it.

1. Add **Define Routine** and give it a name, like `teleport_near`.
2. Build the chain behind it.
3. Add **Routine Parameter** nodes for values the caller should pass in.
4. Call it from anywhere in the graph with the node that has the routine's name.

The `ender_creeper` uses a routine to teleport close to its target.

## Macros: reuse across mobs

A macro is a small graph of its own. Every mob can use it like a node. Macros live in `macros.yml` and in the Macros
tab of the editor.

| Node | What it is in a macro |
|---|---|
| Macro Start | Where the macro begins when it is called |
| Macro End | Where it goes back to the caller |
| Macro Input | A value the caller passes in |
| Macro Output | A value the macro gives back |

When you change a macro, every mob that uses it changes too.
