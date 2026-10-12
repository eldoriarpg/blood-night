/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {catalog, graph} from '~/util/graph/graphFixtures'
import {accepts, nodeMatches, pulledPin} from '~/util/graph/nodeMatches'
import {GraphChanges} from '~/util/graph/graphChanges'
import {applyPatch} from '~/util/patch/jsonPatch'

const ZOMBIE = {id: 'org.bukkit.entity.Zombie', arguments: []}
const LIVING = {id: 'org.bukkit.entity.LivingEntity', arguments: []}
const typed: Catalog = {
    ...catalog,
    types: [
        {id: ZOMBIE.id, name: 'Zombie', raw: ZOMBIE.id, arguments: [], literal: false, values: [], assignableTo: [LIVING.id, 'java.util.List<org.bukkit.entity.Zombie>']},
        {id: LIVING.id, name: 'LivingEntity', raw: LIVING.id, arguments: [], literal: false, values: [], assignableTo: []},
    ],
    nodes: [
        ...catalog.nodes,
        {
            id: 'logic.log', kind: 'action', category: 'debug', name: 'Log', description: '', macroOnly: false,
            inputs: [{id: 'value', name: 'value', description: '', type: {variable: 'T', arguments: []}, optional: false, literal: false}],
            outputs: [], executions: [{id: 'next', name: '', repeating: false}], config: [],
        },
        {
            id: 'zombie.baby', kind: 'pure', category: 'value', name: 'Is baby', description: '', macroOnly: true,
            inputs: [{id: 'zombie', name: 'zombie', description: '', type: ZOMBIE, optional: false, literal: false}],
            outputs: [], executions: [], config: [],
        },
    ],
}

describe('accepts', () => {
    it('follows the catalog\'s assignability and lets type variables take anything', () => {
        expect(accepts(typed, LIVING, ZOMBIE)).toBe(true)
        expect(accepts(typed, ZOMBIE, LIVING)).toBe(false)
        expect(accepts(typed, {variable: 'T', arguments: []}, LIVING)).toBe(true)
        expect(accepts(typed, {raw: 'java.util.List', arguments: [{variable: 'E', arguments: []}]}, ZOMBIE)).toBe(true)
        expect(accepts(typed, {raw: 'java.util.Set', arguments: [{variable: 'E', arguments: []}]}, ZOMBIE)).toBe(false)
        expect(accepts(typed, {variable: 'N', numeric: true, arguments: []}, {id: 'int', arguments: []})).toBe(true)
        expect(accepts(typed, {variable: 'N', numeric: true, arguments: []}, ZOMBIE)).toBe(false)
    })
})

describe('nodeMatches', () => {
    it('offers nodes taking a pulled output, typed before wildcard', () => {
        const pin = pulledPin(graph(), typed, 'target', 'out:entity')
        expect(nodeMatches(typed, pin, false).map(match => [match.type, match.port, match.wildcard])).toEqual([
            ['entity.is_near', 'in:target', false],
            ['logic.log', 'in:value', true],
        ])
    })

    it('offers nodes feeding a pulled input', () => {
        const pin = pulledPin(graph(), typed, 'near', 'in:target')
        expect(nodeMatches(typed, pin, false).map(match => [match.type, match.port])).toEqual([['mob.target', 'out:entity']])
    })

    it('offers macro only nodes in macros only', () => {
        const zombie: Graph = {nodes: {z: {type: 'zombie.baby'}}}
        const pin = {node: 'z', handle: 'out:x', type: ZOMBIE}
        expect(nodeMatches(typed, pin, false).map(match => match.type)).toEqual(['entity.is_near', 'logic.log'])
        expect(nodeMatches(typed, pin, true).map(match => match.type)).toContain('zombie.baby')
        expect(pulledPin(zombie, typed, 'z', 'in:zombie').type).toEqual(ZOMBIE)
    })

    it('offers nodes by execution', () => {
        expect(nodeMatches(typed, pulledPin(graph(), typed, 'ignite', 'exec:next'), false).map(match => match.type))
            .toEqual(['entity.is_near', 'creeper.ignite', 'logic.log'])
        expect(nodeMatches(typed, pulledPin(graph(), typed, 'ignite', 'exec-in'), false).map(match => [match.type, match.port]))
            .toEqual([['mob.tick', 'exec:next'], ['entity.is_near', 'exec:then'], ['creeper.ignite', 'exec:next'], ['logic.log', 'exec:next']])
    })
})

describe('type picks', () => {
    const cast = {
        id: 'cast', kind: 'pure', category: 'logic', name: 'As', description: '', macroOnly: false,
        config: [{id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'entity', selects: 'T', choices: ['entity', 'living_entity', 'zombie']}],
        inputs: [{id: 'value', name: 'value', description: '', type: {id: 'java.lang.Object', arguments: []}, optional: false, literal: false}],
        outputs: [{id: 'result', name: 'result', description: '', type: {variable: 'T', arguments: []}, optional: false, literal: false}],
        executions: [],
    }
    const picking: Catalog = {
        ...typed,
        selectable: {entity: 'org.bukkit.entity.Entity', living_entity: LIVING.id, zombie: ZOMBIE.id},
        nodes: [...typed.nodes, cast],
    }

    it('presets the type that fits the pulled input', () => {
        const match = nodeMatches(picking, pulledPin(graph(), picking, 'near', 'in:target'), false).find(candidate => candidate.type === 'cast')
        expect(match).toMatchObject({port: 'out:result', wildcard: false, config: {type: 'living_entity'}})
    })

    it('adds the node with its preset', () => {
        const document = {behaviour: graph()}
        const {operations} = new GraphChanges('/behaviour', document.behaviour)
            .addWired('cast', {x: 0, y: 0}, {node: 'near', handle: 'in:target'}, 'out:result', {type: 'living_entity'})
        expect(applyPatch(document, operations).behaviour.nodes?.cast_1?.config).toEqual({type: 'living_entity'})
    })
})

describe('addWired', () => {
    const place = {x: 10, y: 20}

    function wired(handle: string, node: string, type: string, port: string): Graph {
        const document = {behaviour: graph()}
        const {operations} = new GraphChanges('/behaviour', document.behaviour).addWired(type, place, {node, handle}, port)
        return applyPatch(document, operations).behaviour
    }

    it('wires a node added at a pulled output or execution input into itself', () => {
        expect(wired('out:entity', 'target', 'entity.is_near', 'in:target').nodes?.is_near_1?.inputs).toEqual({target: {node: 'target', port: 'entity'}})
        expect(wired('exec-in', 'ignite', 'mob.tick', 'exec:next').nodes?.tick_1?.executions).toEqual({next: ['ignite']})
    })

    it('wires a node added at a pulled input or execution output into the pulled node', () => {
        expect(wired('in:target', 'near', 'mob.target', 'out:entity').nodes?.near?.inputs?.target).toEqual({node: 'target_1', port: 'entity'})
        expect(wired('exec:else', 'near', 'creeper.ignite', 'exec-in').nodes?.near?.executions?.else).toEqual(['ignite_1'])
    })
})
