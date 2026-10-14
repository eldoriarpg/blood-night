---
title: Math
description: "Math: every node of this group, with its inputs and outputs."
sidebar_position: 12
---

{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}

# Math

## Abs

`math.abs`

Absolute value of a number

| In | Type | What it is |
|---|---|---|
| Value | any number | A number |

| Out | Type | What it is |
|---|---|---|
| Result | any number | The number without its sign |

## Add

`math.add`

Adds two numbers; whole numbers give a whole number, any decimal a decimal

| In | Type | What it is |
|---|---|---|
| First | any number | The first number |
| Second | any number | The second number |

| Out | Type | What it is |
|---|---|---|
| Result | any number | The outcome, a whole number when both numbers are whole |

## Ceil

`math.ceil`

Rounds up to the next integer

| In | Type | What it is |
|---|---|---|
| Value | Number | A number |

| Out | Type | What it is |
|---|---|---|
| Result | Integer | Rounds up to the next integer |

## Clamp

`math.clamp`

Keeps a number within a lower and upper bound

| In | Type | What it is |
|---|---|---|
| Value | any number | The number to keep in bounds |
| Lower | any number | The smallest allowed value |
| Upper | any number | The largest allowed value |

| Out | Type | What it is |
|---|---|---|
| Result | any number | The number, moved into the bounds when outside |

## Divide

`math.divide`

Divides the first number by the second; whole numbers round toward zero and give 0 when dividing by zero

| In | Type | What it is |
|---|---|---|
| First | any number | The number divided |
| Second | any number | The number divided by |

| Out | Type | What it is |
|---|---|---|
| Result | any number | The quotient; whole numbers round toward zero |

## Floor

`math.floor`

Rounds down to the next integer

| In | Type | What it is |
|---|---|---|
| Value | Number | A number |

| Out | Type | What it is |
|---|---|---|
| Result | Integer | Rounds down to the next integer |

## Modulo Integer

`math.modulo_integer`

The remainder of dividing the first integer by the second, with the second's sign; 0 when dividing by zero

| In | Type | What it is |
|---|---|---|
| First | Integer | The integer divided |
| Second | Integer | The integer divided by |

| Out | Type | What it is |
|---|---|---|
| Result | Integer | The remainder |

## Multiply

`math.multiply`

Multiplies two numbers; whole numbers give a whole number, any decimal a decimal

| In | Type | What it is |
|---|---|---|
| First | any number | The first number |
| Second | any number | The second number |

| Out | Type | What it is |
|---|---|---|
| Result | any number | The outcome, a whole number when both numbers are whole |

## Round

`math.round`

Rounds to the closest integer

| In | Type | What it is |
|---|---|---|
| Value | Number | A number |

| Out | Type | What it is |
|---|---|---|
| Result | Integer | Rounds to the closest integer |

## Subtract

`math.subtract`

Subtracts the second number from the first; whole numbers give a whole number, any decimal a decimal

| In | Type | What it is |
|---|---|---|
| First | any number | The first number |
| Second | any number | The second number |

| Out | Type | What it is |
|---|---|---|
| Result | any number | The outcome, a whole number when both numbers are whole |
