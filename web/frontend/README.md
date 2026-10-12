# BloodNight Web Frontend

The web editor of BloodNight: a Nuxt 4 app in which the mobs, macros, items and settings of a server are edited,
mob and macro behaviour as node graphs on a canvas.

Stack: Nuxt 4 (Vue 3 `<script setup lang="ts">`, TypeScript strict), Vite, Tailwind 4 (tokens in `src/style.css`,
dark only, flat), reka-ui, axios, @nuxtjs/i18n (English and German), @vue-flow/core for the node canvas, FontAwesome
through `AppIcon`, Vitest and Playwright. The setup and the lint rules were taken over from another project's frontend.

## How it connects to the backend

Everything below `/api` belongs to the web backend. The dev server runs on port 4000 and proxies `/api`, HTTP and
WebSocket alike, to `NUXT_BACKEND_URL` (default `http://localhost:8080`). The built server does the same at runtime:
`server/middleware/backend-proxy.ts` forwards HTTP, and `server/routes/api/v1/editor/sessions/[id]/stream.ts` relays
the session stream. The address is read per request, so one build serves any backend.

Nobody opens the editor from the landing page. A player runs `/bloodnight editor` on the server, which opens a
session and hands them a personal link to `/session/<id>#<token>`. The token sits in the URL fragment, which browsers
never send to a server, so `/session/**` renders in the browser only (the landing page `/` renders on the server).
API calls carry the token as a bearer header; the stream carries it as a query parameter, since a browser cannot set
headers on a WebSocket.

## Getting started

All commands run from the repository root through `./toolchain.sh` (`./toolchain.sh help` lists them all).

```bash
./toolchain.sh fe-install    # npm install (Node.js 24, see .nvmrc)
./toolchain.sh fe-dev        # dev server on http://localhost:4000
```

The dev server needs a backend to talk to. Either start the database and backend in Docker
(`DB_PASSWORD=<password> ./toolchain.sh docker-backend`), or the whole stack including a built frontend on 4000
(`docker-up`). `docker-db` with `be-backend` runs the backend from source instead.

## Source layout

Everything lives below `src/`:

- `pages/`: thin route files (`index.vue`, `session/[id].vue`, `style.vue`), each rendering one view.
- `views/`: one `<Name>View.vue` per page, its parts in `views/<name>view/` (the editor's areas: workspace, items,
  settings, revisions).
- `layouts/`: `default` (header bar above the page) and `editor`.
- `components/`: reusable base components (buttons, inputs, containers, feedback, typography). `/style` shows them all.
- `composables/`: the editor logic and shared UI behaviour.
- `util/`: pure logic with tests, plus the `*State.ts` modules holding shared state.
- `api/`: the HTTP calls (`editor.ts`), the stream (`editorStream.ts`) and the generated types (`generated/`).
- `i18n/`: the texts (`en.ts`, `de.ts`) and the vue-i18n config.
- `plugins/`: the FontAwesome plugin, which registers every icon the app uses.

The most important modules:

- `useEditorSession`: opens a session, applies the revisions the stream reports in order, reloads on a gap or a lost
  stream.
- `useEditorChanges`: sends every edit as a JSON Patch.
- `useEditorLocks`: who holds which section; taking and releasing them.
- `util/patch/`: JSON Patch and JSON Pointer.
- `util/graph/`: the graph as drawn, its layout, pins, and `GraphChanges`, which builds the patch operations for graph
  edits; `nodeEdits` (settings and values edited on nodes), `routineCalls` (call nodes for the routines a graph
  defines), `executionOrder` and `graphPlayback` (the order a chain runs in, played on the canvas), `choiceLabels`
  (names of choices in the editor's language).
- `util/session/sections.ts`: which sections a change touches, mirroring the service's rules.
- `util/settings/`: settings forms built from the payload schema, and copying settings between worlds.
- `useItemAssets` and `util/items/`: Minecraft's item icons and names (from the web backend's asset routes) for
  the item pickers, searchable by name in the editor's language, English name or key.

State shared between composables lives in `util/*State.ts` and is reached through composables only; ESLint enforces
this, and refuses mutable module-level state anywhere.

## How editing works

