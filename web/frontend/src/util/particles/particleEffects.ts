/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {DataKind, EditorPayload, Emitter, Graph, ParticleEffect, Piece} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import {jsonClone, type PatchOperation} from '~/util/patch/jsonPatch'
import {pointCount} from '~/util/particles/shapes'

/** A particle effect with its id and whether it ships with the plugin, read only. */
export interface EffectEntry {
    id: string
    effect: ParticleEffect
    builtIn: boolean
}

/** Where an effect is used: worn by a mob, or played by a mob's or a macro's graph. */
export interface EffectUse {
    kind: 'mob' | 'macro'
    /** The mob's or the macro's id. */
    id: string
    /** The pointer to what names the effect. */
    path: string
}

/** The node that plays effects in graphs. */
export const PLAY_EFFECT = 'world.play_effect'
/** The namespace of built-in effects. */
export const BUILT_IN = 'bloodnight:'
/** The slots of a mob's Particles part. */
export const WORN = ['ambient', 'spawn', 'death'] as const
/** The most emitters and pieces one effect has, and particles per emission, as on the server. */
export const LIMITS = {emitters: 8, pieces: 4, perEmission: 256, duration: 600} as const
/** Particles per second per player above which the editor warns. */
export const COSTLY = 400

/** The fields of an emitter's data each kind of particle data uses. */
export const DATA_FIELDS: Readonly<Record<DataKind, readonly string[]>> = {
    NONE: [],
    DUST: ['color', 'size'],
    DUST_TRANSITION: ['color', 'to', 'size'],
    SPELL: ['color', 'power'],
    COLOR: ['color'],
    POWER: ['power'],
    DELAY: ['delay'],
    BLOCK: ['block'],
    ITEM: ['item'],
    TRAIL: ['color', 'ticks'],
    UNSUPPORTED: [],
}

const ID_CHARACTERS = /[^a-z0-9]+/g

/**
 * @param payload the payload
 * @returns the payload's own effects and the built-in ones, each group sorted by name
 */
export function effectEntries(payload: EditorPayload | null | undefined): EffectEntry[] {
    const byName = (a: EffectEntry, b: EffectEntry) => (a.effect.name || a.id).localeCompare(b.effect.name || b.id)
    const own = Object.entries(payload?.particleEffects ?? {}).map(([id, effect]) => ({id, effect, builtIn: false}))
    const builtIn = Object.entries(payload?.builtInEffects ?? {}).map(([id, effect]) => ({id, effect, builtIn: true}))
    return [...own.sort(byName), ...builtIn.sort(byName)]
}

/**
 * @param payload the payload
 * @returns every effect id with its name, for pickers
 */
export function effectLabels(payload: EditorPayload | null | undefined): Record<string, string> {
    return Object.fromEntries(effectEntries(payload).map(entry => [entry.id, entry.effect.name || entry.id]))
}

/**
 * @param name a new effect's name
 * @param taken the ids in use
 * @returns an id made from the name: lower case letters, digits and underscores, numbered when taken
 */
export function effectId(name: string, taken: readonly string[]): string {
    const base = name.trim().toLowerCase().replaceAll(ID_CHARACTERS, '_').replaceAll(/^_+|_+$/g, '') || 'effect'
    let id = base
    for (let number = 2; taken.includes(id); number++) id = `${base}_${number}`
    return id
}

/**
 * @param name the effect's name
 * @param from an effect to start from, e.g. a built-in one; none starts with a single flame
 * @returns the new effect
 */
export function newEffect(name: string, from?: ParticleEffect): ParticleEffect {
    if (from) return {...jsonClone(from), name: name.trim()}
    return {name: name.trim(), duration: 20, emitters: [newEmitter()], pieces: [], self: true}
}

/**
 * @returns an emitter drawing one flame at the centre of the source every tick
 */
export function newEmitter(): Emitter {
    return {
        particle: 'minecraft:flame',
        anchor: {at: 'CENTER', on: 'SOURCE', piece: 0},
        offset: {x: 0, y: 0, z: 0},
        shape: {type: 'POINT'},
        count: 1,
        spread: {x: 0, y: 0, z: 0},
        speed: 0,
        every: 1,
        delay: 0,
        chance: 1,
    }
}

/**
 * @returns a piece showing an item at the source, turned along the path
 */
export function newPiece(): Piece {
    return {
        kind: 'ITEM',
        item: 'minecraft:trident',
        scale: 1,
        rotation: {pitch: 0, yaw: 0, roll: 0},
        anchor: {at: 'EYES', on: 'SOURCE', piece: 0},
        offset: {x: 0, y: 0, z: 0},
        facing: 'PATH',
        stick: 20,
        glow: false,
        width: 0.1,
        grow: 0,
    }
}

