---
title: state
description: "state: every node of this group, with its inputs and outputs."
sidebar_position: 19
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# state

## On Enter State

`state.enter`

Runs when the graph enters the state this node lies in

Goes on with: **Next**

## On Exit State

`state.exit`

Runs when the graph leaves the state this node lies in

Goes on with: **Next**

## Can Enter State

`state.can_enter`

Whether the graph may enter the state this node lies in; without one it always may

| In | Type | What it is |
|---|---|---|
| Allowed | Boolean | True: the state may be entered |

| Out | Type | What it is |
|---|---|---|
| Allowed | Boolean | True: the state may be entered |

## Current State

`state.current`

The name of the innermost state the graph is in

| Out | Type | What it is |
|---|---|---|
| Name | Text |  |

## Go To One Of

`state.go_to_one_of`

Goes to one of up to four states, picked by weight among those that can be entered

| In | Type | What it is |
|---|---|---|
| Weight 1 (optional) | Number | How likely state 1 is; 1 when not given, 0 leaves it out |
| Weight 2 (optional) | Number | How likely state 2 is; 1 when not given, 0 leaves it out |
| Weight 3 (optional) | Number | How likely state 3 is; 1 when not given, 0 leaves it out |
| Weight 4 (optional) | Number | How likely state 4 is; 1 when not given, 0 leaves it out |

| Out | Type | What it is |
|---|---|---|
| Moved | Boolean | True: the graph moved |
| State | Text | The state it moved to; empty if none |

Goes on with: **Next**

## Go To State

`state.go_to`

Leaves the current state and enters another; nothing happens when the graph is in it already or the state says no

| Out | Type | What it is |
|---|---|---|
| Moved | Boolean | True: the graph moved |

Goes on with: **Next**

## In State

`state.in`

Whether the graph is in a state or in a state inside it

| Out | Type | What it is |
|---|---|---|
| Result | Boolean |  |

## Time In State

`state.time`

Seconds since the graph last moved to another state

| Out | Type | What it is |
|---|---|---|
| Seconds | Number |  |
