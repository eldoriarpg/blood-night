/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Shape} from '~/api/generated/payload'
import fixture from './shapes.fixture.json'
import {around, between, cells, frameOf, pointCount, travelled, vec} from './shapes'

interface FixtureCase {
    kind: string
    shape: Shape
    points: number[][]
}

describe('shapes', () => {
    it('draws every shape as the server does', () => {
        const frame = frameOf(vec(1, 2, 3), vec(1, 0.5, 0))
        const moment = {progress: 0.4, emission: 3, tick: 7}
        for (const entry of fixture as FixtureCase[]) {
            const points = entry.kind === 'around'
                ? around(entry.shape, frame, moment)
                : between(entry.shape, vec(0, 1, 0), vec(5, 3, -2), moment)
            expect(points.length, JSON.stringify(entry.shape)).toBe(entry.points.length)
            points.forEach((point, index) => {
                const [x, y, z] = entry.points[index] ?? []
                expect(point.x).toBeCloseTo(x ?? NaN, 5)
                expect(point.y).toBeCloseTo(y ?? NaN, 5)
                expect(point.z).toBeCloseTo(z ?? NaN, 5)
            })
        }
    })

    it('counts points without drawing them', () => {
        expect(pointCount({type: 'PATTERN', rows: ['x.x'], mirror: true})).toBe(4)
        expect(pointCount({type: 'SPIRAL', points: 200, strands: 2})).toBe(256)
        expect(cells(['.x', 'x '])).toEqual([[1, 0], [0, 1]])
    })

    it('travels to the target and on past it', () => {
        const motion = {ticks: 10, through: 2}
        expect(travelled(motion, vec(0, 0, 0), vec(4, 0, 0), 5).x).toBeCloseTo(2)
        expect(travelled(motion, vec(0, 0, 0), vec(4, 0, 0), 20).x).toBeCloseTo(6)
    })
})
