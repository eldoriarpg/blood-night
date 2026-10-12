# BloodNight

A Minecraft plugin for Paper that turns some nights into blood nights: special mobs spawn, and they behave by node
graphs that the server compiles to Java classes. Mobs, their behaviour, items and settings can be edited in a
web editor that several players can use at the same time.

## How the parts fit together

| Module | Builds on |
|---|---|
| `graph-compiler` | `graph-model` |
| `graph-std` | `graph-compiler` |
| `graph-editor-payload` | `graph-compiler` |
| `bukkit-nodes` | `graph-std`, `api` |
| `catalog` | `bukkit-nodes`, `graph-editor-payload` |
| `core` | `api`, `bukkit-nodes`, `catalog`, `graph-editor-payload`, `web-api`, `json-schema` |
| `web-backend` | `web-api`, `graph-editor-payload`, `json-schema` |
| `web/frontend` | talks to `web-backend` over HTTP and a WebSocket |

- The **graph modules** describe graphs, compile them to Java and provide the standard nodes.
- **bukkit-nodes** adds the Paper nodes and triggers; **catalog** bundles the description of every node the editor
  can offer.
- **core** is the plugin itself. It opens editor sessions on the web backend and pulls the results back.
- **web-backend** keeps editor sessions with their revisions and locks, and **web/frontend** is the editor in the
  browser.

## Modules

| Module | Path | What |
|---|---|---|
| `:graph-model` | [`graph-model`](graph-model/README.md) | Graphs, nodes, edges, macros (Bukkit-free) |
| `:graph-compiler` | [`graph-compiler`](graph-compiler/README.md) | Compiles graphs to Java |
| `:graph-std` | [`graph-std`](graph-std/README.md) | Standard nodes |
| `:graph-editor-payload` | [`graph-editor-payload`](graph-editor-payload/README.md) | The catalog the editor draws nodes from |
| `:bukkit-nodes` | [`bukkit-nodes`](bukkit-nodes/README.md) | Paper nodes and triggers |
| `:catalog` | [`catalog`](catalog/README.md) | Bundled catalog |
| `:api` | [`api`](api/README.md) | Plugin API (events) |
| `:core` | [`core`](core/README.md) | The plugin |
| `:web-api` | [`web/api`](web/api/README.md) | Contract between plugin, web backend and frontend |
| `:web-backend` | [`web/backend`](web/backend/README.md) | The editor service |
| `:json-schema` | [`tools/json-schema`](tools/json-schema/README.md) | Writes JSON Schemas for the generated files |
| build logic | [`buildSrc`](buildSrc/README.md) | Gradle convention plugins |
| frontend | [`web/frontend`](web/frontend/README.md) | The web editor (Nuxt) |

## Getting started

Every command goes through `./toolchain.sh`. With Nix (through `direnv`, or `nix-shell`) it runs inside the
project's environment from `shell.nix`, which brings everything. Without Nix it runs commands directly and needs a
JDK 17 or newer to start Gradle (Gradle downloads JDK 25 itself), Node.js 24 and Docker for the web parts;
`./toolchain.sh doctor` checks them.

```bash
direnv allow                    # once, with Nix
./toolchain.sh doctor           # how commands run here, and whether Java, Node.js and Docker fit
./toolchain.sh fe-install       # frontend dependencies; without Nix also fe-e2e-install for the test browsers
./toolchain.sh help             # every command
./toolchain.sh verify           # what CI checks: Java build and tests, frontend tests, lint, build
./toolchain.sh be-server        # Paper test server with the plugin in core/run/
DB_PASSWORD=<password> ./toolchain.sh docker-up   # database, backend on :8080, frontend on :4000
./toolchain.sh fe-dev           # frontend dev server on :4000 against the backend on :8080
```

Some files are generated and committed. After changing a record or settings class the editor sees, run
`./toolchain.sh api-types`; tests and linters fail while the generated files are stale.

## Contributing

Branches, pull requests and writing rules are in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[GNU Affero General Public License v3](LICENSE.md).
