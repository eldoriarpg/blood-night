/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {arrange, elkGraph, type LayoutNode} from '~/util/graph/autoLayout'

function node(id: string, height = 80): LayoutNode {
    return {
        id,
        width: 240,
        height,
        ports: [
            {id: 'exec-in', x: 0, y: 20, side: 'WEST'},
            {id: 'in:value', x: 0, y: 40, side: 'WEST'},
            {id: 'exec:next', x: 240, y: 20, side: 'EAST'},
            {id: 'out:value', x: 240, y: 40, side: 'EAST'},
        ],
    }
}

const nodes = [node('c'), node('a', 200), node('b')]
const edges = [
    {source: 'a', sourceHandle: 'exec:next', target: 'b', targetHandle: 'exec-in'},
    {source: 'b', sourceHandle: 'exec:next', target: 'c', targetHandle: 'exec-in'},
    {source: 'a', sourceHandle: 'out:value', target: 'c', targetHandle: 'in:value'},
    {source: 'gone', sourceHandle: 'out:value', target: 'c', targetHandle: 'in:value'},
]

describe('auto layout', () => {
    it('gives ELK the nodes with fixed pins and the wires between them', () => {
        const graph = elkGraph(nodes, edges)
        expect(graph.children?.map(child => [child.id, child.width, child.height, child.ports?.length])).toEqual([
            ['c', 240, 80, 4], ['a', 240, 200, 4], ['b', 240, 80, 4],
        ])
        expect(graph.edges?.map(edge => [edge.sources[0], edge.targets[0]])).toEqual([
            ['a::exec:next', 'b::exec-in'], ['b::exec:next', 'c::exec-in'], ['a::out:value', 'c::in:value'],
        ])
    })

    it('lays a chain out left to right without overlaps', async () => {
        const positions = await arrange(nodes, edges)
        const a = positions.get('a')!
        const b = positions.get('b')!
        const c = positions.get('c')!
        expect(a.x).toBeLessThan(b.x)
        expect(b.x).toBeLessThan(c.x)
        expect(b.x - a.x).toBeGreaterThanOrEqual(240)
        expect(c.x - b.x).toBeGreaterThanOrEqual(240)
    })
})
