# Graph Compiler

`:graph-compiler`: turns [graph-model](../graph-model/README.md) graphs into Java classes, compiled and loaded at
runtime. It also defines what a node type is, so every node set (standard nodes, Paper nodes) is written against this
module.

## Overview

A consumer describes its world as a `GraphPlatform`:

- a **host** interface the generated classes implement; entry points are its methods, each returning `void`;
- an optional **context** class, passed to the constructor of every generated class (in BloodNight, the mob);
- the **node definitions**, by stable id;
- a **type mapping** with editor names and literal codecs for config values;
- the **classpath** the generated code compiles against;
- optional **node rules**, extra checks per node type for what port types cannot express;
- an optional **clock** (`GraphPlatform.Builder#clock`), the host method the host calls once per tick; nodes that wait,
  such as a delay, add per-tick code to it through `EmitContext#everyTick`, even when the graph has no entry there.

`GraphPlatform.Builder#build()` checks everything at once (duplicate ids, config fields without a codec, invalid
defaults, entries without a matching host method) and reports all problems together in a `PlatformException`.

A node definition is a `NodeDescriptor` (id, kind, editor texts, ports, config fields) plus a code emitter. The
shape decides how code is placed:

- `ExpressionDefinition` (kind `VALUE` or `PURE`): outputs are expressions, evaluated when first read in a scope.
- `StatementDefinition` (kind `ACTION` or `CONTROL_FLOW`): statements placed in an execution chain.
- `EntryDefinition` (kind `ENTRY`): start of a chain, implemented as a host method.
- `MacroDefinition`: a user macro used as a node; never emitted, replaced by its body.

Emitters only receive finished JavaPoet `CodeBlock`s for inputs and config values. Data from a graph reaches the
generated code only through a `LiteralCodec`, so a graph cannot inject code.

`GraphCompiler#compile` runs the pipeline:

1. **Expand macros** (`MacroExpander`): every macro use is replaced by the macro body, copied under `<use>/<node>`.
2. **Validate** (`GraphValidator`): node types exist, config values are valid, required inputs are connected,
   execution ports chain statements only, no cycles, type variables are inferred and every edge is type-checked.
3. **Emit** (`GraphEmitter`): one class implementing the host, one method per used entry point, one field per
   stateful node. The code is meant to be readable: locals are named after what they hold and values read once are
   inlined.
4. **Compile and load** (`RuntimeCompiler`): the embedded Eclipse compiler (ECJ) compiles the source in memory, so
   this works on a JRE too, and each compilation gets its own `GeneratedClassLoader`.

Problems are `GraphProblem`s located at the node (and port) of the graph as stored, so the editor can highlight
them. Problems inside a macro are reported on the node using it, with a trace into the macro body. If the generated
code does not compile, the error is mapped back to the node whose code contains it; that points at a faulty node
definition rather than at the graph.

Besides macros, graphs can define **routines**: `routine.define` starts a named chain, `routine.parameter`
nodes (with a picked type) read its arguments, and `routine.<name>` calls it. Each routine becomes a private method of the generated
class. Call node types are derived from the graph itself, not registered on the platform.

Port types can be type variables, bound per node by what is connected. Two kinds are special:

- A **numeric** variable (`PortType.number`) takes numbers only and binds to the widest one it meets (at least
  `int`), so one Add node serves whole and decimal numbers; left unbound it is `double`.
- A **type field** (`ConfigField.selecting`, `NodeDescriptor.Builder#typeConfig`) is a config field whose value is a
  selectable type id; it binds a variable before inference, so the node's ports and other config fields take the
  picked type (the standard cast and value nodes, macro inputs, routine parameters).

A platform can also declare **graph-wide** variables (`GraphPlatform.Builder#binding`, `Binding`): a fallback type
and the candidate types a compile may bind it to (`GraphCompiler#compile(graph, name, bindings)`). Nodes typed by
such a variable take the graph's type, e.g. BloodNight's Self is a Creeper in a creeper mob's graph; macros checked
on their own keep the fallback. The catalog lists the fallbacks and registers the candidates as types.

