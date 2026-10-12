/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import {catalog, graph} from '~/util/graph/graphFixtures'
import {GraphChanges, freshId} from '~/util/graph/graphChanges'
import type {NodeType} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {graphView, handles, isGrouped, portGroups, type NodeView, type PortView} from '~/util/graph/graphView'
import {layout} from '~/util/graph/graphLayout'
import {applyPatch} from '~/util/patch/jsonPatch'

describe('graphView', () => {
    it('types pins and titles by the picked type', () => {
        const picking = {
            ...catalog,
            selectable: {living_entity: 'org.bukkit.entity.LivingEntity'},
            types: [{id: 'org.bukkit.entity.LivingEntity', name: 'Living Entity', raw: 'org.bukkit.entity.LivingEntity', arguments: [], literal: false, values: [], assignableTo: []}],
            nodes: [...catalog.nodes, {
                id: 'cast', kind: 'pure', category: 'logic', name: 'As', description: '', macroOnly: false, executions: [],
                config: [{id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'living_entity', selects: 'T', choices: ['living_entity']}],
                inputs: [], outputs: [{id: 'result', name: 'result', description: '', type: {variable: 'T', arguments: []}, optional: false, literal: false}],
            }],
        }
        const {nodes} = graphView({nodes: {as: {type: 'cast'}}}, picking, [])
        expect(nodes[0]?.title).toBe('As: Living Entity')
        expect(nodes[0]?.outputs[0]).toMatchObject({pin: 'entity', typeName: 'Living Entity'})
    })

    it('names the type and documentation of each port', () => {
        const documented = {
            ...catalog,
            types: [{id: 'org.bukkit.entity.LivingEntity', name: 'Living Entity', raw: 'org.bukkit.entity.LivingEntity', arguments: [], literal: false, values: [], assignableTo: []}],
            nodes: catalog.nodes.map(node => node.id === 'mob.target'
                ? {...node, outputs: node.outputs?.map(port => ({...port, description: 'The entity the mob is after.'}))}
                : node),
        }
        const {nodes} = graphView(graph(), documented, [])
        expect(nodes.find(node => node.id === 'target')?.outputs[0]).toMatchObject({typeName: 'Living Entity', description: 'The entity the mob is after.'})
        expect(nodes.find(node => node.id === 'near')?.inputs.map(port => port.typeName)).toEqual(['Living Entity', 'double'])
        expect(nodes.find(node => node.id === 'near')?.inputs.map(port => port.optional)).toEqual([false, false])
    })

    it('draws nodes with their pins and wires', () => {
        const {nodes, edges} = graphView(graph(), catalog, [])
        const near = nodes.find(node => node.id === 'near')
        expect(near?.tone).toBe('flow')
        expect(near?.execIn).toBe(true)
        expect(near?.inputs.map(port => [port.id, port.pin, port.connected, port.literal])).toEqual([
            ['target', 'entity', true, undefined],
            ['distance', 'number', false, 4],
        ])
        expect(nodes.find(node => node.id === 'tick')?.execIn).toBe(false)
        expect(nodes.find(node => node.id === 'target')?.outputs[0]?.connected).toBe(true)
        expect(edges).toContainEqual(expect.objectContaining({source: 'target', sourceHandle: handles.output('entity'), target: 'near', targetHandle: handles.input('target'), pin: 'entity'}))
        expect(edges).toContainEqual(expect.objectContaining({source: 'near', sourceHandle: handles.execOut('then'), target: 'ignite', targetHandle: handles.execIn, pin: 'exec'}))
    })

    it('draws unknown nodes from their instance', () => {
        const {nodes} = graphView({nodes: {use: {type: 'macro.burst', inputs: {at: {node: 'x', port: 'y'}}, executions: {next: []}}}}, catalog, [{id: 'burst', name: 'Smoke burst', description: '', category: 'macro', body: {nodes: {}}}])
        expect(nodes[0]).toMatchObject({title: 'Smoke burst', tone: 'macro', known: false})
        expect(nodes[0]?.inputs.map(port => port.id)).toEqual(['at'])
        expect(nodes[0]?.executions.map(port => port.id)).toEqual(['next'])
    })
})

