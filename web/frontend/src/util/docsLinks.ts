/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorArea} from '~/views/editorview/editorArea'

/** Where BloodNight's documentation is published. */
export const DOCS_URL = 'https://bloodnight.docs.eldoria.de'

const PAGES: Record<EditorArea, string> = {
    mobs: '/mobs/custom-mobs',
    macros: '/editor/macros-and-routines',
    items: '/mobs/drops-and-items#items',
    settings: '/reference/settings',
    revisions: '/editor/sessions#revisions',
}

/**
 * @returns the documentation page about an area of the editor
 */
export function docsLink(area: EditorArea): string {
    return DOCS_URL + PAGES[area]
}

/** The documentation page and part about each tab of a world's settings, by the tab's key. */
const WORLD_SECTION_PAGES: Readonly<Record<string, string>> = {
    general: '/nights/choosing-nights#turn-blood-nights-off-in-a-world',
    night: '/nights/night-times',
    selection: '/nights/choosing-nights',
    mobs: '/mobs/spawning#world-settings',
    announcements: '/nights/announcements',
    sounds: '/nights/announcements#sounds',
    deathPlayers: '/rules/death-actions#only-for-players',
    deathMobs: '/rules/death-actions',
    drops: '/mobs/drops-and-items#how-many-drops',
    atmosphere: '/nights/atmosphere',
    rules: '/rules/world-rules',
}

/** The documentation page and part about each tab of the server settings, by the tab's key. */
const SERVER_SECTION_PAGES: Readonly<Record<string, string>> = {
    messages: '/nights/announcements#messages',
    editor: '/getting-started/editor-service',
    resourcePack: '/nights/atmosphere#the-resource-pack',
    plugins: '/placeholders',
    updates: '/reference/settings',
    drops: '/mobs/drops-and-items#drop-tables',
}

/** Other places of the editor the documentation explains. */
export const DOCS_PAGES = {
    problems: '/editor/finding-problems',
    placeholders: '/placeholders',
} as const

/**
 * @param key a settings tab's key, e.g. `night`
 * @param server whether it is a tab of the server settings rather than a world's
 * @returns the documentation about it, none where there is no page for it
 */
export function sectionDocsLink(key: string, server: boolean): string | undefined {
    const page = (server ? SERVER_SECTION_PAGES : WORLD_SECTION_PAGES)[key]
    return page === undefined ? undefined : DOCS_URL + page
}

/**
 * @param category a node's category, e.g. `living_entity`
 * @param name the node's name, e.g. `Do Once`
 * @returns the node's entry in the node reference
 */
export function nodeDocsLink(category: string, name: string): string {
    return `${DOCS_URL}/reference/nodes/${category.replaceAll('_', '-')}#${headingSlug(name)}`
}

/**
 * @returns the anchor the documentation gives a heading, e.g. `how-many-drops` for "How many drops?"
 */
export function headingSlug(heading: string): string {
    return heading.trim().toLowerCase().replaceAll(/[^\p{L}\p{N}\s_-]/gu, '').replaceAll(/\s/g, '-')
}

/** Every page and part the editor links to, for checking they exist. */
export const LINKED_PAGES: readonly string[] = [...Object.values(WORLD_SECTION_PAGES), ...Object.values(SERVER_SECTION_PAGES), ...Object.values(DOCS_PAGES)]
