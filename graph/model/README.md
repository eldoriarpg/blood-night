# Graph Model

`:graph-model`: the data model of node graphs and macros, plus their JSON form. It is pure data with no Bukkit and no
compiler dependency; every other graph module builds on it.

## Overview

A graph is a map of node instances by id. A node instance only says which node type it is and how it is wired; what
the node does is defined elsewhere, by the node definition registered under that type in
[graph-compiler](../compiler/README.md).

A node instance (`NodeInstance`) has:

- `type`: the stable id of its node definition, such as `math.add`.
- `config`: literal values for the definition's config fields, kept as unchecked JSON.
- `inputs`: data edges by input port id. An `Edge` points at an output port of another node.
- `literals`: values typed directly into inputs instead of connecting an edge, also unchecked JSON.
- `executions`: chained node ids by execution port id, in execution order.
- `editor`: editor state such as the position on the canvas. The framework stores it but never reads it.

Data flows backwards along edges (an input names the node and port it reads from), while execution flows forwards
(an execution port lists the nodes that run next). Checking config values, types and connections is left to the
compiler; this module only rejects blank node ids, duplicate ids in the builder and missing required fields.

A `Macro` is a reusable subgraph shared across graphs. Its body is an ordinary graph whose boundary nodes
(`macro.input`, `macro.output`, `macro.start`, `macro.end`) define the ports the macro shows when used as a node of
type `macro.<id>`. Graphs store only that use node; the body is spliced in when compiling. When reading a macro, a
missing name defaults to the id, a missing description to empty and a missing category to `macro`.

JSON written by `GraphJson` is deterministic: nodes, ports and config fields are ordered by id, empty parts are
left out, and unknown properties are rejected on reading. A small graph looks like this (the node types are only
illustrative, the model does not know any):

```json
{"nodes":{
  "add":{"type":"math.add","inputs":{"first":{"node":"five","port":"value"}},"literals":{"second":2.0}},
  "five":{"type":"value.number","config":{"value":5.0}},
  "print":{"type":"debug.print","inputs":{"value":{"node":"add","port":"result"}},"editor":{"x":10,"y":-4}},
  "tick":{"type":"entry.tick","executions":{"next":["print"]}}
}}
```

## Layout

All classes live in `de.eldoria.graph.model`:

- `Graph`: immutable map of node instances by id, with a builder that rejects duplicate ids.
- `NodeInstance`: one placed node (type, config, inputs, literals, executions, editor state), with a builder.
- `Edge`: a data edge pointing at an output port of another node.
- `Macro`: a user-defined reusable subgraph with id, name, description, category and body.
- `GraphJson`: reads and writes graphs and macros as JSON; `mapper()` exposes the mapper for embedding graphs into
  larger documents.

## Using it

Build graphs in code with the builders, or read them from JSON:

```java
Graph graph = Graph.builder()
        .node("tick", NodeInstance.of("entry.tick").chain("next", "print"))
        .node("print", NodeInstance.of("debug.print").literal("value", StringNode.valueOf("hello")))
        .build();
String json = GraphJson.write(graph);
Graph again = GraphJson.read(json);
```

These builders check nothing against a platform. To have node types, ports and config fields checked as they are
named, use `Graphs` in [graph-compiler](../compiler/README.md).

## Dependencies

Depends on: Jackson 3 databind and annotations (exposed as `api`), JSpecify (from the `bloodnight.library`
convention plugin).

Used by: [graph-compiler](../compiler/README.md) directly, and through it by every module that compiles or
edits graphs.

## Building and testing

```bash
./toolchain.sh be-test1 graph-model '*'
./toolchain.sh be-format
```

`./toolchain.sh be-build` runs formatting checks, tests and SpotBugs over every module.