describe('layout', () => {
    it('keeps stored positions and orders the rest left to right', () => {
        const positions = layout(graph())
        expect(positions.get('ignite')).toEqual({x: 900, y: 40})
        expect(positions.get('tick')?.x).toBeLessThan(positions.get('near')?.x ?? 0)
        expect(positions.get('target')?.x).toBeLessThan(positions.get('near')?.x ?? 0)
    })
})

describe('GraphChanges', () => {
    const base = '/mobs/m/behaviour'
    const changed = (operations: ReturnType<GraphChanges['move']>) => applyPatch({mobs: {m: {behaviour: graph()}}}, operations).mobs.m.behaviour

    it('moves, connects and types values', () => {
        const changes = new GraphChanges(base, graph())
        expect(changed(changes.move('tick', {x: 10.4, y: 20})).nodes.tick?.editor).toEqual({x: 10, y: 20})
        expect(changed(changes.connectData('near', 'distance', 'target', 'entity')).nodes.near).toMatchObject({inputs: {distance: {node: 'target', port: 'entity'}}, literals: {}})
        expect(changed(changes.connectExecution('ignite', 'next', 'tick')).nodes.ignite?.executions).toEqual({next: ['tick']})
        expect(changed(changes.connectExecution('near', 'then', 'tick')).nodes.near?.executions).toEqual({then: ['ignite', 'tick']})
        expect(changed(changes.literal('ignite', 'fuse', 10)).nodes.ignite?.literals).toEqual({fuse: 10})
        expect(changed(changes.literal('near', 'distance', undefined)).nodes.near?.literals).toEqual({})
    })

    it('removes nodes with every wire to them', () => {
        const result = changed(new GraphChanges(base, graph()).remove(['near', 'target']))
        expect(Object.keys(result.nodes)).toEqual(['tick', 'ignite'])
        expect(result.nodes.tick?.executions).toEqual({next: []})
    })

    it('adds nodes with fresh ids', () => {
        const {id, operations} = new GraphChanges(base, graph()).add('creeper.ignite', {x: 1, y: 2})
        expect(id).toBe('ignite_1')
        expect(changed(operations).nodes.ignite_1).toEqual({type: 'creeper.ignite', editor: {x: 1, y: 2}})
        expect(freshId('a.b', ['b_1', 'b_2'])).toBe('b_3')
    })
})

describe('portGroups', () => {
    const port = (id: string, group?: string): PortView => ({
        id, name: id, description: '', typeName: '', optional: false, pin: 'other', connected: false, literal: undefined,
        literalAllowed: false, options: [], typeId: '', format: undefined, group,
    })

    it('runs ports of the same group together under its name', () => {
        const ports = [port('fuse', 'Creeper'), port('powered', 'Creeper'), port('aware', 'Mob'), port('x', 'Entity')]

        expect(portGroups(ports, true).map(group => [group.name, group.ports.map(each => each.id)])).toEqual([
            ['Creeper', ['fuse', 'powered']],
            ['Mob', ['aware']],
            ['Entity', ['x']],
        ])
    })

    it('draws no headers for a node with fewer than two groups', () => {
        const ports = [port('target'), port('x', 'Entity')]
        const node = {inputs: [ports[0]!], outputs: [ports[1]!]} as NodeView

        expect(isGrouped(node)).toBe(false)
        expect(portGroups(ports, false)).toEqual([{name: undefined, ports}])
    })
})

