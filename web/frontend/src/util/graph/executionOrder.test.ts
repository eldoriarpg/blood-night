/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {executionOrder} from '~/util/graph/executionOrder'

const port = (id: string) => ({id, name: id, type: {id: 'int', arguments: []}})
const catalog: Catalog = {
    nodes: [
        {id: 'mob.tick', kind: 'entry', category: '', name: '', description: '', macroOnly: false, executions: [{id: 'next', name: ''}]},
        {id: 'flow.branch', kind: 'control_flow', category: '', name: '', description: '', macroOnly: false, inputs: [port('condition')], executions: [{id: 'true', name: ''}, {id: 'false', name: ''}, {id: 'next', name: ''}]},
        {id: 'act', kind: 'action', category: '', name: '', description: '', macroOnly: false, inputs: [port('first'), port('second')], executions: [{id: 'next', name: ''}]},
        {id: 'pure', kind: 'pure', category: '', name: '', description: '', macroOnly: false, inputs: [port('value')]},
        {id: 'routine.define', kind: 'entry', category: '', name: '', description: '', macroOnly: false, executions: [{id: 'next', name: ''}]},
        {id: 'routine.jump', kind: 'action', category: '', name: '', description: '', macroOnly: false, executions: [{id: 'next', name: ''}]},
        {id: 'flow.delay', kind: 'action', category: '', name: '', description: '', macroOnly: false, inputs: [port('duration')], executions: [{id: 'next', name: ''}]},
    ],
} as Catalog

const order = (graph: Graph, start = 'tick') => executionOrder(graph, catalog, start).map(step => step.node)

describe('executionOrder', () => {
    it('computes inputs before what reads them, each once', () => {
        const graph: Graph = {nodes: {
            tick: {type: 'mob.tick', executions: {next: ['a', 'b']}},
            a: {type: 'act', inputs: {second: {node: 'y', port: 'result'}, first: {node: 'x', port: 'result'}}},
            b: {type: 'act', inputs: {first: {node: 'x', port: 'result'}}},
            x: {type: 'pure', inputs: {value: {node: 'z', port: 'result'}}},
            y: {type: 'pure'},
            z: {type: 'pure'},
        }}

        expect(order(graph)).toEqual(['tick', 'z', 'x', 'y', 'a', 'b'])
    })

    it('shows every arm of a branch in the order the branch declares them', () => {
        const graph: Graph = {nodes: {
            tick: {type: 'mob.tick', executions: {next: ['branch']}},
            branch: {type: 'flow.branch', executions: {next: ['after'], false: ['no'], true: ['yes']}},
            yes: {type: 'act'},
            no: {type: 'act'},
            after: {type: 'act'},
        }}

        expect(order(graph)).toEqual(['tick', 'branch', 'yes', 'no', 'after'])
    })

    it('names the wire each node was reached through', () => {
        const graph: Graph = {nodes: {
            tick: {type: 'mob.tick', executions: {next: ['a']}},
            a: {type: 'act', inputs: {first: {node: 'x', port: 'result'}}},
            x: {type: 'pure'},
        }}

        expect(executionOrder(graph, catalog, 'tick')).toEqual([
            {node: 'tick', wire: undefined, at: 0},
            {node: 'x', wire: 'data:a:first', at: 350},
            {node: 'a', wire: 'exec:tick:next:0', at: 700},
        ])
    })

    it('runs a called routine before going on', () => {
        const graph: Graph = {nodes: {
            tick: {type: 'mob.tick', executions: {next: ['call', 'after']}},
            call: {type: 'routine.jump'},
            define: {type: 'routine.define', config: {name: 'jump'}, executions: {next: ['inside']}},
            inside: {type: 'act'},
            after: {type: 'act'},
        }}

        expect(order(graph)).toEqual(['tick', 'call', 'define', 'inside', 'after'])
    })

    it('holds back what follows a delay while the rest goes on', () => {
        const graph: Graph = {nodes: {
            tick: {type: 'mob.tick', executions: {next: ['wait', 'meanwhile']}},
            wait: {type: 'flow.delay', literals: {duration: 1.5}, executions: {next: ['later']}},
            later: {type: 'act'},
            meanwhile: {type: 'act'},
        }}

        expect(executionOrder(graph, catalog, 'tick').map(step => [step.node, step.at])).toEqual([
            ['tick', 0], ['wait', 350], ['meanwhile', 700], ['later', 2200],
        ])
    })

    it('takes a delay in its unit and caps long ones', () => {
        const at = (literals: Record<string, unknown>, config: Record<string, unknown> = {}) => executionOrder({nodes: {
            tick: {type: 'mob.tick', executions: {next: ['wait']}},
            wait: {type: 'flow.delay', literals, config, executions: {next: ['later']}},
            later: {type: 'act'},
        }}, catalog, 'tick').find(step => step.node === 'later')?.at

        expect(at({duration: 20}, {unit: 'TICKS'})).toBe(700 + 1000)
        expect(at({duration: 5}, {unit: 'MINUTES'})).toBe(700 + 10_000)
        expect(at({})).toBe(700 + 1000)
    })

    it('stops at nodes it already reached', () => {
        const graph: Graph = {nodes: {
            tick: {type: 'mob.tick', executions: {next: ['a']}},
            a: {type: 'act', executions: {next: ['a']}},
        }}

        expect(order(graph)).toEqual(['tick', 'a'])
    })
})
