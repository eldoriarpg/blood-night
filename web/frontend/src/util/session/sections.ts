/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {applyPatch, type PatchOperation} from '~/util/patch/jsonPatch'
import {PatchError, segments} from '~/util/patch/jsonPointer'

/** The sections a change touches, as the editor service decides them before taking their locks. */
export interface Touched {
    sections: Set<string>
    everything: boolean
}

const READ_ONLY: ReadonlySet<string> = new Set(['choices', 'hashes', 'catalog', 'problems', 'macroProblems', 'configProblems', 'store', 'setup', 'legacy', 'builtInEffects'])

/** Section names, as the service and the locks name them. */
export const section = {
    mob: (id: string) => `mobs/${id}`,
    item: (id: string) => `items/${id}`,
    lootTable: (id: string) => `lootTables/${id}`,
    particleEffect: (id: string) => `particleEffects/${id}`,
    macro: (id: string) => `macros/${id}`,
    main: 'settings/main',
    world: (uid: string) => `settings/worlds/${uid}`,
}

/**
 * The sections of the payload a change touches; mirrors the service's rules, so the editor can tell before sending
 * that a change needs a section someone else holds.
 *
 * @param document the payload before the change
 * @param operations the change
 * @returns the touched sections
 * @throws PatchError when a change targets something read only or does not fit
 */
export function sectionsOf(document: unknown, operations: readonly PatchOperation[]): Touched {
    const touched: Touched = {sections: new Set(), everything: false}
    let current = document
    for (const operation of operations) {
        const targets = [operation.path, ...(operation.op === 'move' ? [operation.from] : [])]
        for (const target of targets) {
            const name = sectionAt(current, segments(target), operation)
            if (name === undefined) touched.everything = true
            else touched.sections.add(name)
        }
        current = applyPatch(current, [operation])
    }
    return touched
}

function sectionAt(document: unknown, path: string[], operation: PatchOperation): string | undefined {
    const [root, second, third] = path
    if (root === undefined) return undefined
    if (READ_ONLY.has(root)) throw new PatchError(`${root} cannot be changed in the editor`)
    if (root === 'mobs') return second === undefined ? undefined : section.mob(second)
    if (root === 'items') return second === undefined ? undefined : section.item(second)
    if (root === 'lootTables') return second === undefined ? undefined : section.lootTable(second)
    if (root === 'particleEffects') return second === undefined ? undefined : section.particleEffect(second)
    if (root === 'macros') return second === undefined ? undefined : section.macro(macroId(document, path, operation))
    if (root === 'settings') {
        if (second === 'main') return section.main
        if (second === 'worlds') return third === undefined ? undefined : section.world(third)
        return undefined
    }
    throw new PatchError(`Unknown part ${root}`)
}

function macroId(document: unknown, path: string[], operation: PatchOperation): string {
    const added = path.length === 2 && operation.op === 'add'
    const macros = (document as {macros?: {id?: string}[]}).macros ?? []
    const macro = added ? operation.value as {id?: string} : macros[Number(path[1])]
    if (!macro?.id) throw new PatchError('The macro has no id')
    return macro.id
}
