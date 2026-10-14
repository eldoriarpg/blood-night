#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND="$ROOT/web/frontend"
DOCS="$ROOT/docs"
COMPOSE=(docker compose -p bloodnight -f "$ROOT/.docker/compose.yaml")
COMMAND_GROUPS=(be fe docs docker)

usage() {
    cat <<'HELP'
Usage: ./toolchain.sh <command> [args]
       ./toolchain.sh <group> <command> [args]   (e.g. "fe test" for "fe-test"; a group alone lists its commands)

Every command runs inside the project's nix environment (shell.nix): through direnv when the checkout's .envrc
is allowed (direnv allow), otherwise through nix-shell. Without nix it runs directly and needs a JDK 17 or newer to
start Gradle (Gradle downloads JDK 25 for the build itself), Node.js 24 and Docker; "doctor" checks them.

Backend (Gradle)
  be-build              Everything Java: formatting check, tests, SpotBugs, jars
  be-test [args]        All Java tests, e.g. be-test --tests '*EditorAppTest'
  be-test1 <module> <pattern>
                        The tests matching a pattern in one module, e.g. be-test1 core '*Configuration*'
  be-format             Applies the Java formatting and license headers (spotlessApply)
  be-format-check       Checks the formatting without changing anything
  be-spotbugs           SpotBugs over every module
  be-snapshot           Rewrites the catalog snapshot test after an intended change to nodes; review its diff
  be-jar                Builds the plugin jar (core/build/libs)
  be-server             Starts the Paper test server with the plugin (core/run)
  be-backend            Runs the web backend from source (needs a database, see docker-db); item assets are cached
                        in web/backend/build/asset-cache unless ASSET_CACHE says otherwise

Frontend (web/frontend)
  fe-install            npm install: reconciles node_modules and the lock file with package.json
  fe-install-clean      npm ci: installs exactly what the lock file names, as CI does
  fe-dev                Dev server on http://localhost:4000, proxying /api to NUXT_BACKEND_URL (default :8080)
  fe-test               Unit tests, including the lint rules' tests
  fe-test1 <pattern>    The unit tests of files matching a pattern
  fe-lint [name]        Every linter of scripts/linters.mjs, or one of them by name
  fe-eslint [args]      ESLint alone, e.g. fe-eslint --fix
  fe-typecheck          vue-tsc for the app, tsc for the server and the end-to-end stories
  fe-build              Lint, type checks and the production build
  fe-preview            Serves the production build on port 4000
  fe-e2e [args]         End-to-end stories against BN_E2E_URL (default the dev server on :4000, started when
                        not running)
  fe-e2e1 <pattern>     The end-to-end stories matching a pattern, in chromium
  fe-e2e-install        Downloads the browsers of the end-to-end stories (nix provides them already)

Documentation (docs, Docusaurus)
  docs-install          npm install for the documentation site
  docs-install-clean    npm ci, as CI does
  docs-dev              Dev server on http://localhost:4001
  docs-generate         Builds the catalog and plugin descriptor, then rewrites the reference pages
  docs-check            Fails when the reference pages are stale
  docs-build            The static site in docs/build; fails on broken links

Generated files
  api-types             Payload schema, API description and the frontend types, after changing a record or a
                        settings class the editor sees

Docker (.docker/compose.yaml, needs DB_PASSWORD)
  docker-up             Database, web backend on :8080 and frontend on :4000, rebuilt
  docker-db             Only the database, on 127.0.0.1 at DB_PORT (default 5432) for be-backend
  docker-backend        Database and web backend, rebuilt
  docker-down           Stops everything, keeps the data
  docker-reset          Stops everything and deletes the data
  docker-logs [service] Follows the logs

Everything
  verify                be-build, fe-test, fe-build, docs-check and docs-build: what CI checks
  doctor                Shows how commands run here and whether Java, Node.js and Docker are fit
HELP
}

environment() {
    if [ -f "$ROOT/.envrc" ] && command -v direnv >/dev/null 2>&1 && direnv exec "$ROOT" true >/dev/null 2>&1; then
        echo direnv
    elif command -v nix-shell >/dev/null 2>&1 && [ -z "${IN_NIX_SHELL:-}" ]; then
        echo nix-shell
    else
        echo system
    fi
}

ENVIRONMENT="$(environment)"

run() {
    case "$ENVIRONMENT" in
        direnv)    direnv exec "$ROOT" "$@" ;;
        nix-shell) nix-shell "$ROOT/shell.nix" --run "$(printf '%q ' "$@")" ;;
        *)         "$@" ;;
    esac
}

major_version() {
    sed -n "s/$2/\1/p" <<<"$1" | head -n 1
}

java_major() {
    major_version "$(run java -version 2>&1)" '.*version "\([0-9]*\).*'
}

node_major() {
    major_version "$(run node --version 2>/dev/null)" '^v\([0-9]*\).*'
}

require() {
    local tool="$1" have="$2" least="$3" hint="$4"
    if [ -z "$have" ]; then
        echo "toolchain: $tool not found. $hint" >&2
        exit 1
    elif [ "$have" -lt "$least" ]; then
        echo "toolchain: $tool $have found, $least or newer needed. $hint" >&2
        exit 1
    fi
}

needs_java() {
    [ "$ENVIRONMENT" = system ] || return 0
    require Java "$(java_major)" 17 "Install a JDK (17 or newer starts Gradle, which downloads JDK 25 for the build) or use nix."
}

needs_node() {
    [ "$ENVIRONMENT" = system ] || return 0
    require Node.js "$(node_major)" 24 "Install Node.js 24 (web/frontend/.nvmrc names it for nvm and fnm) or use nix."
}

