/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {editorPlace} from '~/util/editorPlace'

/**
 * Where the editor is, kept while it switches areas, and the ways to go somewhere else in it, e.g. from a mob's loot to
 * one of its tables.
 */
export function useEditorNavigation() {
    const place = editorPlace()
    const selection = useWorkspaceSelection()

    return {
        place,
        /** Opens a loot table in the loot area. */
        openLootTable(id: string): void {
            place.value = {...place.value, area: 'loot', lootTable: id}
        },
        /** Opens a particle effect in the particles area. */
        openEffect(id: string): void {
            place.value = {...place.value, area: 'particles', particleEffect: id}
        },
        /** Opens a mob in the mobs area. */
        openMob(id: string): void {
            selection.value.mobs = id
            place.value = {...place.value, area: 'mobs'}
        },
        /** Opens a tab of a world's settings. */
        openWorld(uid: string, tab: string): void {
            place.value = {...place.value, area: 'settings', settings: 'world', world: uid, worldTab: tab}
        },
        /** Opens a tab of the server settings. */
        openServer(tab: string): void {
            place.value = {...place.value, area: 'settings', settings: 'server', serverTab: tab}
        },
    }
}
