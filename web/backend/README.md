# Web Backend

`:web-backend`: BloodNight's web service, for now the editor service. A Minecraft server opens an editor session with its
mobs, macros, items and settings; players edit them together in the web editor; the server pulls the result back.

## Overview

- Built on Javalin 7, stores its data in PostgreSQL through SADU. The main class is `de.eldoria.bloodnight.web.WebService`.
- On start it reads its settings from the environment, migrates the database schema and starts the HTTP and
  WebSocket API.
- A background task deletes expired sessions (and catalogs older than a day that no session uses) every ten minutes,
  and releases the sections of absent participants every 30 seconds.
- Live state (who is online, who holds which section) is kept in memory, so the service runs as a single instance.
- It also serves Minecraft's item icons and names for the editor's item pickers (see below).

### Sessions, revisions and locks

A server opens a session with its editor payload and the version of the node catalog it runs; the catalog has to be
uploaded first. The answer carries a pull token for the server and a personal editor link for the first participant.
With the pull token the server can add further participants (each gets their own link and token), read the result
once the session is submitted, and close it.

Every save is a revision. Revision 0 is the payload the session was opened with; each later revision stores the JSON
Patch operations (RFC 6902) that led to it and its author, so any revision can be rebuilt by replaying them and
restored as a new revision. A revision is only written on top of the revision it was applied to; concurrent saves are
retried on the newer state (up to ten times) instead of overwriting each other.

The payload is divided into sections: `mobs/<id>`, `macros/<id>`, `items/<id>`, `settings/main`,
`settings/globalDrops` and `settings/worlds/<uid>`. A change may only touch sections that are free or held by its
sender; touching a free section takes it, and a section held by someone else answers `423 Locked`. A change above
section level (the whole payload, or e.g. `/mobs` itself) needs every section free of others, which is why whole
saves and restores do. The server's own parts (`choices`, `hashes`, `catalog` and the problem lists) cannot be changed.
A participant's sections are released when their last stream closes, or once they have not been heard from for two
minutes.

Each saved payload is checked by `PayloadValidator` against the session's catalog (node types, ports, connections,
macro and routine uses, item references). Problems are reported with the revision, never refused, since the editor
saves work in progress.

Sessions expire after `SESSION_HOURS` without use; every use moves the expiry forward.

## Layout

All classes are in `de.eldoria.bloodnight.web`.

| Class | Purpose |
|---|---|
| `WebService` | Entry point: configuration, database, API, background sweeps |
| `ServiceConfig` | Settings read from the environment |
| `Database` | Connection pool, schema migration, default query configuration |
| `EditorApp` | The HTTP routes and the WebSocket stream |
| `SessionRepository`, `StoredSession`, `StoredRevision` | Sessions and their revisions in the database |
| `ParticipantRepository`, `Participant` | Who may edit a session, by token hash |
| `CatalogRepository` | Uploaded catalogs by version, cached in memory |
| `Rooms`, `Room` | In-memory locks, presence and open streams per session |
| `Sections` | Which sections a set of operations touches |
| `JsonPatch` | RFC 6902 JSON Patch on Jackson trees |
| `PayloadValidator` | Best-effort checks of a payload against a catalog |
| `RateLimiter` | Sessions per address in a sliding window |
| `Tokens` | Random session ids and tokens; tokens are stored only as SHA-256 hashes |

Resources: `database/` holds the SADU migrations, `api/openapi.json` the generated API description,
`logback.xml` the console logging.

## Configuration

All settings are environment variables; a missing or blank variable takes the default.

| Variable | Meaning | Default |
|---|---|---|
| `PORT` | HTTP port | `8080` |
| `EDITOR_URL` | A participant's editor link; `{id}` is replaced by the session id, `{token}` by the participant token | `http://localhost:4000/session/{id}#{token}` |
| `ALLOWED_ORIGINS` | Comma separated CORS origins of the editor; empty allows any | empty |
| `SESSION_HOURS` | How long a session lives without being used | `168` |
| `SESSIONS_PER_HOUR` | Sessions one address may open per hour | `20` |
| `MAX_PAYLOAD_MB` | Largest request body | `10` |
| `ASSET_CACHE` | Where Minecraft's item assets are kept once fetched (a volume in compose) | `/var/cache/bloodnight/assets` |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `postgres` |
| `DB_USER` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | empty |
| `DB_SCHEMA` | Schema the tables live in, created by the migration | `bloodnight_web` |

## Database

Tables in `DB_SCHEMA`, created and migrated by SADU's updater on start (`src/main/resources/database/postgresql`):

