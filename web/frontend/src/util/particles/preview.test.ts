/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {ParticleEffect} from '~/api/generated/payload'
import {point, Simulation} from './preview'
import {vec} from './shapes'

const source = {feet: vec(0, 0, 0), height: 2, yaw: 0}
const target = {feet: vec(6, 0, 0), height: 2, yaw: 90}

describe('preview', () => {
    it('emits on its ticks and lets particles fade', () => {
        const effect: ParticleEffect = {
            name: 'Ring',
            duration: 4,
            emitters: [{particle: 'minecraft:dust', data: {color: '#ff0000'}, shape: {type: 'RING', points: 8}, every: 2}],
        }
        const play = new Simulation(effect, source, null, {'minecraft:dust': 'DUST'}, () => 0.5)
        play.step()
        expect(play.dots).toHaveLength(8)
        expect(play.dots[0]?.color).toBe('#ff0000')
        play.step()
        expect(play.dots).toHaveLength(8)
        for (let tick = 0; tick < 40; tick++) play.step()
        expect(play.done).toBe(true)
    })

    it('flies a piece to the target and draws a span between the ends', () => {
        const effect: ParticleEffect = {
            name: 'Spear',
            duration: 10,
            pieces: [
                {kind: 'ITEM', item: 'minecraft:trident', anchor: {at: 'CENTER'}, motion: {ticks: 4}, stick: 2},
                {kind: 'SPAN', block: 'minecraft:red_stained_glass', anchor: {at: 'CENTER'}, grow: 2},
            ],
        }
        const play = new Simulation(effect, source, target, {}, () => 0.5)
        for (let tick = 0; tick < 5; tick++) play.step()
        expect(play.pieces[0]?.position.x).toBeCloseTo(6)
        expect(play.pieces[1]?.end?.x).toBeCloseTo(6)
        expect(point(target, {at: 'EYES'}).y).toBeCloseTo(1.7)
    })
})
