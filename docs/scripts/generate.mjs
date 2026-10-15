/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/**
 * Writes the reference pages under docs/reference from what the build produces: the node catalog, the settings
 * schema and the plugin descriptor. With --check it writes nothing and fails when a page would change.
 */
import {mkdirSync, readdirSync, readFileSync, rmSync, writeFileSync, existsSync} from 'node:fs'
import {dirname, join, relative} from 'node:path'
import {fileURLToPath} from 'node:url'
import {parse, stringify} from 'yaml'

const docs = dirname(dirname(fileURLToPath(import.meta.url)))
const root = dirname(docs)
const target = join(docs, 'docs', 'reference')
const check = process.argv.includes('--check')

const sources = {
  catalog: join(root, 'plugin', 'catalog', 'build', 'catalog', 'catalog.json'),
  schema: join(root, 'web', 'frontend', 'src', 'api', 'generated', 'payload.schema.json'),
  descriptor: join(root, 'plugin', 'core', 'build', 'generated', 'plugin-yml', 'Paper', 'paper-plugin.yml'),
}

const generatedNote = '{/* Made by docs/scripts/generate.mjs. Change the plugin, then run ./toolchain.sh docs-generate. */}'

const categoryNames = {
  event: 'Triggers and events',
  mob: 'The mob',
  entity: 'Entities',
  living_entity: 'Living entities',
  player: 'Players',
  projectile: 'Projectiles',
  world: 'The world',
  location: 'Locations',
  vector: 'Vectors',
  block: 'Blocks',
  item: 'Items',
  flow: 'Flow',
  logic: 'Logic',
  math: 'Math',
  function: 'Chance and cooldown',
  timer: 'Timers',
  variable: 'Variables',
  value: 'Values',
  macro: 'Macros',
  routine: 'Routines',
  layout: 'Layout',
  debug: 'Debugging',
  object: 'Objects',
}

/**
 * @param {string} text any text from the catalog or schema
 * @returns {string} the text safe as MDX prose: no tags, no expressions
 */
function prose(text) {
  return (text ?? '').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('{', '\\{').replaceAll('}', '\\}')
}

/**
 * @param {string} text any text from the catalog or schema
 * @returns {string} the text safe for one cell of a Markdown table
 */
function cell(text) {
  return prose(text).replaceAll('|', '\\|').replaceAll('\n', ' ').trim()
}

/**
 * @param {string} text text shown as inline code in a table cell, where MDX takes it literally
 * @returns {string} the text safe for that cell
 */
function codeCell(text) {
  return text.replaceAll('|', '\\|').replaceAll('\n', ' ').trim()
}

/**
 * @param {string} id a category id
 * @returns {string} the page's file name and slug part
 */
function slug(id) {
  return id.replaceAll('_', '-')
}

/**
 * @returns {Map<string, string>} every page of the reference, by its path relative to the reference folder
 */
function pages() {
  const result = new Map()
  const catalog = JSON.parse(readFileSync(sources.catalog, 'utf8'))
  const schema = JSON.parse(readFileSync(sources.schema, 'utf8'))
  const descriptor = parse(readFileSync(sources.descriptor, 'utf8'))

  result.set('_category_.json', JSON.stringify({label: 'Reference', position: 1}, null, 2) + '\n')
  result.set('settings.md', settingsPage(schema))
  result.set('permissions.md', permissionsPage(descriptor))
  for (const [path, page] of nodePages(catalog)) result.set(path, page)
  return result
}

/**
 * @param {object} catalog the node catalog
 * @returns {Map<string, string>} the node overview and one page per category
 */