describe('graphView settings', () => {
    const selectable = {boolean: 'boolean', integer: 'int', text: 'java.lang.String'}
    const select: NodeType = {
        id: 'select', kind: 'pure', category: 'logic', name: 'Select', description: '', macroOnly: false,
        config: [
            {id: 'key_type', name: 'Key Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'integer', selects: 'K', choices: ['boolean', 'integer']},
            {id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'integer', selects: 'T', choices: ['integer', 'text']},
            ...[0, 1, 2].map(i => ({id: `key_${i}`, name: `Key ${i + 1}`, type: {variable: 'K', arguments: []}, defaultValue: null, beside: `option_${i}`})),
        ],
        inputs: [
            {id: 'key', name: 'Key', type: {variable: 'K', arguments: []}, optional: false, literal: false},
            ...[0, 1, 2].map(i => ({id: `option_${i}`, name: `Option ${i + 1}`, type: {variable: 'T', arguments: []}, optional: true, literal: false})),
            {id: 'default', name: 'Default', type: {variable: 'T', arguments: []}, optional: true, literal: false},
        ],
        outputs: [{id: 'value', name: 'Value', type: {variable: 'T', arguments: []}}],
    }
    const variable: NodeType = {
        id: 'variable.get', kind: 'pure', category: 'variable', name: 'Get Variable', description: '', macroOnly: false,
        config: [{id: 'name', name: 'Name', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'variable'}],
    }
    const setter: NodeType = {...variable, id: 'variable.set', name: 'Set Variable'}
    const withNodes = {...catalog, selectable, nodes: [...catalog.nodes, select, variable, setter]}

    it('draws a select keyed by a boolean as true and false only', () => {
        const view = graphView({nodes: {pick: {type: 'select', config: {key_type: 'boolean'}}}}, withNodes, [])
        const pick = view.nodes.find(node => node.id === 'pick')!

        expect(pick.inputs.map(port => port.name)).toEqual(['Key', 'True', 'False'])
        expect(pick.config.map(field => field.id)).toEqual(['key_type', 'type'])
    })

    it('keeps the keys of a select keyed by anything else beside their options', () => {
        const view = graphView({nodes: {pick: {type: 'select'}}}, withNodes, [])
        const pick = view.nodes.find(node => node.id === 'pick')!

        expect(pick.config.filter(field => field.beside).map(field => [field.id, field.beside])).toEqual([
            ['key_0', 'option_0'], ['key_1', 'option_1'], ['key_2', 'option_2'],
        ])
    })

    it('suggests the names related nodes already use', () => {
        const view = graphView({nodes: {
            a: {type: 'variable.set', config: {name: 'hits'}},
            b: {type: 'variable.get', config: {name: 'mark'}},
            c: {type: 'variable.get'},
        }}, withNodes, [])
        const c = view.nodes.find(node => node.id === 'c')!

        expect(c.config[0]!.suggestions).toEqual([{value: 'hits', hint: ''}, {value: 'mark', hint: ''}])
    })
})

describe('removing execution wires', () => {
    const graph: Graph = {nodes: {
        tick: {type: 'mob.tick', executions: {next: ['a', 'b', 'c']}},
        a: {type: 'x'},
        b: {type: 'x'},
        c: {type: 'x'},
    }}

    it('removes several wires of one output from the last one back', () => {
        expect(new GraphChanges('/g', graph).disconnectExecutions([
            {source: 'tick', port: 'next', position: 0},
            {source: 'tick', port: 'next', position: 2},
        ])).toEqual([
            {op: 'remove', path: '/g/nodes/tick/executions/next/2'},
            {op: 'remove', path: '/g/nodes/tick/executions/next/0'},
        ])
    })

    it('orders the wires of a removed node with the wires removed beside it', () => {
        expect(new GraphChanges('/g', graph).remove(['c'], [{source: 'tick', port: 'next', position: 0}])).toEqual([
            {op: 'remove', path: '/g/nodes/tick/executions/next/2'},
            {op: 'remove', path: '/g/nodes/tick/executions/next/0'},
            {op: 'remove', path: '/g/nodes/c'},
        ])
    })
})
