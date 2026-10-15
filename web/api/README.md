# Web API

`:web-api`: the contract between the plugin, the web backend and the web editor. It holds the editor service's routes
and the records they exchange, with no Bukkit or server code, so both the plugin and the backend can depend on it.

## Overview

Everything lives in `de.eldoria.bloodnight.web.api.editor`. The types are plain Java records and enums serialized
with Jackson. The frontend does not use them directly: the backend generates an OpenAPI description from them (see
[web-backend](../backend/README.md)), and the frontend's TypeScript types are generated from that description.

The package is `@NullMarked` (JSpecify): a field is non-null unless marked `@Nullable`.

## Layout

### Routes

- `EditorRoutes`: the route constants of the editor API, version 1, all under `/api/v1/editor/`, plus
  `path(route, values...)` to fill their placeholders. Its doc comment lists every method, body, answer and which
  token each call takes.

### Requests

- `CreateSession`: opens a session: the catalog version the server runs, its editor payload and the name of the
  first participant.
- `AddParticipant`: lets another person edit a session, by name.
- `LockRequest`: takes or releases a section.
- `ChangeBatch`: JSON Patch operations (RFC 6902) on the payload, saved as one revision.

### Responses

- `SessionCreated`: session id, the opener's editor link, the pull token and the expiry.
- `ParticipantCreated`: a participant's name and personal editor link.
- `SessionView`: a session as the editor and the pulling server see it: state, revision, catalog version, payload,
  problems, held sections, who is online, the asking participant (`you`, null for the server) and the expiry.
- `ChangeAccepted`: the revision a save produced and the problems found in the payload.
- `RevisionInfo`: one revision: number, time, number of operations and author (none for revision 0).
- `PayloadProblem`: a JSON pointer into the payload and what is wrong there.
- `LockInfo`: a section and the name of the participant holding it.

### Stream

- `StreamEvent`: a message on a session's WebSocket. Only the fields the event's type uses are written
  (`@JsonInclude(NON_NULL)`); static factories (`hello`, `change`, `lock`, `unlock`, `presence`, `of`) build each kind.
- `StreamEventType`: `HELLO` (on connect: revision, locks, who is online), `CHANGE`, `LOCK`, `UNLOCK`, `PRESENCE`,
  `SUBMITTED` and `CLOSED`.

### States

- `SessionState`: `EDITING` while the session is being edited, `SUBMITTED` once it was marked as done.

## Tokens

Each session has a pull token, which the server that opened it uses to add participants, pull the result and close
the session. Each participant has their own token, carried in their editor link; editing calls use it. Both are sent
as `Authorization: Bearer <token>`; the stream takes the participant token as the `token` query parameter.

## Dependencies

- Depends on: Jackson databind and annotations (exposed as `api`), JSpecify through the library convention.
- Used by: [web-backend](../backend/README.md) (serves the routes) and [core](../../plugin/core/README.md) (the plugin's
  editor client, which calls them). The [frontend](../frontend/README.md) sees these types only through the generated
  OpenAPI description.

## Building and testing

The module has no tests of its own. After changing a record or a route, regenerate the generated files and commit
them; the backend's `ApiSpecTest` fails while `openapi.json` is stale:

```sh
./toolchain.sh api-types
```
