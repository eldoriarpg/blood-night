/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {channelsEdit, channelsHex, configEdit} from '~/util/graph/nodeEdits'
import {catalog as base} from '~/util/graph/graphFixtures'

const nameField = {id: 'name', name: 'Name', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'variable'}
const variableType = {id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'boolean', selects: 'T', choices: ['boolean', 'integer', 'location']}

const catalog: Catalog = {
    ...base,
    nodes: [{
        id: 'variable.get', kind: 'pure', category: 'variable', name: 'Get Variable', description: '', macroOnly: false,
        config: [nameField, variableType],
        outputs: [{id: 'value', name: 'Value', type: {variable: 'T', arguments: []}}],
    }, {
        id: 'variable.set', kind: 'action', category: 'variable', name: 'Set Variable', description: '', macroOnly: false,
        config: [nameField, variableType],
        inputs: [{id: 'value', name: 'Value', type: {variable: 'T', arguments: []}, optional: false, literal: false}],
    }, {
        id: 'value', kind: 'value', category: 'value', name: 'Value', description: '', macroOnly: false,
        config: [
            {id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'boolean', selects: 'T', choices: ['boolean', 'integer']},
            {id: 'value', name: 'Value', type: {variable: 'T', arguments: []}},
        ],
    }],
}

describe('configEdit', () => {
    it('clears the value a new type pick no longer fits', () => {
        const graph: Graph = {nodes: {v: {type: 'value', config: {type: 'boolean', value: true}}}}

        expect(configEdit('/g', graph, catalog, 'v', 'type', 'integer')).toEqual([
            {op: 'add', path: '/g/nodes/v/config/type', value: 'integer'},
            {op: 'remove', path: '/g/nodes/v/config/value'},
        ])
    })

    it('keeps other fields when a plain value changes', () => {
        const graph: Graph = {nodes: {v: {type: 'value', config: {type: 'integer'}}}}

        expect(configEdit('/g', graph, catalog, 'v', 'value', 3)).toEqual([{op: 'add', path: '/g/nodes/v/config/value', value: 3}])
    })
})

describe('channels', () => {
    it('types a colour into red, green and blue', () => {
        const graph: Graph = {nodes: {c: {type: 'color.rgb'}}}

        expect(channelsEdit('/g', graph, 'c', '#ff8000')).toEqual([
            {op: 'add', path: '/g/nodes/c/literals', value: {}},
            {op: 'add', path: '/g/nodes/c/literals/red', value: 255},
            {op: 'add', path: '/g/nodes/c/literals/green', value: 128},
            {op: 'add', path: '/g/nodes/c/literals/blue', value: 0},
        ])
    })

    it('reads the channels back as a colour, clamping and filling missing ones', () => {
        expect(channelsHex({red: 300, green: 16})).toBe('#ff1000')
    })
})

describe('variables', () => {
    const graph: Graph = {nodes: {
        mark: {type: 'variable.set', config: {name: 'mark', type: 'location'}},
        read: {type: 'variable.get'},
        count: {type: 'variable.set', config: {name: 'hits', type: 'integer'}, literals: {value: 3}},
    }}

    it('takes over the type of the variable a name picks', () => {
        expect(configEdit('/g', graph, catalog, 'read', 'name', 'mark')).toEqual([
            {op: 'add', path: '/g/nodes/read/config', value: {}},
            {op: 'add', path: '/g/nodes/read/config/name', value: 'mark'},
            {op: 'add', path: '/g/nodes/read/config/type', value: 'location'},
        ])
    })

    it('keeps its type for a new name', () => {
        expect(configEdit('/g', graph, catalog, 'read', 'name', 'fresh')).toEqual([
            {op: 'add', path: '/g/nodes/read/config', value: {}},
            {op: 'add', path: '/g/nodes/read/config/name', value: 'fresh'},
        ])
    })

    it('drops a typed-in value the new type no longer fits', () => {
        expect(configEdit('/g', graph, catalog, 'count', 'name', 'mark')).toEqual([
            {op: 'add', path: '/g/nodes/count/config/name', value: 'mark'},
            {op: 'add', path: '/g/nodes/count/config/type', value: 'location'},
            {op: 'remove', path: '/g/nodes/count/literals/value'},
        ])
    })
})
