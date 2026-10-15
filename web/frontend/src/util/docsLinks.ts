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
