/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {ParticleEffect} from '~/api/generated/payload'
import {effectCost} from './particleEffects'

function ring(points: number, tiles?: number): ParticleEffect {
    return {name: 'Ring', duration: 39, emitters: [{shape: {type: 'RING', points}, every: 1, tiles}]} as ParticleEffect
}

describe('effectCost', () => {
    it('counts a looping ring per second', () => {
        expect(effectCost(ring(20))).toEqual({count: 400, total: false})
    })

    it('divides a tiled ring by its tiles', () => {
        expect(effectCost(ring(20, 10))).toEqual({count: 40, total: false})
    })

    it('counts a burst shorter than a second in total', () => {
        const burst = {name: 'Burst', duration: 2, emitters: [{shape: {type: 'SPHERE', points: 24}, every: 1}]} as ParticleEffect
        expect(effectCost(burst)).toEqual({count: 72, total: true})
    })

    it('takes a beam as 16 blocks long', () => {
        const beam = {name: 'Beam', duration: 19, emitters: [{shape: {type: 'BEAM', density: 2}, every: 1}]} as ParticleEffect
        expect(effectCost(beam)).toEqual({count: 33 * 20, total: false})
    })

    it('takes a beam with its own end as long as that', () => {
        const bolt = {name: 'Bolt', duration: 0, emitters: [{shape: {type: 'BEAM', density: 2, to: {x: 0, y: -12, z: 0}}}]} as ParticleEffect
        expect(effectCost(bolt)).toEqual({count: 25, total: true})
    })
})
