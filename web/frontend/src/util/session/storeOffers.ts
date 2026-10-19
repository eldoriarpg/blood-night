/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorPayload, Offer} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import type {PatchOperation} from '~/util/patch/jsonPatch'
import {addMob} from '~/util/session/newEntries'

/**
 * Where a store mob stands for this session: not installed, installed as offered, offered in a newer version, changed
 * since it was installed (by the server, when the session opened), or its id taken by a mob of the server's own.
 */
export type OfferState = 'available' | 'installed' | 'update' | 'changed' | 'taken'

/**
 * @param offer a mob the store offers
 * @param payload the session's payload
 * @returns where the offer stands
 */
export function offerState(offer: Offer, payload: EditorPayload): OfferState {
    const mob = payload.mobs?.[offer.id]
    if (!mob) return 'available'
    if (mob.origin?.entry !== offer.id) return 'taken'
    const known = payload.store?.states?.[offer.id]
    if (known === 'CHANGED' || known === 'CHANGED_OUTDATED') return 'changed'
    return mob.origin.hash === offer.hash ? 'installed' : 'update'
}

/**
 * @param offer a mob the store offers
 * @param payload the session's payload
 * @returns the operations installing it, with the loot tables it brings that the session lacks
 */
export function installOffer(offer: Offer, payload: EditorPayload): PatchOperation[] {
    return [...addMob(payload, offer.mob), ...addMissing([offer], payload, 'lootTables'), ...addMissing([offer], payload, 'particleEffects')]
}

/**
 * @param offers mobs the store offers
 * @param payload the session's payload
 * @returns the operations installing all of them at once
 */
export function installOffers(offers: readonly Offer[], payload: EditorPayload): PatchOperation[] {
    const head: PatchOperation[] = payload.mobs ? [] : [{op: 'add', path: '/mobs', value: {}}]
    return [
        ...head,
        ...offers.map(offer => ({op: 'add', path: pointer('mobs', offer.id), value: offer.mob}) as PatchOperation),
        ...addMissing(offers, payload, 'lootTables'),
        ...addMissing(offers, payload, 'particleEffects'),
    ]
}

/**
 * @returns the operations adding the loot tables or particle effects the offers bring and the session has none of
 *     that id, each once; one the session has stays as it is
 */
function addMissing(offers: readonly Offer[], payload: EditorPayload, root: 'lootTables' | 'particleEffects'): PatchOperation[] {
    const missing = new Map<string, unknown>()
    for (const offer of offers) {
        for (const [id, value] of Object.entries(offer[root] ?? {})) {
            if (!payload[root]?.[id] && !missing.has(id)) missing.set(id, value)
        }
    }
    if (missing.size === 0) return []
    const head: PatchOperation[] = payload[root] ? [] : [{op: 'add', path: `/${root}`, value: {}}]
    return [...head, ...[...missing].map(([id, value]) => ({op: 'add', path: pointer(root, id), value}) as PatchOperation)]
}

/**
 * @returns whether the installed mob wears other particle effects than its store entry, which an update can take
 */
export function looksDiffer(offer: Offer, payload: EditorPayload): boolean {
    const mob = payload.mobs?.[offer.id]
    if (!mob || mob.origin?.entry !== offer.id) return false
    return JSON.stringify(mob.particles ?? null) !== JSON.stringify(offer.mob.particles ?? null)
}

/**
 * Takes the offer's logic (behaviour, extension and movement) and keeps the installed mob's own name, numbers, drops
 * and particle effects, unless asked to take the store's particle effects too: then the mob wears the offer's, and the
 * effects the offer brings replace those of the same id.
 *
 * @param offer a mob the store offers
 * @param payload the session's payload, needed when the particle effects are taken
 * @param particles whether the store's particle effects are taken
 * @returns the operations updating the installed mob
 */
export function updateOffer(offer: Offer, payload?: EditorPayload, particles = false): PatchOperation[] {
    const logic: PatchOperation[] = [
        {op: 'replace', path: pointer('mobs', offer.id, 'behaviour'), value: offer.mob.behaviour},
        {op: 'replace', path: pointer('mobs', offer.id, 'extension'), value: offer.mob.extension},
        {op: 'add', path: pointer('mobs', offer.id, 'movement'), value: offer.mob.movement},
        {op: 'add', path: pointer('mobs', offer.id, 'origin'), value: offer.mob.origin},
    ]
    if (!particles || !payload) return logic
    const worn: PatchOperation = offer.mob.particles
        ? {op: 'add', path: pointer('mobs', offer.id, 'particles'), value: offer.mob.particles}
        : {op: 'remove', path: pointer('mobs', offer.id, 'particles')}
    const effects = Object.entries(offer.particleEffects ?? {})
    const head: PatchOperation[] = effects.length > 0 && !payload.particleEffects ? [{op: 'add', path: '/particleEffects', value: {}}] : []
    const brought = effects.map(([id, effect]) => ({op: 'add', path: pointer('particleEffects', id), value: effect}) as PatchOperation)
    const keepsWorn = !offer.mob.particles && !payload.mobs?.[offer.id]?.particles
    return [...logic, ...(keepsWorn ? [] : [worn]), ...head, ...brought]
}

/**
 * @param texts a text by language, e.g. `{en: 'Speed Zombie', de: 'Flinker Zombie'}`
 * @param locale the editor's language, e.g. `de`
 * @param fallback what to show without any text
 * @returns the text in the editor's language, else in English, else the fallback
 */
export function localized(texts: Readonly<Record<string, string>> | undefined, locale: string, fallback: string): string {
    return texts?.[locale.split('-')[0] ?? locale] ?? texts?.en ?? fallback
}