function nodePages(catalog) {
  const typeNames = new Map(catalog.types.map(type => [type.id, type.name]))
  const variableNames = {SELF: 'the mob', EXTENSION: 'the extension'}
  const typeName = ref => {
    if (ref.variable) return variableNames[ref.variable] ?? (ref.numeric ? 'any number' : 'any type')
    const raw = ref.id ?? ref.raw
    const name = typeNames.get(raw) ?? raw.split('.').pop()
    return ref.arguments?.length ? `${name} of ${ref.arguments.map(typeName).join(', ')}` : name
  }
  const byCategory = new Map()
  for (const node of catalog.nodes) {
    if (!byCategory.has(node.category)) byCategory.set(node.category, [])
    byCategory.get(node.category).push(node)
  }
  const categories = [...byCategory.keys()].sort((a, b) => label(a).localeCompare(label(b)))
  const result = new Map()
  result.set('nodes/_category_.json', JSON.stringify({label: 'Nodes', position: 3}, null, 2) + '\n')

  const overview = [
    '---',
    'title: Nodes',
    'description: Every node you can use in a mob\'s graph, by group.',
    'sidebar_position: 0',
    '---',
    '',
    generatedNote,
    '',
    '# Nodes',
    '',
    `These are all ${catalog.nodes.length} nodes you can use in a mob's graph. Each group has its own page.`,
    '',
    '| Group | Nodes |',
    '|---|---|',
    ...categories.map(category =>
      `| [${label(category)}](./${slug(category)}.md) | ${byCategory.get(category).length} |`),
    '',
  ]
  result.set('nodes/index.md', overview.join('\n'))

  categories.forEach((category, index) => {
    const nodes = byCategory.get(category).sort((a, b) => (a.kind === 'entry' ? 0 : 1) - (b.kind === 'entry' ? 0 : 1) || a.name.localeCompare(b.name))
    const lines = [
      '---',
      `title: ${label(category)}`,
      `description: "${label(category)}: every node of this group, with its inputs and outputs."`,
      `sidebar_position: ${index + 1}`,
      '---',
      '',
      generatedNote,
      '',
      `# ${label(category)}`,
      '',
    ]
    for (const node of nodes) {
      lines.push(`## ${node.name}`, '', `\`${node.id}\``, '')
      if (node.description) lines.push(prose(node.description), '')
      if (node.inputs?.length) {
        lines.push('| In | Type | What it is |', '|---|---|---|')
        for (const pin of node.inputs) {
          const optional = pin.optional ? ' (optional)' : ''
          lines.push(`| ${cell(pin.name)}${optional} | ${cell(typeName(pin.type))} | ${cell(pin.description)} |`)
        }
        lines.push('')
      }
      if (node.outputs?.length) {
        lines.push('| Out | Type | What it is |', '|---|---|---|')
        for (const pin of node.outputs) {
          lines.push(`| ${cell(pin.name)} | ${cell(typeName(pin.type))} | ${cell(pin.description)} |`)
        }
        lines.push('')
      }
      if (node.executions?.length) {
        lines.push(`Goes on with: ${node.executions.map(execution => `**${execution.name}**`).join(', ')}`, '')
      }
    }
    result.set(`nodes/${slug(category)}.md`, lines.join('\n'))
  })
  return result
}

/**
 * @param {string} category a category id
 * @returns {string} its name for people
 */
function label(category) {
  return categoryNames[category] ?? category
}

/**
 * @param {object} schema the payload schema
 * @returns {string} the settings page: every key with its type and default, per file
 */
function settingsPage(schema) {
  const defs = schema.$defs
  const files = [
    ['config.yml', 'ConfigFile', 'Settings for the whole server.'],
    ['worlds/<world id>.yml', 'WorldSettings', 'Settings for one world. Every world has its own file.'],
    ['global_drops.yml', 'GlobalDrops', 'Drops every world can use.'],
  ]
  const lines = [
    '---',
    'title: Settings',
    'description: Every setting of BloodNight with its default value.',
    'sidebar_position: 1',
    '---',
    '',
    generatedNote,
    '',
    '# Settings',
    '',
    ':::tip[Use the web editor]',
    'The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks',
    'what you type. This page shows the files, so you know what the editor writes for you.',
    ':::',
    '',
    'Here you find every setting with its default value. The guide pages explain what they do.',
    '',
    'Missing keys are added with their default when the plugin loads the file.',
    '',
  ]
  for (const [file, def, about] of files) {
    lines.push(`## ${file.replace('<world id>', '&lt;world id&gt;')}`, '', about, '')
    lines.push('| Key | Type | Default |', '|---|---|---|')
    for (const [key, type, value] of keys(defs, defs[def], '')) {
      lines.push(`| \`${key}\` | ${type} | ${value} |`)
    }
    lines.push('', 'The whole file with its defaults:', '', '```yaml', stringify(defaults(defs, defs[def])).trimEnd(), '```', '')
  }
  return lines.join('\n')
}

/**
 * @returns {object} the definition behind a property: its own, or the one it references
 */