- `editor_catalog`: uploaded catalogs by `version`, as JSONB.
- `editor_session`: one row per session: pull token hash, catalog version, current and initial payload, problems,
  state (`EDITING` or `SUBMITTED`), current revision, created and expiry time.
- `editor_revision`: the operations and author of each revision after 0, keyed by session and revision.
- `editor_participant`: name and token hash of everyone who may edit a session.

Revisions and participants are deleted with their session.

## Running

With Docker (`.docker/compose.yaml`, the image is built by `.docker/web.Dockerfile`):

```sh
DB_PASSWORD=<password> ./toolchain.sh docker-backend   # database and web backend on :8080
DB_PASSWORD=<password> ./toolchain.sh docker-up        # also the frontend on :4000
```

Compose sets `DB_HOST` to its database and passes `DB_PASSWORD`, `EDITOR_URL` and `ALLOWED_ORIGINS` through to the
service; `PORT` there only chooses the published host port. The database stays inside Docker there; `docker-db`
starts it alone and publishes it on `127.0.0.1` at `DB_PORT` (default `5432`), so the backend can run from source
against it:

```sh
DB_PASSWORD=<password> ./toolchain.sh docker-db    # only the database
DB_PASSWORD=<password> ./toolchain.sh be-backend   # ./gradlew :web-backend:run with the DB_* variables of the shell
                                                   # and ASSET_CACHE defaulting to web/backend/build/asset-cache
```

## Routes

All routes are under `/api/v1/editor/` and defined in `EditorRoutes` of [web-api](../api/README.md):

- `catalogs/{version}`: check, read and upload catalogs (`HEAD`, `GET`, `PUT`).
- `sessions`: open a session (`POST`).
- `sessions/{id}`: read (`GET`) or close (`DELETE`, pull token) a session.
- `sessions/{id}/participants`: add a participant (`POST`, pull token).
- `sessions/{id}/locks`: take or release a section (`POST`, `DELETE`).
- `sessions/{id}/payload` and `sessions/{id}/changes`: save a whole payload (`PUT`) or a JSON Patch batch (`POST`).
- `sessions/{id}/revisions`, `.../revisions/{revision}`, `.../revisions/{revision}/restore`: list, read and restore
  revisions.
- `sessions/{id}/submit` and `sessions/{id}/result`: mark as done, and pull the submitted session (pull token).
- `sessions/{id}/stream?token=<participant token>`: WebSocket of `StreamEvent`s; clients send `ping` to stay present.

Calls authenticate with `Authorization: Bearer <token>`: editing calls with a participant token, the server's calls
with the pull token. The full description, generated from the routes and records, is
[`src/main/resources/api/openapi.json`](src/main/resources/api/openapi.json). The stream itself is not an OpenAPI path,
but its `StreamEvent` schema is included.

### Item assets

`GET /api/v1/assets/{version}/items` answers every item of a Minecraft version with its icon and its English and
German names, plus the names of the registry entries the editor offers (enchantments, entity types, effects, biomes,
attributes, potions); `GET /api/v1/assets/{version}/textures/{item|block}/{name}.png` the icons. Both need no token and are
cacheable for good. Nothing from Mojang is part of the repository: on the first request for a version, `ItemAssets`
reads Mojang's version manifest, downloads that version's client jar (about 40 MB) and German language file, checks
both against the sha1 Mojang publishes, and `ItemAssetExtractor` takes each item's icon from its model (the flat
item texture, or one face of a block) and its names. The result lands in `ASSET_CACHE/<version>/` in one move.
Concurrent first requests share one download; a failed one answers `503` and is tried again on the next request, an
unknown version answers `404`.

## Dependencies

- Depends on: [web-api](../api/README.md) (the contract), [graph-editor-payload](../../graph-editor-payload/README.md)
  (reading catalogs), Javalin, SADU, the PostgreSQL driver, Jackson; Logback at runtime.
- Tests also use [json-schema](../../tools/json-schema/README.md) and Testcontainers.
- Used by: no Gradle module. [core](../../core/README.md) talks to it over HTTP, the [frontend](../frontend/README.md)
  over HTTP and WebSocket, with types generated from the OpenAPI description.

## Building and testing

```sh
./toolchain.sh be-test1 web-backend '*'               # all tests of this module
./toolchain.sh be-test1 web-backend '*EditorAppTest'  # one test class
./gradlew :web-backend:generateApiSpec               # rewrite openapi.json
./toolchain.sh api-types                             # all generated files, including the frontend types
```

`EditorAppTest` starts PostgreSQL 17 through Testcontainers, so these tests need a running Docker. `ApiSpecTest` fails
when a route is missing from the description or the committed `openapi.json` is stale; regenerate it after changing a
route or a record in [web-api](../api/README.md).
