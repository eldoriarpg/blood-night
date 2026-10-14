/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorPayload, Offer} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import type {PatchOperation} from '~/util/patch/jsonPatch'

/** How often blood nights come: kept as they are, or one of the presets the docs recommend. */
export type Frequency = 'keep' | 'rarely' | 'sometimes' | 'often' | 'moon'

/** How hard blood nights are: kept as they are, or one of three presets. */
export type Difficulty = 'keep' | 'gentle' | 'normal' | 'brutal'

/** What the admin picked in the setup wizard. */
export interface SetupChoices {
    /** Whether the old BloodNight's converted files replace the session's first. */
    takeOver: boolean
    /** Whether each world has blood nights, by the world's UID. */
    worlds: Record<string, boolean>
    frequency: Frequency
    difficulty: Difficulty
    /** Sky, vignette, darkness pulses and screen effect together. */
    effects: boolean
    heartbeat: boolean
    resourcePack: boolean
    /** The ids of the store mobs to install. */
    mobs: string[]
}

const FREQUENCIES: Readonly<Record<Exclude<Frequency, 'keep'>, Readonly<Record<string, unknown>>>> = {
    rarely: {type: 'INTERVAL', interval: {interval: 7, probability: 100}},
    sometimes: {type: 'RANDOM', random: {probability: 30}},
    often: {type: 'RANDOM', random: {probability: 60}},
    moon: {type: 'MOON_PHASE'},
}

const DIFFICULTIES: Readonly<Record<Exclude<Difficulty, 'keep'>, number>> = {gentle: 1, normal: 2, brutal: 3}

const EFFECTS: readonly string[] = ['sky', 'vignette', 'darknessPulses', 'screenEffect']

/**
 * @param payload the session's payload
 * @param takeOver whether the old BloodNight's files are taken over
 * @returns the payload's settings, or the old BloodNight's converted ones where they are taken over
 */
function source(payload: EditorPayload, takeOver: boolean) {
    const legacy = takeOver ? payload.legacy : undefined
    return {
        main: legacy?.main ?? payload.settings.main,
        worlds: {...payload.settings.worlds, ...legacy?.worlds},
        mobs: legacy?.mobs ?? payload.mobs ?? {},
    }
}

/**
 * @param payload the session's payload
 * @param takeOver whether the old BloodNight's files are taken over
 * @returns what the wizard starts with: the settings as they are, the `classic` pack ticked where its mobs are free
 */
export function initialChoices(payload: EditorPayload, takeOver = payload.legacy !== undefined): SetupChoices {
    const {main, worlds, mobs} = source(payload, takeOver)
    const first = Object.values(worlds)[0]?.settings.atmosphere
    return {
        takeOver,
        worlds: Object.fromEntries(Object.entries(worlds).map(([uid, world]) => [uid, world.settings.enabled ?? false])),
        frequency: 'keep',
        difficulty: 'keep',
        effects: first?.sky ?? true,
        heartbeat: first?.heartbeat ?? true,
        resourcePack: main.resourcePack?.offer ?? true,
        mobs: (payload.store?.offers ?? []).filter(offer => offer.pack === 'classic' && !mobs[offer.id]).map(offer => offer.id),
    }
}

/**
 * @param payload the session's payload
 * @param choices what the admin picked
 * @returns the store offers the choices install: ticked and not taken by a mob of the result
 */
export function chosenOffers(payload: EditorPayload, choices: SetupChoices): Offer[] {
    const {mobs} = source(payload, choices.takeOver)
    return (payload.store?.offers ?? []).filter(offer => choices.mobs.includes(offer.id) && !mobs[offer.id])
}

function takeOverOperations(payload: EditorPayload): PatchOperation[] {
    const legacy = payload.legacy
    if (!legacy) return []
    return [
        {op: 'add', path: pointer('settings', 'main'), value: legacy.main},
        ...Object.entries(legacy.worlds).map(([uid, world]) =>
            ({op: 'add', path: pointer('settings', 'worlds', uid), value: world}) as PatchOperation),
        {op: 'add', path: pointer('mobs'), value: legacy.mobs},
        {op: 'add', path: pointer('items'), value: legacy.items},
    ]
}

function worldOperations(uid: string, enabled: boolean, choices: SetupChoices): PatchOperation[] {
    const at = (...path: string[]) => pointer('settings', 'worlds', uid, 'settings', ...path)
    const operations: PatchOperation[] = [{op: 'add', path: at('enabled'), value: enabled}]
    if (!enabled) return operations
    if (choices.frequency !== 'keep') {
        for (const [key, value] of Object.entries(FREQUENCIES[choices.frequency])) {
            operations.push({op: 'add', path: at('nightSelection', key), value})
        }
    }
    if (choices.difficulty !== 'keep') {
        const factor = DIFFICULTIES[choices.difficulty]
        operations.push({op: 'add', path: at('mobSettings', 'damageModifier'), value: factor})
        operations.push({op: 'add', path: at('mobSettings', 'healthMultiplier'), value: factor})
    }
    for (const effect of EFFECTS) operations.push({op: 'add', path: at('atmosphere', effect), value: choices.effects})
    operations.push({op: 'add', path: at('atmosphere', 'heartbeat'), value: choices.heartbeat})
    return operations
}

/**
 * Everything the wizard changes, as one change: the old BloodNight's files when taken over, then each world's
 * settings, the resource pack offer, the ticked store mobs, and the setup marked as done.
 *
 * @param payload the session's payload
 * @param choices what the admin picked
 * @returns the operations
 */
export function setupOperations(payload: EditorPayload, choices: SetupChoices): PatchOperation[] {
    const head = choices.takeOver ? takeOverOperations(payload) : []
    const mobsMissing = !choices.takeOver && !payload.mobs
    return [
        ...head,
        ...Object.entries(choices.worlds).flatMap(([uid, enabled]) => worldOperations(uid, enabled, choices)),
        {op: 'add', path: pointer('settings', 'main', 'resourcePack', 'offer'), value: choices.resourcePack},
        ...(mobsMissing ? [{op: 'add', path: pointer('mobs'), value: {}} as PatchOperation] : []),
        ...chosenOffers(payload, choices).map(offer => ({op: 'add', path: pointer('mobs', offer.id), value: offer.mob}) as PatchOperation),
        {op: 'add', path: pointer('settings', 'main', 'setup'), value: {done: true}},
    ]
}
