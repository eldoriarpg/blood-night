/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/**
 * Copies the production compose file of ../.docker into static/self-hosting/compose.yaml, so the site offers the same
 * file the hosted editor runs on.
 */
import {copyFileSync, mkdirSync} from 'node:fs'
import {dirname, join, relative} from 'node:path'
import {fileURLToPath} from 'node:url'

const docs = dirname(dirname(fileURLToPath(import.meta.url)))
const source = join(dirname(docs), '.docker', 'compose.prod.yaml')
const target = join(docs, 'static', 'self-hosting', 'compose.yaml')

mkdirSync(dirname(target), {recursive: true})
copyFileSync(source, target)
console.log(`Copied ${relative(dirname(docs), source)} to ${relative(docs, target)}.`)
