# Graph Standard Nodes

`:graph-std`: the generic node set every platform can use (values, math, logic, functions, control flow and
debugging), plus the small runtime classes generated code calls into. None of these nodes know anything about Bukkit
or BloodNight.

## Overview

Each standard node is a public constant of `StandardNodes`, defined with the building blocks of
[graph-compiler](../compiler/README.md). `StandardNodes.all()` returns the whole set, and `StdNodes` holds the
type id of each one, so graphs built in code do not spell ids by hand.

| Category | Nodes |
|---|---|
| `math` | `add`, `subtract`, `multiply`, `divide`, `abs`, `clamp` (whole numbers in, a whole number out; any decimal in, a decimal out), `modulo_integer`, `ceil`, `floor`, `round` |
| `logic` | `and`, `or`, `not`, `equal`, `is_null`, `greater`, `greater_or_equal`, `less`, `less_or_equal` |
| `function` | `chance`, `cooldown` (duration in a picked unit), `random_integer`, `random_number` |
| `timer` | `elapsed` (in a picked unit), `reset` (named timers shared by every node naming them) |
| `flow` | `branch`, `batch`, `for_each`, `delay` and `retriggerable_delay` (the rest of the chain runs later), `do_once`, `do_n`, `reset_once`, `flip_flop`, `gate`, `set_gate` |
| `layout` | `reroute` (data), `reroute.execution`: pass a wire on unchanged, only for layout |
| `debug` | `print` |

Port ids are consistent across nodes: `first`/`second` and `value` in, `result` out, `lower`/`upper` for ranges.
Control flow separates the repeated `body` from the following `next`. `flow.for_each` takes any `Iterable<T>` and
outputs `element` typed by what is connected.

`StandardNodes.typed(TypeMapping)` adds the nodes whose type is picked in the editor, from the platform's selectable
types. They are not part of `all()` because they depend on the platform's types:

- `cast` ("As") gives the value as the picked type, or missing when it is not one, and `matches` tells which
  (reference types only).
- `value` ("Value") is a fixed value of the picked type (types with a literal form only).
- `flow.switch` runs the case whose value, typed beside it, equals the switched value (cases `case_0` to `case_7`,
  `default`).
- `select` gives one of several values by a key typed beside each option; a boolean key gives `option_0` for true
  and `option_1` for false.
- `variable.get` and `variable.set` read and write a value kept per behaviour instance under a name.

Named state (timers, variables, named Do Once, gates) is shared by every node using the name; one name holds one
type. Waiting needs the platform's clock (`GraphPlatform.Builder.clock`), which hands pending delays back each tick.

Integer division and modulo by zero yield 0 instead of throwing. `function.cooldown` and `timer.elapsed` are
stateful, so the compiler evaluates them exactly where the graph says.

## Layout

- `de.eldoria.graph.std.StandardNodes`: every standard node definition, `all()` and `typed(TypeMapping)`.
- `de.eldoria.graph.std.StdNodes`: type id constants, plus `cast(selectable)` for cast node ids.
- `de.eldoria.graph.std.runtime`: classes the generated code uses:
  - `Cooldown`: state of one cooldown node per behaviour instance;
  - `Timer`: state of one named timer per behaviour instance;
  - `TimeUnit`: the units cooldowns, timers and delays are given in;
  - `Delays`: chains held back by delay nodes, handed back on the clock;
  - `Switches`: finds the case or option matching a value;
  - `GateChange`: what Set Gate does;
  - `Integers`: division and modulo that cannot throw;
  - `Randoms`: random numbers, integers and chances.

## Using it

Register the nodes on a platform and put this module on the compile classpath of generated classes, since the
generated code refers to the `runtime` classes:

```java
GraphPlatform platform = GraphPlatform.builder(MyHost.class)
        .define(StandardNodes.all())
        .define(StandardNodes.typed(types))
        .clock("onTick")
        .classpath(List.of(pathOf(MyHost.class), pathOf(Cooldown.class)))
        .build();
```

Here `pathOf` stands for the jar or directory a class was loaded from (the tests read it from the class's code
source).

To add a standard node:

1. Add the definition as a constant in `StandardNodes` (the private helpers such as `binary`, `unary` and `value`
   cover most simple cases) and list it in `all()`.
2. Add its id to `StdNodes`. `StdNodesTest` fails unless every node in `all()` has exactly one constant there.
3. If the node needs state or a helper at runtime, put that class in `de.eldoria.graph.std.runtime`.
4. Cover its behaviour in `StandardNodesTest`; `Harness` compiles and runs small graphs and records the values they
   produce.
5. The node changes the editor catalog, so update the snapshot of
   [graph-editor-payload](../editor-payload/README.md).

## Dependencies

Depends on: [graph-compiler](../compiler/README.md) (`api`).

Used by: `bukkit-nodes` (which adds the Paper nodes on top), and the tests of
[graph-editor-payload](../editor-payload/README.md).

## Building and testing

```bash
./toolchain.sh be-test1 graph-std '*'
./toolchain.sh be-test1 graph-std '*StandardNodesTest'
./toolchain.sh be-format
```