doctor() {
    echo "Commands run through: $ENVIRONMENT"
    echo "Java:    $(java_major || true)"
    echo "Node.js: $(node_major || true)"
    if command -v docker >/dev/null 2>&1; then echo "Docker:  $(docker --version)"; else echo "Docker:  not found (docker-* commands need it)"; fi
    if [ "$ENVIRONMENT" = system ]; then
        needs_java
        needs_node
        echo "Run ./toolchain.sh fe-install, fe-e2e-install and docs-install once before the frontend and docs commands."
    fi
}

fe() {
    needs_node
    cd "$FRONTEND"
}

docs() {
    needs_node
    cd "$DOCS"
}

be() {
    needs_java
    cd "$ROOT"
}

needs_password() {
    if [ -z "${DB_PASSWORD:-}" ]; then
        echo "toolchain: set DB_PASSWORD for the database, e.g. DB_PASSWORD=\$(op read op://...) ./toolchain.sh $1" >&2
        exit 1
    fi
}

is_group() {
    local candidate
    for candidate in "${COMMAND_GROUPS[@]}"; do
        [ "$1" = "$candidate" ] && return 0
    done
    return 1
}

list_group() {
    sed -n 's/^    \([a-z][a-z0-9|_-]*\)).*/\1/p' "$ROOT/toolchain.sh" | tr '|' '\n' | sed -n "s/^$1-//p"
}

if is_group "${1:-}"; then
    if [ $# -ge 2 ]; then
        set -- "$1-$2" "${@:3}"
    else
        echo "Commands in '$1':" >&2
        list_group "$1" | sed "s|^|  $1 |" >&2
        exit 2
    fi
fi

cmd="${1:-help}"
shift || true

case "$cmd" in
    be-build)        be; run ./gradlew build "$@" ;;
    be-test)         be; run ./gradlew test "$@" ;;
    be-test1)        be; run ./gradlew ":${1:?module}:test" --tests "${2:?pattern}" ;;
    be-format)       be; run ./gradlew spotlessApply ;;
    be-format-check) be; run ./gradlew spotlessCheck ;;
    be-spotbugs)     be; run ./gradlew spotbugsMain ;;
    be-snapshot)     be; run ./gradlew :graph-editor-payload:test -Dsnapshot.update=true ;;
    be-jar)          be; run ./gradlew :core:shadowJar ;;
    be-server)       be; run ./gradlew :core:runServer ;;
    be-backend)      be; ASSET_CACHE="${ASSET_CACHE:-$ROOT/web/backend/build/asset-cache}" run ./gradlew :web-backend:run ;;
    fe-install)       fe; run npm install "$@" ;;
    fe-install-clean) fe; run npm ci "$@" ;;
    fe-dev)           fe; run npx nuxi dev "$@" ;;
    fe-test)          fe; run npx vitest run "$@" ;;
    fe-test1)         fe; run npx vitest run "${1:?pattern}" ;;
    fe-lint)          fe; run node scripts/lint.mjs "$@" ;;
    fe-eslint)        fe; run npx eslint --max-warnings=0 "${@:-src}" ;;
    fe-typecheck)     fe; run npx vue-tsc --noEmit -p . && run npx tsc --noEmit -p .nuxt/tsconfig.server.json && run npx tsc -p tsconfig.e2e.json ;;
    fe-build)         fe; run node scripts/build.mjs "$@" ;;
    fe-preview)       fe; NITRO_PORT=4000 run node .output/server/index.mjs ;;
    fe-e2e)           fe; run npx playwright test "$@" ;;
    fe-e2e1)          fe; run npx playwright test --project=chromium -g "${1:?pattern}" ;;
    fe-e2e-install)   fe; run npx playwright install --with-deps chromium firefox ;;
    docs-install)       docs; run npm install "$@" ;;
    docs-install-clean) docs; run npm ci "$@" ;;
    docs-dev)           docs; run npm start -- "$@" ;;
    docs-generate)
        be
        run ./gradlew :catalog:generateCatalog :core:generatePaperPluginDescription -q
        docs
        run node scripts/generate.mjs
        ;;
    docs-check)         docs; run node scripts/generate.mjs --check ;;
    docs-build)         docs; run npm run build ;;
    api-types)
        be
        run ./gradlew :core:generatePayloadSchema :web-backend:generateApiSpec -q
        fe
        run node scripts/generate-api-types.mjs
        ;;
    docker-up)      needs_password "$cmd"; "${COMPOSE[@]}" up -d --build ;;
    docker-db)      needs_password "$cmd"; "${COMPOSE[@]}" -f "$ROOT/.docker/compose.database-port.yaml" up -d postgres ;;
    docker-backend) needs_password "$cmd"; "${COMPOSE[@]}" up -d --build postgres web ;;
    docker-down)    DB_PASSWORD="${DB_PASSWORD:-unused}" "${COMPOSE[@]}" down ;;
    docker-reset)   DB_PASSWORD="${DB_PASSWORD:-unused}" "${COMPOSE[@]}" down -v ;;
    docker-logs)    DB_PASSWORD="${DB_PASSWORD:-unused}" "${COMPOSE[@]}" logs -f "$@" ;;
    verify)
        "$0" be-build
        "$0" fe-test
        "$0" fe-build
        "$0" docs-check
        "$0" docs-build
        ;;
    doctor)         doctor ;;
    help|-h|--help) usage ;;
    *)
        echo "toolchain: unknown command '$cmd'" >&2
        usage >&2
        exit 2
        ;;
esac