State nodes share by name (`EmitContext#sharedField`, e.g. named timers and variables); the first declaration of a
name fixes its type, and a later one of another type is reported as a problem on that node.

A config field can name the port it belongs to (`ConfigField#beside`, `NodeDescriptor.Builder#configBeside`), e.g.
the value a switch case matches; the editor draws it beside that port instead of with the node's other settings.
Ports can carry a group (`Port#group`) the editor draws them under; reflected nodes group their properties by the
type that brings them (`ReflectionRules.Builder#group`).

An entry definition can carry an `EntryGuard`: a condition from its parameters and config under which its chains
run, wrapped around that entry's chains only (BloodNight's kill trigger narrows to players or other entities this
way).

## Layout

- `de.eldoria.graph.compiler.GraphCompiler`: the entry point; `compile`, `generate` (source only) and `load`
  (restore cached bytecode without compiling again).
- `de.eldoria.graph.compiler.CompiledGraph`: the loaded class; `instantiate` creates a host instance with its own
  state, `bytecode()` returns class files for caching.
- `de.eldoria.graph.compiler.Graphs`: builds graphs in code, checking node types, ports and config fields against a
  platform as they are named.
- `de.eldoria.graph.compiler.MacroLibrary`: user macros checked against a platform; a broken macro is left out and
  its problems are kept per macro.
- `de.eldoria.graph.compiler.MacroBoundary`, `Routines`: the boundary node types of macros and routines.
- `de.eldoria.graph.compiler.GraphProblem`, `GraphCompilationException`: located problems and the exception
  carrying them.
- `de.eldoria.graph.compiler.RuntimeCompiler`: in-memory compilation with ECJ.
- `de.eldoria.graph.definition`: node definitions, descriptors, ports (`Port`, `PortType`, `ExecutionPort`,
  `ConfigField`) and the emit contexts.
- `de.eldoria.graph.platform`: `GraphPlatform`, `NodeRule`, `PlatformException`.
- `de.eldoria.graph.reflect`: derives nodes by reflection: `ReflectedType` (getters, setters, constructors,
  factories, actions and queries of a class; its read and change nodes are variants of the `object.mutate` family
  the editor shows as one Mutate node),
  `ReflectedEntry` (an entry point from a host method),
  `ReflectionRules` (which methods qualify) and `Documentation` (help texts, e.g. from javadoc).
- `de.eldoria.graph.types`: `TypeMapping`, `LiteralCodec` and the built-in `Codecs` for primitives, strings and
  enums.

## Using it

```java
GraphPlatform platform = GraphPlatform.builder(MyHost.class)
        .context(MyContext.class)
        .define(StandardNodes.all())
        .define(myNodes)
        .classpath(paths)
        .build();
MacroLibrary macros = MacroLibrary.compile(platform, userMacros);
GraphCompiler<MyHost> compiler = new GraphCompiler<>(platform, MyHost.class, parentLoader, macros);
MyHost behaviour = compiler.compile(graph, ClassName.get("generated", "Behaviour")).instantiate(context);
```

The parent class loader must be able to load the host, the context and everything generated code refers to, and the
classpath must contain the jars or directories of those classes, because the compiler cannot look into foreign class
loaders.

To add a node type, build a `NodeDescriptor` with `NodeDescriptor.builder(id, kind)` and pair it with an emitter in
the matching definition record. Node ids are dotted lower-case paths (`math.add`), port and field ids single
segments (`result`); the category defaults to the first id segment and names are derived from ids. Mark a node
`stateful()` when evaluating it changes state, so its code is never moved. See
[graph-std](../graph-std/README.md) for many small examples.

## Dependencies

Depends on: [graph-model](../graph-model/README.md) and JavaPoet (both `api`), ECJ (implementation).

Used by: [graph-std](../graph-std/README.md) and [graph-editor-payload](../graph-editor-payload/README.md), and
through them by `bukkit-nodes`, `catalog`, `core` and `web-backend`.

## Building and testing

```bash
./toolchain.sh be-test1 graph-compiler '*'
./toolchain.sh be-test1 graph-compiler '*MacroTest'
./toolchain.sh be-format
```

The tests use a small toy platform (`de.eldoria.graph.toy`) instead of Bukkit.
