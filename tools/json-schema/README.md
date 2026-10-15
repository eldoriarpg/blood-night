# JSON Schema

`:json-schema`: a small build tool that writes JSON Schemas (draft 2020-12, which OpenAPI 3.1 also uses) from Java
records and classes. It is used only by test-scope generators that produce the committed schema files the web editor
builds its types from.

## Overview

The single class `de.eldoria.bloodnight.schema.JsonSchemas` describes types the way a Jackson mapper with field
visibility reads and writes them, which is how the plugin's configuration and the editor payload are mapped: records
by their components, other classes by their fields.

Two ways to use it:

- `JsonSchemas.document(root, mapper)`: a complete schema document with `$schema`, a `title`, the root's schema and
  every definition it needs under `$defs`.
- `new JsonSchemas(referencePrefix, mapper)`, then `schema(type)` for each type and `definitions()` for everything
  collected. The prefix is where references point, `#/$defs/` for a plain schema or `#/components/schemas/` for an
  OpenAPI document.

The mapper is only used to write the default values of class fields as the configuration would write them.

## Mapping rules

| Java type | Schema |
|---|---|
| `String`, `UUID`, `char` / `Character` | `string` |
| `boolean` / `Boolean` | `boolean` |
| `int`, `long`, `short`, `byte` and their boxes | `integer` |
| `double`, `float`, other `Number`s | `number` |
| `Instant` | `string` with format `date-time` |
| `Object`, `JsonNode` | `{}`, accepts anything |
| arrays, `Collection`s | `array` of the element type; `Set`s add `uniqueItems` |
| `Map<K, V>` | `object` whose `additionalProperties` follow `V` |
| any enum | a named definition: a `string` listing the constant names |
| classes in `de.eldoria.*` packages | a named definition, referenced with `$ref` |
| other classes (e.g. item stacks) | an opaque `object` |

### Definitions

- Definitions are named by the class's simple name. When a second class claims a name already taken, it is qualified
  by its last package segment and outer classes, joined with `_`.
- Self-referencing and mutually referencing types work; each definition is built once.

### Records

- One property per component, named by `@JsonProperty` if present; `@JsonIgnore`d components are left out.
- Every component is `required` unless it is annotated `@Nullable` (any annotation of that simple name, on the
  component or its type), or the record carries `@JsonInclude` with a value other than `ALWAYS` or `USE_DEFAULTS`,
  in which case nothing is required.

### Classes

- One property per field, inherited fields first. Names follow `@JsonProperty`; static, transient, synthetic and
  `@JsonIgnore`d fields are left out.
- Fields are never required. The class is instantiated through its no-argument constructor (private is fine) and each
  non-null field value becomes the property's `default`, written by the mapper. A property that is a `$ref` is wrapped
  in `allOf` so the default can sit beside it. Without a usable constructor there are no defaults.

## Dependencies

- Depends on: Jackson databind and annotations (exposed as `api`), JSpecify through the library convention.
- Used by (test scope only):
  - [core](../../plugin/core/README.md): `PayloadSchema`, run by `./gradlew :core:generatePayloadSchema`, writes
    `web/frontend/src/api/generated/payload.schema.json` from the editor payload and the catalog.
  - [web-backend](../../web/backend/README.md): `ApiSpec`, run by `./gradlew :web-backend:generateApiSpec`, writes the
    schemas of `web/backend/src/main/resources/api/openapi.json` from the [web-api](../../web/api/README.md) records.

The [frontend](../../web/frontend/README.md) generates its TypeScript types from both files.

## Building and testing

```sh
./toolchain.sh be-test1 json-schema '*'
./toolchain.sh api-types   # regenerates every schema file and the frontend types
```

`JsonSchemasTest` covers records with required and nullable components, records that leave values out, classes with
defaults, renamed, ignored, static and transient fields, and inherited fields.
