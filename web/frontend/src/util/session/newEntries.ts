/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {CustomMob, EditorPayload, EntityType, Macro} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import type {PatchOperation} from '~/util/patch/jsonPatch'

const ID = /^[a-z0-9_]+$/

/**
 * Why an id cannot name a new mob or macro, or null when it can: ids are lower case letters, digits and underscores,
 * and unique among their kind.
 *
 * @param id the id
 * @param taken the ids in use
 * @returns the i18n key of the reason, or null
 */
export function idProblem(id: string, taken: readonly string[]): string | null {
    if (!ID.test(id)) return 'editor.create.badId'
    if (taken.includes(id)) return 'editor.create.takenId'
    return null
}

/**
 * A new mob as the plugin reads one: no behaviour yet, the entity's own health and damage, spawning by the default
 * rules during blood nights.
 *
 * @param id the mob's id
 * @param entityType the entity it takes over
 * @returns the mob
 */
export function newMob(id: string, entityType: EntityType): CustomMob {
    return {
        id,
        attributes: {displayName: '', entityType, exactType: false, healthModifier: 'DEFAULT', health: 1, damageModifier: 'DEFAULT', damage: 1, changes: []},
        behaviour: {nodes: {}},
        extension: {type: 'NONE'},
        spawn: {weight: 10, nightOnly: true, despawn: 'NIGHT_END', reasons: [], conditions: {worlds: [], biomes: [], surface: 'ANY'}},
    } as unknown as CustomMob
}

/**
 * @param id the macro's id
 * @param name its name in the editor
 * @returns a macro with an empty body
 */
export function newMacro(id: string, name: string): Macro {
    return {id, name: name || id, description: '', category: 'macro', body: {nodes: {}}}
}

/**
 * @param payload the payload
 * @param mob the new mob
 * @returns the operations adding it
 */
export function addMob(payload: EditorPayload, mob: CustomMob): PatchOperation[] {
    const operations: PatchOperation[] = payload.mobs ? [] : [{op: 'add', path: '/mobs', value: {}}]
    return [...operations, {op: 'add', path: pointer('mobs', mob.id), value: mob}]
}

/**
 * @param payload the payload
 * @param macro the new macro
 * @returns the operations adding it
 */
export function addMacro(payload: EditorPayload, macro: Macro): PatchOperation[] {
    const operations: PatchOperation[] = payload.macros ? [] : [{op: 'add', path: '/macros', value: []}]
    return [...operations, {op: 'add', path: '/macros/-', value: macro}]
}