/**
 * @returns the operations adding an effect
 */
export function addEffect(payload: EditorPayload, id: string, effect: ParticleEffect): PatchOperation[] {
    const head: PatchOperation[] = payload.particleEffects ? [] : [{op: 'add', path: '/particleEffects', value: {}}]
    return [...head, {op: 'add', path: pointer('particleEffects', id), value: effect}]
}

/**
 * @returns the operations removing an effect and every place that names it: mobs stop wearing it, graphs keep their
 *     play nodes, which then show the effect as missing
 */
export function removeEffect(payload: EditorPayload, id: string): PatchOperation[] {
    const worn = usesOf(payload, id).filter(use => use.kind === 'mob' && use.path.includes('/particles/'))
    return [...worn.map(use => ({op: 'remove', path: use.path}) as PatchOperation), {op: 'remove', path: pointer('particleEffects', id)}]
}

/**
 * @returns where mobs wear the effect and where graphs play it
 */
export function usesOf(payload: EditorPayload | null | undefined, id: string): EffectUse[] {
    const uses: EffectUse[] = []
    for (const [mobId, mob] of Object.entries(payload?.mobs ?? {})) {
        for (const slot of WORN) {
            if (mob.particles?.[slot] === id) uses.push({kind: 'mob', id: mobId, path: pointer('mobs', mobId, 'particles', slot)})
        }
        uses.push(...played(mob.behaviour, id).map(node => ({kind: 'mob' as const, id: mobId, path: pointer('mobs', mobId, 'behaviour', 'nodes', node)})))
    }
    payload?.macros?.forEach((macro, index) => {
        uses.push(...played(macro.body, id).map(node => ({kind: 'macro' as const, id: macro.id, path: pointer('macros', String(index), 'body', 'nodes', node)})))
    })
    return uses
}

function played(graph: Graph | undefined, id: string): string[] {
    return Object.entries(graph?.nodes ?? {})
        .filter(([, node]) => node.type === PLAY_EFFECT && node.config?.effect === id)
        .map(([node]) => node)
}

/** What an effect costs each player near it: particles per second, or in total for a play shorter than a second. */
export interface EffectCost {
    count: number
    total: boolean
}

/**
 * @param effect an effect
 * @returns about how many particles each player near it gets while it plays; a beam or arc without its own end is
 *     taken as 16 blocks long
 */
export function effectCost(effect: ParticleEffect): EffectCost {
    const duration = Math.max(effect.duration ?? 20, 0) + 1
    const total = (effect.emitters ?? []).reduce((sum, emitter) => {
        const every = Math.max(emitter.every ?? 1, 1)
        const emissions = Math.max(0, Math.ceil((duration - (emitter.delay ?? 0)) / every))
        const share = 1 / Math.max(emitter.tiles ?? 1, 1)
        return sum + emissions * (emitter.chance ?? 1) * Math.ceil(pointCount(emitter.shape) * share) * (emitter.count ?? 1)
    }, 0)
    if (duration < 20) return {count: Math.round(total), total: true}
    return {count: Math.round(total * 20 / duration), total: false}
}

/**
 * @returns what breaks the server's limits, as i18n keys with their values
 */
export function limitProblems(effect: ParticleEffect): {key: string, values: Record<string, number>}[] {
    const problems: {key: string, values: Record<string, number>}[] = []
    if ((effect.emitters?.length ?? 0) > LIMITS.emitters) problems.push({key: 'editor.particles.limit.emitters', values: {max: LIMITS.emitters}})
    if ((effect.pieces?.length ?? 0) > LIMITS.pieces) problems.push({key: 'editor.particles.limit.pieces', values: {max: LIMITS.pieces}})
    effect.emitters?.forEach((emitter, index) => {
        const particles = pointCount(emitter.shape) * (emitter.count ?? 1)
        if (emitter.shape?.type !== 'BEAM' && emitter.shape?.type !== 'ARC' && particles > LIMITS.perEmission) {
            problems.push({key: 'editor.particles.limit.perEmission', values: {emitter: index + 1, count: particles, max: LIMITS.perEmission}})
        }
    })
    return problems
}

/**
 * @returns whether the effect uses a target: something sits on it, runs to it or travels to it
 */
export function aimed(effect: ParticleEffect): boolean {
    const emitters = (effect.emitters ?? []).some(emitter => emitter.anchor?.on === 'TARGET' || emitter.motion !== undefined
        || emitter.shape?.type === 'BEAM' || emitter.shape?.type === 'ARC')
    const pieces = (effect.pieces ?? []).some(piece => piece.anchor?.on === 'TARGET' || piece.motion !== undefined || piece.kind === 'SPAN')
    return emitters || pieces
}
