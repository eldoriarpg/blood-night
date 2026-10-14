---
title: Flow
sidebar_position: 5
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Flow

## Batch

`flow.batch`

Runs a chain several times, then continues

| In | Type | What it is |
|---|---|---|
| Count | Integer | How often the body runs |

| Out | Type | What it is |
|---|---|---|
| Index | Integer | The current run, counting from 0 |

Goes on with: **Body**, **Next**

## Branch

`flow.branch`

Runs one of two chains depending on a condition, then continues

| In | Type | What it is |
|---|---|---|
| Condition | Boolean | Decides which chain runs |

Goes on with: **True**, **False**, **Next**

## Delay

`flow.delay`

Runs the rest of the chain once the duration has passed; starting it again while it waits waits twice

| In | Type | What it is |
|---|---|---|
| Duration | Number | How long to wait, in the picked unit |

Goes on with: **Next**

## Do N

`flow.do_n`

Runs the rest of the chain the first n times; named, Reset Do Once of the same name lets it run again

| In | Type | What it is |
|---|---|---|
| Count | Integer | How often the chain runs |

| Out | Type | What it is |
|---|---|---|
| Counter | Integer | How often it has run, this time included |

Goes on with: **Next**

## Do Once

`flow.do_once`

Runs the rest of the chain the first time only; named, Reset Do Once of the same name lets it run again

Goes on with: **Next**

## Flip Flop

`flow.flip_flop`

Runs A, then B the next time, then A again, and so on

| Out | Type | What it is |
|---|---|---|
| Is A | Boolean | Whether this time runs A |

Goes on with: **A**, **B**

## For Each

`flow.for_each`

Runs a chain for every element of a list, then continues

| In | Type | What it is |
|---|---|---|
| List | Iterable of any type | The values to go through |

| Out | Type | What it is |
|---|---|---|
| Element | any type | The current value, inside the body |

Goes on with: **Body**, **Next**

## Gate

`flow.gate`

Runs the rest of the chain only while the gate of its name is open; gates start open

Goes on with: **Next**

## Reset Do Once

`flow.reset_once`

Lets the Do Once and Do N nodes of the same name run again

Goes on with: **Next**

## Retriggerable Delay

`flow.retriggerable_delay`

Runs the rest of the chain once the duration has passed; starting it again while it waits starts the wait anew

| In | Type | What it is |
|---|---|---|
| Duration | Number | How long to wait, in the picked unit |

Goes on with: **Next**

## Set Gate

`flow.set_gate`

Opens, closes or toggles the gate of its name

Goes on with: **Next**

## Switch

`flow.switch`

Runs the chain of the case matching the value, default otherwise, then continues

| In | Type | What it is |
|---|---|---|
| Value | any type | Picks the case with the same value |

Goes on with: **Case 1**, **Case 2**, **Case 3**, **Case 4**, **Case 5**, **Case 6**, **Case 7**, **Case 8**, **Default**, **Next**
