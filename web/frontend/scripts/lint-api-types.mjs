#!/usr/bin/env node
/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/**
 * API types linter: the committed generated types must be what `scripts/generate-api-types.mjs` writes from the
 * committed descriptions, so a changed record on the Java side is regenerated here rather than noticed as a field the
 * screen reads and nobody sends. Exit code 1 when either file is stale.
 */

import {readFileSync, existsSync} from 'fs'
import {renderApiTypes, renderPayloadTypes, API_TARGET, PAYLOAD_TARGET} from './generate-api-types.mjs'
import {RED, GREEN, RESET, BOLD} from './lint-utils.mjs'

const stale = []
for (const [target, render] of [[API_TARGET, renderApiTypes], [PAYLOAD_TARGET, renderPayloadTypes]]) {
    const committed = existsSync(target) ? readFileSync(target, 'utf-8') : ''
    if (committed !== await render()) stale.push(target)
}
if (stale.length > 0) {
    console.log(`${RED}${BOLD}The generated types are out of date:${RESET} ${stale.join(', ')}`)
    console.log(`  Run ${BOLD}node scripts/generate-api-types.mjs${RESET} and commit what it writes.`)
    process.exit(1)
}
console.log(`${GREEN}The generated types match the descriptions${RESET}`)
