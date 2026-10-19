/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorArea} from '~/views/editorview/editorArea'

/** Where the editor is: the area, the loot table, the particle effect, the settings scope, world and tabs shown. */
export interface EditorPlace {
    area: EditorArea
    lootTable: string
    particleEffect: string
    settings: 'world' | 'server'
    world: string
    worldTab: string
    serverTab: string
}

/** Where the editor is, kept while it switches areas. */
export function editorPlace() {
    return useState<EditorPlace>('editorPlace', () => ({area: 'mobs', lootTable: '', particleEffect: '', settings: 'world', world: '', worldTab: '', serverTab: ''}))
}
