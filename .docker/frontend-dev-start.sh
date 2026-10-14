#!/bin/sh
# Installs node_modules only when package-lock.json changed since the last install, then runs the Nuxt dev server.
set -e

stamp=node_modules/.lock-stamp
want=$(md5sum package-lock.json | cut -d' ' -f1)

if [ "$(cat "$stamp" 2>/dev/null)" = "$want" ]; then
    echo "[frontend] node_modules matches package-lock.json, no install needed."
else
    echo "[frontend] package-lock.json changed, running npm ci. The dev server starts after it."
    npm ci
    echo "$want" > "$stamp"
fi

echo "[frontend] Starting the dev server on port $NITRO_PORT."
exec npx nuxi dev --host "$NITRO_HOST" --port "$NITRO_PORT"
