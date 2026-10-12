/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {changingSibling, familyOf, variantFor, withVariants} from '~/util/graph/mutate'
import {applyPatch} from '~/util/patch/jsonPatch'

const ENTITY = 'org.bukkit.entity.Entity'
const LIVING = 'org.bukkit.entity.LivingEntity'
const ZOMBIE = 'org.bukkit.entity.Zombie'

function port(id: string, type: string, optional = false) {
    return {id, name: id, description: '', type: {id: type, arguments: []}, optional, literal: type === 'int'}
}

function variant(id: string, kind: string, target: string, extra: number) {
    const setters = kind === 'action' ? Array.from({length: extra}, (_, index) => port(`p${index}`, 'int', true)) : []
    return {
        id, kind, category: 'entity', name: id, description: '', macroOnly: false, family: 'object.mutate',
        inputs: [port('target', target), ...setters],
        outputs: Array.from({length: extra}, (_, index) => port(`p${index}`, 'int')),
        executions: kind === 'action' ? [{id: 'next', name: ''}] : [],
        config: [],
    }
}

const catalog: Catalog = {
    format: 1,
    version: 'test',
    selectable: {},
    bindings: {},
    types: [
        {id: LIVING, name: 'Living Entity', raw: LIVING, arguments: [], literal: false, values: [], assignableTo: [ENTITY]},
        {id: ZOMBIE, name: 'Zombie', raw: ZOMBIE, arguments: [], literal: false, values: [], assignableTo: [LIVING, ENTITY]},
    ],
    nodes: [
        {id: 'object.mutate', kind: 'pure', category: 'object', name: 'Mutate', description: '', macroOnly: false, inputs: [port('target', 'java.lang.Object', true)], outputs: [port('target', 'java.lang.Object')], executions: [], config: []},
        variant('entity.read', 'pure', ENTITY, 2),
        variant('entity.mutate', 'action', ENTITY, 2),
        variant('living_entity.read', 'pure', LIVING, 4),
        variant('living_entity.mutate', 'action', LIVING, 4),
        {id: 'mob.self', kind: 'pure', category: 'mob', name: 'Self', description: '', macroOnly: false, inputs: [], outputs: [port('entity', ZOMBIE)], executions: [], config: []},
        {id: 'mob.tick', kind: 'entry', category: 'entry', name: 'Tick', description: '', macroOnly: false, inputs: [], outputs: [], executions: [{id: 'next', name: ''}], config: []},
    ],
}

describe('mutate variants', () => {
    it('picks the most specific variant the wired type fits, reading unless something changes', () => {
        expect(variantFor(catalog, 'object.mutate', {id: ZOMBIE}, false)).toBe('living_entity.read')
        expect(variantFor(catalog, 'object.mutate', {id: ZOMBIE}, true)).toBe('living_entity.mutate')
        expect(variantFor(catalog, 'object.mutate', {id: ENTITY}, false)).toBe('entity.read')
        expect(variantFor(catalog, 'object.mutate', {id: 'java.lang.String'}, false)).toBeUndefined()
        expect(familyOf(catalog, 'entity.read')).toBe('object.mutate')
        expect(familyOf(catalog, 'object.mutate')).toBe('object.mutate')
        expect(changingSibling(catalog, 'entity.read')?.id).toBe('entity.mutate')
        expect(variantFor(catalog, 'object.mutate', {id: ZOMBIE}, true, {inputs: ['p3'], outputs: []})).toBe('living_entity.mutate')
        expect(variantFor(catalog, 'object.mutate', {id: ZOMBIE}, true, {inputs: ['p9'], outputs: []})).toBeUndefined()
    })

    const graph = (): Graph => ({nodes: {self: {type: 'mob.self'}, tick: {type: 'mob.tick'}, m: {type: 'object.mutate'}}})

    function edit(start: Graph, operations: Parameters<typeof withVariants>[3]): Graph {
        return applyPatch({behaviour: start}, withVariants('/behaviour', start, catalog, operations)).behaviour
    }

    it('turns into the reading variant of what is wired in', () => {
        const wired = edit(graph(), [{op: 'add', path: '/behaviour/nodes/m/inputs', value: {target: {node: 'self', port: 'entity'}}}])
        expect(wired.nodes?.m?.type).toBe('living_entity.read')
    })

    it('changes once a property is set, and reads again without its execution wires when none is', () => {
        const reading = edit(graph(), [{op: 'add', path: '/behaviour/nodes/m/inputs', value: {target: {node: 'self', port: 'entity'}}}])
        const changing = edit(reading, [{op: 'add', path: '/behaviour/nodes/m/literals', value: {p1: 3}}])
        expect(changing.nodes?.m?.type).toBe('living_entity.mutate')

        const chained = applyPatch({behaviour: changing}, [
            {op: 'add', path: '/behaviour/nodes/tick/executions', value: {next: ['m']}},
            {op: 'add', path: '/behaviour/nodes/m/executions', value: {next: ['tick']}},
        ]).behaviour
        const back = edit(chained, [{op: 'remove', path: '/behaviour/nodes/m/literals'}])
        expect(back.nodes?.m?.type).toBe('living_entity.read')
        expect(back.nodes?.m?.executions).toBeUndefined()
        expect(back.nodes?.tick?.executions?.next).toEqual([])
    })
})
