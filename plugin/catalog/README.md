# Catalog

`:catalog`: generates the editor catalog of the BloodNight node platform at build time, with descriptions taken from
the Paper API's javadoc. The result is bundled into the plugin jar.

## Overview

The catalog is the JSON description of every node, port and type the web editor draws from. It is produced by
`CatalogGenerator` from [graph-editor-payload](../../graph/editor-payload/README.md) over the platform of
[bukkit-nodes](../bukkit-nodes/README.md).

Reflected nodes need human-readable descriptions, and the only good source is the Paper API's javadoc, which is not
available on a running server. So this module builds the catalog once, without a server, and reads the javadoc
straight from the Paper API **sources jar**:

1. The `generateCatalog` Gradle task resolves the Paper API sources jar (configuration `apiSources`) and runs
   `CatalogMain` with the output path and the sources jars as arguments.
2. `CatalogMain` creates `BloodNightPlatform` with a `JavadocDocumentation` and writes the catalog to
   `plugin/catalog/build/catalog/catalog.json`.
3. [core](../core/README.md)'s `processResources` depends on `:catalog:generateCatalog` and copies that file into the
   plugin jar as `/catalog.json`.

On the server, core's `RuntimeCatalog` generates a live catalog (from the server's own API and registries, data pack
entries included) and copies the descriptions over from the bundled one. That merged catalog is what the plugin
uploads to the editor service and what `/bloodnight export` writes.

`JavadocDocumentation` parses source files lazily with JavaParser. A description is the first sentence of the
javadoc, with inline tags (`{@link}`, `{@code}` and similar) reduced to their text and HTML removed. Methods without
javadoc of their own take it from the method they override, searching superclasses and interfaces. Nested types and
constructors are found too.

## Layout

- `de.eldoria.bloodnight.catalog.CatalogMain`: command line entry point used by `generateCatalog`. Arguments: the
  output file, then any number of sources jars. Prints the catalog version and node and type counts.
- `de.eldoria.bloodnight.catalog.JavadocDocumentation`: a `Documentation` backed by sources jars.

## Dependencies

Depends on: [bukkit-nodes](../bukkit-nodes/README.md), [graph-editor-payload](../../graph/editor-payload/README.md),
JavaParser and the Paper API (as `implementation`, since the catalog runs outside a server), plus the Paper API
sources jar.

Used by: [core](../core/README.md), as a build task dependency rather than a library dependency: core only takes the
generated `catalog.json`.

## Building and testing

```sh
./gradlew :catalog:generateCatalog                         # writes plugin/catalog/build/catalog/catalog.json
./toolchain.sh be-test1 catalog '*JavadocDocumentation*'   # the tests
./toolchain.sh be-jar                                      # builds the plugin jar, generating the catalog on the way
```

The tests read the real Paper API sources jar, passed in through the `catalog.sources` system property by the Gradle
test task.
