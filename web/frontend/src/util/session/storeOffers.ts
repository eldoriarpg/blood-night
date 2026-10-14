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
 * @returns the operations installing it
 */
export function installOffer(offer: Offer, payload: EditorPayload): PatchOperation[] {
    return addMob(payload, offer.mob)
}

/**
 * @param offers mobs the store offers
 * @param payload the session's payload
 * @returns the operations installing all of them at once
 */
export function installOffers(offers: readonly Offer[], payload: EditorPayload): PatchOperation[] {
    const head: PatchOperation[] = payload.mobs ? [] : [{op: 'add', path: '/mobs', value: {}}]
    return [...head, ...offers.map(offer => ({op: 'add', path: pointer('mobs', offer.id), value: offer.mob}) as PatchOperation)]
}

/**
 * Takes the offer's logic (behaviour, extension and movement) and keeps the installed mob's own name, numbers and
 * drops.
 *
 * @param offer a mob the store offers
 * @returns the operations updating the installed mob
 */
export function updateOffer(offer: Offer): PatchOperation[] {
    return [
        {op: 'replace', path: pointer('mobs', offer.id, 'behaviour'), value: offer.mob.behaviour},
        {op: 'replace', path: pointer('mobs', offer.id, 'extension'), value: offer.mob.extension},
        {op: 'add', path: pointer('mobs', offer.id, 'movement'), value: offer.mob.movement},
        {op: 'add', path: pointer('mobs', offer.id, 'origin'), value: offer.mob.origin},
    ]
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
