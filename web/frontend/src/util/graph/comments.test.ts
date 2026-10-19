/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Comment, Graph} from '~/api/generated/payload'
import type {EdgeView} from '~/util/graph/graphView'
import {commentViews, frameAround, hiddenNodes, reroutedEdges} from '~/util/graph/comments'

function frame(x: number, y: number, width: number, height: number, collapsed = false): Comment {
    return {title: '', note: '', color: 'grey', x, y, width, height, collapsed}
}

const positions = new Map([
    ['a', {x: 10, y: 10}],
    ['b', {x: 120, y: 40}],
    ['c', {x: 500, y: 500}],
])

function wire(id: string, source: string, target: string): EdgeView {
    return {id, source, sourceHandle: 'out:value', target, targetHandle: 'in:value', pin: 'number'}
}

describe('comment frames', () => {
    it('hold the nodes and smaller frames whose corner lies inside', () => {
        const graph = {nodes: {}, comments: {outer: frame(0, 0, 400, 300), inner: frame(100, 20, 100, 100)}} as Graph
        const views = commentViews(graph, positions)

        expect(views.find(view => view.id === 'outer')).toMatchObject({members: ['a', 'b'], nested: ['inner'], hidden: false})
        expect(views.find(view => view.id === 'inner')).toMatchObject({members: ['b'], nested: []})
    })

    it('closed frames hide their nodes and the frames inside', () => {
        const graph = {nodes: {}, comments: {outer: frame(0, 0, 400, 300, true), inner: frame(100, 20, 100, 100, true)}} as Graph
        const views = commentViews(graph, positions)

        expect(views.find(view => view.id === 'inner')?.hidden).toBe(true)
        expect(hiddenNodes(views)).toEqual(new Map([['a', 'outer'], ['b', 'outer']]))
    })

    it('reroute wires to a closed frame and leave out the wires inside it', () => {
        const hidden = new Map([['a', 'f'], ['b', 'f']])
        const {edges, pins} = reroutedEdges([wire('w1', 'a', 'b'), wire('w2', 'a', 'c'), wire('w3', 'c', 'b'), wire('w4', 'c', 'c')], hidden)

        expect(edges.map(edge => `${edge.source}/${edge.sourceHandle}>${edge.target}/${edge.targetHandle}`)).toEqual([
            'comment:f/cout:w2>c/in:value',
            'c/out:value>comment:f/cin:w3',
            'c/out:value>c/in:value',
        ])
        expect(pins.get('f')).toEqual({
            inputs: [{handle: 'cin:w3', pin: 'number', node: 'b', port: 'in:value'}],
            outputs: [{handle: 'cout:w2', pin: 'number', node: 'a', port: 'out:value'}],
        })
    })

    it('fit around nodes with room for the title, on the grid', () => {
        expect(frameAround([{x: 100, y: 100, width: 200, height: 80}, {x: 360, y: 150, width: 100, height: 60}], 22))
            .toEqual({x: 66, y: 22, width: 418, height: 220})
    })
})
