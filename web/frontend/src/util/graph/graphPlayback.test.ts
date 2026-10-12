/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {graphPlayback, LINGER_MILLIS} from '~/util/graph/graphPlayback'

beforeEach(() => {
    vi.useFakeTimers()
})

afterEach(() => {
    vi.useRealTimers()
})

describe('graphPlayback', () => {
    it('lights the steps one after another and clears them after the last', () => {
        const playback = graphPlayback()
        playback.play([
            {node: 'tick', wire: undefined, at: 0},
            {node: 'ignite', wire: 'exec:tick:next:0', at: 350},
        ])

        vi.advanceTimersByTime(0)
        expect([...playback.order.value]).toEqual([['tick', 1]])
        expect(playback.current.value).toBe('tick')

        vi.advanceTimersByTime(350)
        expect([...playback.order.value]).toEqual([['tick', 1], ['ignite', 2]])
        expect([...playback.wires.value]).toEqual(['exec:tick:next:0'])
        expect(playback.current.value).toBe('ignite')

        vi.advanceTimersByTime(LINGER_MILLIS)
        expect(playback.order.value.size).toBe(0)
        expect(playback.current.value).toBeNull()
    })

    it('starts over when played again', () => {
        const playback = graphPlayback()
        playback.play([{node: 'a', wire: undefined, at: 0}, {node: 'b', wire: undefined, at: 1000}])
        vi.advanceTimersByTime(0)
        playback.play([{node: 'c', wire: undefined, at: 0}])
        vi.advanceTimersByTime(1000)

        expect([...playback.order.value]).toEqual([['c', 1]])
    })
})
