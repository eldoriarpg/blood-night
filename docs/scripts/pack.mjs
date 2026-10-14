/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/**
 * Zips the resource pack in ../resourcepack into static/pack/bloodnight.zip, so the site serves it. Entries are sorted
 * and dated alike, so the same pack always gives the same file and the same hash.
 */
import {mkdirSync, readdirSync, readFileSync, writeFileSync} from 'node:fs'
import {dirname, join, relative} from 'node:path'
import {fileURLToPath} from 'node:url'
import {zipSync} from 'fflate'

const docs = dirname(dirname(fileURLToPath(import.meta.url)))
const source = join(dirname(docs), 'resourcepack')
const target = join(docs, 'static', 'pack', 'bloodnight.zip')
const date = new Date('2020-01-01T00:00:00Z')

const files = readdirSync(source, {recursive: true, withFileTypes: true})
  .filter(entry => entry.isFile())
  .map(entry => join(entry.parentPath, entry.name))
  .sort()
const entries = Object.fromEntries(files.map(file => [
  relative(source, file).split('\\').join('/'),
  [readFileSync(file), {mtime: date, level: 9}],
]))

mkdirSync(dirname(target), {recursive: true})
writeFileSync(target, zipSync(entries))
console.log(`Packed ${files.length} files into ${relative(docs, target)}.`)
