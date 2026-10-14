/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {EditorPayload} from '~/api/generated/payload'
import {chosenOffers, initialChoices, setupOperations} from '~/util/session/setupWizard'

function world(enabled: boolean) {
    return {name: 'world', settings: {enabled, atmosphere: {sky: false, heartbeat: true}}}
}

function offer(id: string, pack: string) {
    return {id, pack, mob: {id, origin: {store: 'bloodnight', entry: id, hash: 'h'}}}
}

function payload(legacy = false): EditorPayload {
    return {
        mobs: {speed_zombie: {id: 'speed_zombie'}},
        settings: {main: {resourcePack: {offer: false}}, worlds: {w1: world(false), w2: world(false)}},
        store: {packs: [], offers: [offer('speed_zombie', 'classic'), offer('night_hound', 'classic'), offer('launcher', 'end')], states: {}},
        legacy: legacy ? {main: {resourcePack: {offer: true}}, worlds: {w1: world(true)}, mobs: {old_one: {id: 'old_one'}}, items: {}, notes: []} : undefined,
    } as unknown as EditorPayload
}

describe('setup wizard', () => {
    it('starts from the settings as they are and ticks the free classic mobs', () => {
        expect(initialChoices(payload())).toEqual({
            takeOver: false,
            worlds: {w1: false, w2: false},
            frequency: 'keep',
            difficulty: 'keep',
            effects: false,
            heartbeat: true,
            resourcePack: false,
            mobs: ['night_hound'],
        })
    })

    it('starts from the old installation when there is one', () => {
        const choices = initialChoices(payload(true))
        expect(choices.takeOver).toBe(true)
        expect(choices.worlds).toEqual({w1: true, w2: false})
        expect(choices.resourcePack).toBe(true)
        expect(choices.mobs).toEqual(['speed_zombie', 'night_hound'])
    })

    it('writes presets only into enabled worlds and marks the setup as done', () => {
        const choices = {...initialChoices(payload()), worlds: {w1: true, w2: false}, frequency: 'rarely', difficulty: 'brutal'} as const
        const operations = setupOperations(payload(), choices)
        const paths = operations.map(operation => operation.path)
        expect(paths).toContain('/settings/worlds/w1/settings/nightSelection/interval')
        expect(paths).toContain('/settings/worlds/w1/settings/mobSettings/damageModifier')
        expect(paths.filter(path => path.startsWith('/settings/worlds/w2'))).toEqual(['/settings/worlds/w2/settings/enabled'])
        expect(paths).toContain('/mobs/night_hound')
        expect(operations.at(-1)).toEqual({op: 'add', path: '/settings/main/setup', value: {done: true}})
    })

    it('takes the old installation over before anything else', () => {
        const data = payload(true)
        const choices = initialChoices(data)
        const paths = setupOperations(data, choices).map(operation => operation.path)
        expect(paths.slice(0, 4)).toEqual(['/settings/main', '/settings/worlds/w1', '/mobs', '/items'])
        expect(chosenOffers(data, choices).map(entry => entry.id)).toEqual(['speed_zombie', 'night_hound'])
    })
})
