/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {catalog as base} from '~/util/graph/graphFixtures'
import {graphView} from '~/util/graph/graphView'
import {routineNames, withRoutines} from '~/util/graph/routineCalls'

const catalog: Catalog = {
    ...base,
    selectable: {boolean: 'boolean', integer: 'int', living_entity: 'org.bukkit.entity.LivingEntity'},
    types: [{id: 'int', name: 'Integer', raw: 'int', literal: true}, {id: 'org.bukkit.entity.LivingEntity', name: 'Living Entity', raw: 'org.bukkit.entity.LivingEntity'}],
    nodes: [...base.nodes ?? [], {
        id: 'routine.parameter', kind: 'value', category: 'routine', name: 'Parameter', description: '', macroOnly: false,
        config: [
            {id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'boolean', selects: 'T', choices: ['boolean', 'integer', 'living_entity']},
            {id: 'routine', name: 'Routine', type: {id: 'java.lang.String', arguments: []}},
            {id: 'name', name: 'Name', type: {id: 'java.lang.String', arguments: []}},
        ],
    }],
}

const graph: Graph = {nodes: {
    define: {type: 'routine.define', config: {name: 'knockback'}},
    victim: {type: 'routine.parameter', config: {routine: 'knockback', name: 'victim', type: 'living_entity'}},
    power: {type: 'routine.parameter', config: {routine: 'knockback', name: 'power', type: 'integer'}},
    other: {type: 'routine.parameter', config: {routine: 'heal', name: 'amount', type: 'integer'}},
    call: {type: 'routine.knockback'},
}}

describe('withRoutines', () => {
    it('adds a call node per routine with an input per parameter', () => {
        const knockback = withRoutines(catalog, graph).nodes?.find(node => node.id === 'routine.knockback')

        expect(knockback?.name).toBe('Call knockback')
        expect(knockback?.inputs?.map(port => [port.id, port.type?.id, port.literal])).toEqual([
            ['victim', 'org.bukkit.entity.LivingEntity', false],
            ['power', 'int', true],
        ])
        expect(knockback?.executions?.map(port => port.id)).toEqual(['next'])
    })

    it('draws a placed call with its inputs before anything is wired', () => {
        const call = graphView(graph, withRoutines(catalog, graph), []).nodes.find(node => node.id === 'call')

        expect(call?.title).toBe('Call knockback')
        expect(call?.inputs.map(port => port.id)).toEqual(['victim', 'power'])
    })

    it('suggests the defined routines for a parameter', () => {
        const view = graphView(graph, withRoutines(catalog, graph), [])
        const victim = view.nodes.find(node => node.id === 'victim')

        expect(victim?.config.find(field => field.id === 'routine')?.suggestions.map(suggestion => suggestion.value)).toEqual(['heal', 'knockback'])
        expect(routineNames(graph)).toEqual(['knockback'])
    })
})