function resolve(defs, property) {
  const ref = property.$ref ?? property.allOf?.[0]?.$ref
  return ref ? defs[ref.split('/').pop()] : property
}

/**
 * @returns {Array<[string, string, string]>} key path, type and default of every leaf below a definition
 */
function keys(defs, definition, prefix) {
  const rows = []
  for (const [name, property] of Object.entries(definition.properties ?? {})) {
    const path = prefix + name
    const resolved = resolve(defs, property)
    if (resolved.type === 'object' && resolved.properties) {
      rows.push(...keys(defs, resolved, path + '.'))
      continue
    }
    const limit = property.maxItems === undefined ? '' : `, at most ${property.maxItems}`
    rows.push([path, typeOf(defs, resolved) + limit, shown(property.default)])
  }
  return rows
}

/**
 * @returns {string} a short type name for people
 */
function typeOf(defs, definition) {
  if (definition.enum) return definition.enum.map(value => `\`${value}\``).join(', ')
  if (definition.type === 'array') return `list of ${typeOf(defs, resolve(defs, definition.items ?? {}))}`
  if (definition.type === 'object') return 'map'
  return {integer: 'whole number', number: 'number', boolean: 'true or false', string: 'text'}[definition.type] ?? 'value'
}

/**
 * @returns {string} a default value for a table cell
 */
function shown(value) {
  if (value === undefined) return ''
  if (typeof value === 'string') return value === '' ? '(empty)' : `\`${codeCell(value)}\``
  return `\`${codeCell(JSON.stringify(value))}\``
}

/**
 * @returns {object} the defaults of a definition, nested like the file
 */
function defaults(defs, definition) {
  const result = {}
  for (const [name, property] of Object.entries(definition.properties ?? {})) {
    const resolved = resolve(defs, property)
    if (property.default !== undefined) result[name] = property.default
    else if (resolved.type === 'object' && resolved.properties) result[name] = defaults(defs, resolved)
  }
  return result
}

/**
 * @param {object} descriptor the generated paper-plugin.yml
 * @returns {string} the permissions page
 */
function permissionsPage(descriptor) {
  const lines = [
    '---',
    'title: Permissions',
    'description: Every permission of BloodNight and what it allows.',
    'sidebar_position: 2',
    '---',
    '',
    generatedNote,
    '',
    '# Permissions',
    '',
    'Only operators have these permissions at first. Give them to other players with your permissions plugin.',
    '',
    '| Permission | What it allows | Includes |',
    '|---|---|---|',
  ]
  for (const [name, permission] of Object.entries(descriptor.permissions ?? {}).sort(([a], [b]) => a.localeCompare(b))) {
    const children = Object.keys(permission.children ?? {}).map(child => `\`${child}\``).join(', ')
    lines.push(`| \`${name}\` | ${cell(permission.description)} | ${children} |`)
  }
  lines.push('')
  return lines.join('\n')
}

/**
 * @returns {string[]} every file below a folder, relative to it
 */
function existing(folder) {
  if (!existsSync(folder)) return []
  return readdirSync(folder, {recursive: true, withFileTypes: true})
    .filter(entry => entry.isFile())
    .map(entry => relative(folder, join(entry.parentPath, entry.name)))
}

const missing = Object.entries(sources).filter(([, path]) => !existsSync(path))
if (missing.length) {
  console.error(`Missing ${missing.map(([name, path]) => `${name} (${relative(root, path)})`).join(', ')}.`)
  console.error('Run ./toolchain.sh docs-generate, which builds them first.')
  process.exit(1)
}

const wanted = pages()
if (check) {
  const stale = [
    ...[...wanted].filter(([path, page]) => !existsSync(join(target, path)) || readFileSync(join(target, path), 'utf8') !== page).map(([path]) => path),
    ...existing(target).filter(path => !wanted.has(path)),
  ]
  if (stale.length) {
    console.error(`Reference pages are stale: ${stale.join(', ')}. Run ./toolchain.sh docs-generate.`)
    process.exit(1)
  }
  console.log('Reference pages are up to date.')
} else {
  rmSync(target, {recursive: true, force: true})
  for (const [path, page] of wanted) {
    mkdirSync(dirname(join(target, path)), {recursive: true})
    writeFileSync(join(target, path), page)
  }
  console.log(`Wrote ${wanted.size} reference files.`)
}
