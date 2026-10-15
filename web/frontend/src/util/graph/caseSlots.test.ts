/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {caseSlots} from '~/util/graph/caseSlots'
import type {ConfigView, NodeView} from '~/util/graph/graphView'

function switchNode(cases: Record<number, {value?: unknown, connected?: boolean}>): NodeView {
    const ids = [0, 1, 2, 3].map(index => `case_${index}`)
    return {
        id: 'n', type: 'flow.switch', title: 'Switch', tone: 'flow', description: '', known: true, entry: false, reroute: false, channels: false,
        execIn: true, inputs: [], outputs: [], position: {x: 0, y: 0},
        executions: [...ids.map((id, index) => ({id, name: `Case ${index + 1}`, connected: cases[index]?.connected ?? false})), {id: 'default', name: 'default', connected: false}],
        config: ids.map((id, index) => ({id, beside: id, value: cases[index]?.value ?? null}) as unknown as ConfigView),
    }
}

describe('caseSlots', () => {
    it('draws the first case of an empty node and adds the next', () => {
        const slots = caseSlots(switchNode({}), new Set())
        expect(slots.shown).toEqual(['case_0'])
        expect(slots.next).toBe('case_1')
        expect(slots.removable).toBeUndefined()
    })

    it('draws every case up to the last one in use, without gaps', () => {
        const slots = caseSlots(switchNode({1: {value: 3}}), new Set())
        expect(slots.shown).toEqual(['case_0', 'case_1'])
        expect(slots.next).toBe('case_2')
        expect(slots.removable).toBe('case_1')
        expect(slots.hidden.has('default')).toBe(false)
    })

    it('counts added cases and keeps a wired last case', () => {
        expect(caseSlots(switchNode({}), new Set(['case_1'])).shown).toEqual(['case_0', 'case_1'])
        expect(caseSlots(switchNode({1: {connected: true}}), new Set()).removable).toBeUndefined()
    })

    it('has nothing to add once every case is drawn', () => {
        expect(caseSlots(switchNode({0: {value: 1}, 1: {value: 2}, 2: {value: 3}, 3: {value: 4}}), new Set()).next).toBeUndefined()
    })
})
