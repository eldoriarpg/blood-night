/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {CustomMob, EditorPayload, Offer} from '~/api/generated/payload'
import {installOffer, installOffers, localized, looksDiffer, offerState, updateOffer} from '~/util/session/storeOffers'

function mob(id: string, hash?: string): CustomMob {
    return {id, origin: hash ? {store: 'bloodnight', entry: id, hash} : undefined} as unknown as CustomMob
}

const offer = {id: 'speed_zombie', hash: 'new', mob: {...mob('speed_zombie', 'new'), behaviour: {nodes: {}}, extension: {type: 'NONE'}}} as unknown as Offer

function payload(mobs: CustomMob[], states: Record<string, string> = {}): EditorPayload {
    return {mobs: Object.fromEntries(mobs.map(entry => [entry.id, entry])), store: {packs: [], offers: [offer], states}} as unknown as EditorPayload
}

describe('store offers', () => {
    it('tells where an offer stands', () => {
        expect(offerState(offer, payload([]))).toBe('available')
        expect(offerState(offer, payload([mob('speed_zombie', 'new')]))).toBe('installed')
        expect(offerState(offer, payload([mob('speed_zombie', 'old')]))).toBe('update')
        expect(offerState(offer, payload([mob('speed_zombie', 'old')], {speed_zombie: 'CHANGED_OUTDATED'}))).toBe('changed')
        expect(offerState(offer, payload([mob('speed_zombie')]))).toBe('taken')
    })

    it('installs the offered mob and updates only its logic', () => {
        expect(installOffer(offer, payload([]))).toEqual([{op: 'add', path: '/mobs/speed_zombie', value: offer.mob}])
        expect(installOffers([offer], {store: {}} as unknown as EditorPayload).map(operation => operation.path)).toEqual(['/mobs', '/mobs/speed_zombie'])
        expect(updateOffer(offer).map(operation => operation.path)).toEqual([
            '/mobs/speed_zombie/behaviour', '/mobs/speed_zombie/extension', '/mobs/speed_zombie/movement', '/mobs/speed_zombie/origin',
        ])
    })

    it('brings the offer\'s loot tables the session lacks and keeps its own', () => {
        const withTables = {...offer, lootTables: {speed_zombie: {name: 'Speed Zombie', entries: []}, bones: {name: 'Bones', entries: []}}} as unknown as Offer
        const own = {...payload([]), lootTables: {bones: {name: 'Mine', entries: []}}} as unknown as EditorPayload
        expect(installOffer(withTables, own).map(operation => operation.path)).toEqual(['/mobs/speed_zombie', '/lootTables/speed_zombie'])
        expect(installOffers([withTables, withTables], payload([])).map(operation => operation.path))
            .toEqual(['/mobs/speed_zombie', '/mobs/speed_zombie', '/lootTables', '/lootTables/speed_zombie', '/lootTables/bones'])
    })

    it('takes the store\'s particle effects only when asked', () => {
        const glowing = {...offer, mob: {...offer.mob, particles: {ambient: 'glow'}}, particleEffects: {glow: {name: 'Glow'}}} as unknown as Offer
        const restyled = {...mob('speed_zombie', 'new'), particles: {ambient: 'mine'}} as unknown as CustomMob
        const session = payload([restyled])
        expect(looksDiffer(glowing, session)).toBe(true)
        expect(updateOffer(glowing, session).map(operation => operation.path)).not.toContain('/mobs/speed_zombie/particles')
        expect(updateOffer(glowing, session, true).map(operation => operation.path)).toEqual([
            '/mobs/speed_zombie/behaviour', '/mobs/speed_zombie/extension', '/mobs/speed_zombie/movement', '/mobs/speed_zombie/origin',
            '/mobs/speed_zombie/particles', '/particleEffects', '/particleEffects/glow',
        ])
        expect(installOffer(glowing, payload([])).map(operation => operation.path)).toEqual(['/mobs/speed_zombie', '/particleEffects', '/particleEffects/glow'])
    })

    it('picks the editor language, then English', () => {
        expect(localized({en: 'Speed Zombie', de: 'Flinker Zombie'}, 'de', 'x')).toBe('Flinker Zombie')
        expect(localized({en: 'Speed Zombie'}, 'fr', 'x')).toBe('Speed Zombie')
        expect(localized(undefined, 'en', 'x')).toBe('x')
    })
})
