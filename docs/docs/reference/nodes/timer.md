---
title: Timers
description: "Timers: every node of this group, with its inputs and outputs."
sidebar_position: 19
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Timers

## Reset Timer

`timer.reset`

Starts a named timer again; every timer node with the same name shares it

Goes on with: **Next**

## Timer Elapsed

`timer.elapsed`

Time since the behaviour started or a named timer was last reset

| Out | Type | What it is |
|---|---|---|
| Elapsed | Number | Time passed in the picked unit, with fractions |
