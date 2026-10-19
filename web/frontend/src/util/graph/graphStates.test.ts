/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {StateChanges, removable, stateCatalog, stateGraph, stateRows, transitions} from './graphStates'
import {stateLayout} from './stateLayout'
import {GraphChanges} from './graphChanges'

const graph: Graph = {
    states: [
        {name: 'fight', initial: true},
        {name: 'calm', parent: 'fight', initial: true},
        {name: 'angry', parent: 'fight'},
        {name: 'casting'},
    ],
    nodes: {
        always: {type: 'value.number'},
        tick: {type: 'mob.tick', state: 'calm', executions: {next: ['go']}},
        go: {type: 'state.go_to', state: 'calm', config: {state: 'angry'}, inputs: {when: {node: 'always', port: 'value'}}},
        cast: {type: 'state.go_to_one_of', state: 'angry', config: {state_1: 'casting', state_2: 'calm', state_3: ''}},
        aura: {type: 'world.play_effect', state: 'fight'},
        reader: {type: 'debug.print', state: 'angry', inputs: {value: {node: 'aura', port: 'x'}}},
    },
    comments: {frame: {title: '', note: '', color: 'grey', x: 0, y: 0, width: 0, height: 0, collapsed: false, state: 'calm'}},
}

describe('graph states', () => {
    it('lists every state below its parent with its depth', () => {
        expect(stateRows(graph).map(row => `${'-'.repeat(row.depth)}${row.name}`)).toEqual(['fight', '-calm', '-angry', 'casting'])
    })

    it('shows a state with the values it reads from outside', () => {
        const calm = stateGraph(graph, 'calm')
        expect(Object.keys(calm.graph.nodes).sort()).toEqual(['always', 'go', 'tick'])
        expect([...calm.borrowed]).toEqual(['always'])
        expect(Object.keys(calm.graph.comments ?? {})).toEqual(['frame'])
        expect(Object.keys(stateGraph(graph, 'angry').graph.nodes).sort()).toEqual(['aura', 'cast', 'reader'])
        expect(Object.keys(stateGraph(graph, undefined).graph.nodes)).toEqual(['always'])
    })

    it('gives a borrowed node a place of its own in every state', () => {
        const placed: Graph = {
            ...graph,
            nodes: {...graph.nodes, always: {type: 'value.number', editor: {x: 5, y: 6, at: {calm: {x: 100, y: 200}}}}},
        }
        expect(stateGraph(placed, 'calm').graph.nodes.always?.editor).toEqual({at: {calm: {x: 100, y: 200}}, x: 100, y: 200})
        expect(stateGraph(placed, undefined).graph.nodes.always?.editor).toEqual({x: 5, y: 6, at: {calm: {x: 100, y: 200}}})

        const inCalm = new GraphChanges('/g', placed, 'calm').move('always', {x: 1, y: 2})
        expect(inCalm).toEqual([{op: 'add', path: '/g/nodes/always/editor', value: {x: 5, y: 6, at: {calm: {x: 1, y: 2}}}}])
        const own = new GraphChanges('/g', placed, 'calm').move('tick', {x: 3, y: 4})
        expect(own).toEqual([{op: 'add', path: '/g/nodes/tick/editor', value: {x: 3, y: 4}}])
    })

    it('offers the state nodes only where they fit', () => {
        const catalog = {nodes: [{id: 'state.enter'}, {id: 'state.go_to'}, {id: 'mob.tick'}]} as Catalog
        const ids = (state: string | undefined, of: Graph = graph) => (stateCatalog(catalog, of, state).nodes ?? []).map(node => node.id)
        expect(ids('calm')).toEqual(['state.enter', 'state.go_to', 'mob.tick'])
        expect(ids(undefined)).toEqual(['state.go_to', 'mob.tick'])
        expect(ids(undefined, {nodes: {}})).toEqual(['mob.tick'])
    })

    it('finds the moves between states', () => {
        expect(transitions(graph)).toEqual([
            {from: 'calm', to: 'angry'},
            {from: 'angry', to: 'casting'},
            {from: 'angry', to: 'calm'},
        ])
    })

    it('renames a state everywhere it is named', () => {
        const operations = new StateChanges('/mobs/x/behaviour', graph).rename('calm', 'idle')
        expect(operations[0]).toEqual({
            op: 'add',
            path: '/mobs/x/behaviour/states',
            value: [
                {name: 'fight', initial: true},
                {name: 'idle', parent: 'fight', initial: true},
                {name: 'angry', parent: 'fight'},
                {name: 'casting'},
            ],
        })
        const paths = operations.slice(1).map(operation => operation.path)
        expect(paths).toEqual([
            '/mobs/x/behaviour/nodes/tick',
            '/mobs/x/behaviour/nodes/go',
            '/mobs/x/behaviour/nodes/cast',
            '/mobs/x/behaviour/comments/frame/state',
        ])
    })

    it('adds states after their siblings and keeps one initial', () => {
        const added = new StateChanges('/g', graph).add('dive', 'fight')[0] as {value: {name: string}[]}
        expect(added.value.map(state => state.name)).toEqual(['fight', 'calm', 'angry', 'dive', 'casting'])
        const first = new StateChanges('/g', {nodes: {}}).add('only', undefined)[0] as {value: unknown}
        expect(first.value).toEqual([{name: 'only', initial: true}])
    })

    it('removes only states nothing lies in', () => {
        expect(removable(graph, 'casting')).toBe(true)
        expect(removable(graph, 'fight')).toBe(false)
        expect(removable(graph, 'calm')).toBe(false)
    })

    it('lays nested states out inside their parents', async () => {
        const {boxes, arrows} = await stateLayout(graph)
        const box = (name: string) => boxes.find(candidate => candidate.name === name)!
        const fight = box('fight')
        const calm = box('calm')
        expect(calm.x).toBeGreaterThanOrEqual(fight.x)
        expect(calm.x + calm.width).toBeLessThanOrEqual(fight.x + fight.width)
        expect(fight.parent).toBe(true)
        expect(arrows).toHaveLength(3)
    })
})