Every edit is a JSON Patch sent through `useEditorChanges().apply()`. The payload is divided into sections (one per
mob, macro, item, the main settings, the global drops and each world's settings). Before sending, the editor works out
which sections a change touches with the same rules the service uses; a change touching a section another participant
holds is refused on the spot, and sections held by others show read only. An accepted change is shown at once as
pending on top of the service's payload. It is taken back when the service refuses it, and the session is reloaded when
the stream does not report it back within a few seconds. The stream reports every revision to every participant, which
is how several people edit one session at once.

## The canvas

Graphs are edited on a Vue Flow canvas, one canvas per open mob or macro:

- Settings and typed-in values are edited on the nodes themselves; the inspector (double click a node) shows the same
  with the node's description. Choices use the editor's own dropdown (`ChoicePicker`), searchable for long lists;
  name fields of variables, routines, gates and timers suggest the names the graph already uses.
- Nodes are added from the palette, by double clicking the empty canvas, or by letting go of a wire on it (then only
  nodes that take the wire are offered). A double click on a wire puts a reroute on it.
- Nodes snap to a grid (Alt places freely), Space and drag moves the view, a drag selects in a box. The Shortcuts
  button lists all of this.
- The play button of an event lights up the nodes and wires it runs, in order, with delays waiting their time.

## Generated API types

`src/api/generated/` holds `schema.ts` (from the backend's OpenAPI description), `payload.ts` and
`payload.schema.json` (from the editor payload's JSON Schema). They are generated, committed and never edited by hand.
Every string enum also becomes a constant of the same name holding its values, so import those rather than copying the
values. After changing a record or settings class on the Java side, regenerate everything with:

```bash
./toolchain.sh api-types
```

The `api-types` linter fails when the committed files differ from what the generator writes.

## Translations

Every user-facing text is in `src/i18n/en.ts` and `src/i18n/de.ts`; English is the fallback. ESLint reports missing
keys, unused keys and messages vue-i18n would split into plural choices.

## Linting

`./toolchain.sh fe-lint` runs every linter of `scripts/linters.mjs`; `fe-lint <name>` runs one. Findings are fixed,
not suppressed.

- `eslint`: Nuxt's config plus accessibility, vue-i18n, layer import rules and the custom rules below, no warnings
  allowed.
- `duplication`: jscpd over `src/` (`.jscpd.json`).
- `icons`: every icon named in the code is registered in `src/plugins/fontawesome.ts`.
- `browser-storage`: every `localStorage` key is declared in `browser-storage.json`; cookies and session storage are
  refused.
- `em-dash`: no em dash in any text file of the frontend, entities included.
- `comments`: only doc comments and `TODO`s, in every source including templates.
- `api-types`: the generated types match their descriptions.

The custom ESLint rules (`eslint/`, prefix `bloodnight/`):

- `button-row`: two or more labelled buttons on one line go in `ButtonRow`.
- `context-title`: a page whose address points at one thing is titled after that thing.
- `max-function-lines`: a maximum number of lines per function, composable wrappers excepted.
- `no-dead-button-size`: no `size` on button components that declare no size prop.
- `no-hand-kept-enum`: no hand-kept copies of the generated enum constants.
- `no-module-state`: no mutable module-level state; `useState` and `browserRef` stay server safe.
- `no-stacked-inline-text`: no two inline typography components stacked with only a line break between them.
- `repeated-class-pattern`: an element pattern repeated across the views becomes a component.
- `route-view-content`: a page renders a view that reaches a titled `ViewContent`.
- `section-density`: limits how many sections one element near the top of a view may stack.
- `styled-elements`: the styled components instead of raw controls, headings and badges.
- `template-block-size`: limits how many lines one template block of a view may span.
- `i18n-no-plural-messages`: no messages vue-i18n splits into plural choices.
- `i18n-unused-keys`: no translations nothing refers to.

## Tests

Unit tests run on Vitest with the Nuxt environment (a file touching no Nuxt API can opt into `happy-dom` for speed).
They cover `src/`, `server/`, `scripts/` and the ESLint rules in `eslint/`.

```bash
./toolchain.sh fe-test             # all unit tests
./toolchain.sh fe-test1 <pattern>  # the tests of matching files
./toolchain.sh fe-typecheck        # vue-tsc for the app, tsc for the server and the stories
```

End-to-end stories live in `e2e/` as `<feature>.e2e.ts` and run on Playwright in Chromium and Firefox against
`BN_E2E_URL` (default `http://localhost:4000`). Without `BN_E2E_URL` and outside CI, Playwright starts the dev
server when none is running. The editor
stories need no backend: `e2e/editorFixture.ts` answers the session, catalog, lock and change calls and the stream
itself.

```bash
./toolchain.sh fe-e2e-install      # once without Nix: downloads Chromium and Firefox (Nix provides them)
./toolchain.sh fe-e2e              # all stories
./toolchain.sh fe-e2e1 <pattern>   # matching stories, Chromium only
```

## Build and Docker

`./toolchain.sh fe-build` runs `scripts/build.mjs`: all linters, the type checks, then `nuxi build` into `.output/`.
`fe-preview` serves that build on port 4000.

`.docker/web-frontend.Dockerfile` builds the image from the repository root: it installs with `npm ci`, runs
`nuxi build`, and runs `node server/index.mjs` on port 4000 in a `node:24-alpine` image. Set `NUXT_BACKEND_URL` to
the backend's address; `.docker/compose.yaml` points it at the `web` service.
