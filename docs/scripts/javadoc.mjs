/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/**
 * Copies the API's javadoc from ../plugin/api/build/docs/javadoc into static/javadoc, so the site serves it next to the
 * developer pages. The javadoc is built by Gradle (`./gradlew :api:javadoc`); without it the site cannot be built.
 */
import {cpSync, existsSync, rmSync} from 'node:fs'
import {dirname, join, relative} from 'node:path'
import {fileURLToPath} from 'node:url'

const docs = dirname(dirname(fileURLToPath(import.meta.url)))
const source = join(dirname(docs), 'plugin', 'api', 'build', 'docs', 'javadoc')
const target = join(docs, 'static', 'javadoc')

if (!existsSync(join(source, 'index.html'))) {
  console.error(`No javadoc in ${relative(dirname(docs), source)}. Run ./gradlew :api:javadoc first.`)
  process.exit(1)
}
rmSync(target, {recursive: true, force: true})
cpSync(source, target, {recursive: true})
console.log(`Copied ${relative(dirname(docs), source)} to ${relative(docs, target)}.`)
