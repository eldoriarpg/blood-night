/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/**
 * Icon Linter
 *
 * Checks that every icon a file names as `['fas', 'icon-name']` is registered in `library.add()` of
 * the FontAwesome plugin. Icons register one by one so the bundler can leave out what nobody uses,
 * which makes the plugin the whole inventory and this check possible at all. An icon named but not
 * registered draws nothing.
 *
 * Exit code 1 if any unregistered icons are found.
 */
import {readFileSync} from 'node:fs'
import {join} from 'node:path'
import {BOLD, GREEN, RESET, SRC, createReporter, rel, walk} from './lint-utils.mjs'

const reporter = createReporter()
const PLUGIN = join(SRC, 'plugins', 'fontawesome.ts')

/** An icon named as a pair, such as `['fas', 'icon-name']`. */
const ICON_PAIR = /\['fas',\s*'([a-z0-9-]+)'\]/g

/** An icon named by a component attribute, such as `icon="icon-name"`. */
const ICON_ATTRIBUTE = /(?<![:\w-])icon="([a-z0-9-]+)"/g

/**
 * The name a file uses for a registered variable: `faChevronDown` is `chevron-down`.
 *
 * @param variable the imported variable
 * @returns the icon name
 */
function iconName(variable) {
    return variable
        .replace(/^fa/, '')
        .replace(/([A-Z])/g, '-$1')
        .toLowerCase()
        .replace(/^-/, '')
}

/**
 * The icons the plugin registers.
 *
 * @returns their names
 */
function registeredIcons() {
    const plugin = readFileSync(PLUGIN, 'utf-8')
    const calls = [...plugin.matchAll(/library\.add\(([^)]+)\)/g)]
    if (calls.length === 0) {
        console.error(`Could not find library.add() in ${rel(PLUGIN)}`)
        process.exit(1)
    }
    return new Set(calls.flatMap(call => call[1].split(',').map(part => part.trim()).filter(Boolean)).map(iconName))
}

/**
 * Reports every icon a file names that the plugin does not register.
 *
 * @param file the absolute path
 * @param registered the registered icon names
 */
function check(file, registered) {
    const lines = readFileSync(file, 'utf-8').split('\n')
    lines.forEach((line, index) => {
        for (const match of [...line.matchAll(ICON_PAIR), ...line.matchAll(ICON_ATTRIBUTE)]) {
            const name = match[1]
            if (registered.has(name)) continue
            const variable = `fa${name.split('-').map(part => part.charAt(0).toUpperCase() + part.slice(1)).join('')}`
            reporter.error(file, index + 1, `Icon '${name}' is not registered. Add ${variable} to src/plugins/fontawesome.ts.`)
        }
    })
}

const registered = registeredIcons()
const files = [...walk(SRC, '.vue'), ...walk(SRC, '.ts')].filter(file => !file.endsWith('.test.ts'))
files.forEach(file => check(file, registered))

if (reporter.errors.length === 0) {
    console.log(`\n${GREEN}${BOLD}Icon lint passed.${RESET} All ${registered.size} registered icons are valid.\n`)
} else {
    reporter.print()
    reporter.exit()
}
