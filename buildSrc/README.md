# Build Logic

`buildSrc`: the Gradle convention plugins every Java module of BloodNight applies, so that toolchain, repositories,
tests, formatting and static analysis are configured in one place. It is not a project module and ships no code.

## Overview

`buildSrc` is built with the `kotlin-dsl` plugin before the main build. Its `settings.gradle.kts` reads the
repository's version catalog (`gradle/libs.versions.toml`), and `build.gradle.kts` puts the catalog's generated
accessors on the plugin classpath, so the convention plugins look up versions with `the<LibrariesForLibs>()` just like
the module build scripts. It also adds the Spotless and SpotBugs Gradle plugins, with versions from the catalog.

The plugins are precompiled script plugins in `src/main/kotlin`, each applied by its id. They build on each other:

```
bloodnight.java  <-  bloodnight.library  <-  bloodnight.paper
```

## Convention plugins

### `bloodnight.java`

The base for every module. It applies `java`, Spotless and SpotBugs and configures:

- Group `de.eldoria.bloodnight`, version `1.0.0`, Java toolchain 25.
- Repositories: Maven Central, PaperMC, and EldoNexus (public and proxies).
- Dependencies: the Jackson BOM as a platform, SpotBugs annotations (compile only, main and test), JUnit Jupiter with
  parameterized tests through the JUnit BOM, and the JUnit Platform launcher.
- Tests run on the JUnit Platform, and a test run fails if any test was skipped. This catches tests that never really
  run, for example when MockBukkit throws for a method it does not implement.
- Spotless for `src/**/*.java`: license header from the root `HEADER.txt`, Palantir Java Format (version from the
  catalog, Javadoc left as written), unused imports removed, imports ordered (others, `java`, `javax`, static),
  trailing whitespace trimmed, a final newline, UTF-8.
- SpotBugs on main sources with maximum effort and the lowest report threshold; any finding fails the build. The
  filter `config/spotbugs/null.xml` limits it to the null pointer (`NP`) patterns. `spotbugsMain` writes HTML and XML
  reports; `spotbugsTest` is disabled.

### `bloodnight.library`

`bloodnight.java` plus `java-library`, and JSpecify as an `api` dependency so the `@NullMarked` / `@Nullable`
annotations reach consumers.

### `bloodnight.paper`

`bloodnight.library` for modules that run on a Paper server: Paper API as `compileOnly`, and for tests the Paper API in
the version MockBukkit supports plus MockBukkit itself. The test classpaths are forced to that Paper API version,
because the current MockBukkit fails on the newer Paper API the plugin is built against; a `TODO` marks this until
MockBukkit catches up.

## Used by

| Plugin | Modules |
|---|---|
| `bloodnight.java` | [web-backend](../web/backend/README.md) (an application, not a library) |
| `bloodnight.library` | [graph-model](../graph-model/README.md), [graph-compiler](../graph-compiler/README.md), [graph-std](../graph-std/README.md), [graph-editor-payload](../graph-editor-payload/README.md), [web-api](../web/api/README.md), [json-schema](../tools/json-schema/README.md) |
| `bloodnight.paper` | [api](../api/README.md), [bukkit-nodes](../bukkit-nodes/README.md), [catalog](../catalog/README.md), [core](../core/README.md) |

Modules add their own plugins on top where needed, for example `application` in web-backend or Shadow and the Paper
plugin tooling in core. The root `build.gradle.kts` only declares the Shadow plugin without applying it.

## Building and testing

`buildSrc` has no tests; Gradle compiles it automatically at the start of every build. The checks it sets up run
through the toolchain:

```sh
./toolchain.sh be-build          # formatting check, tests, SpotBugs, jars
./toolchain.sh be-format         # applies formatting and license headers (spotlessApply)
./toolchain.sh be-format-check   # checks formatting only
./toolchain.sh be-spotbugs       # SpotBugs over every module
```

Versions of the plugins and libraries configured here are changed in `gradle/libs.versions.toml`, not in `buildSrc`.
