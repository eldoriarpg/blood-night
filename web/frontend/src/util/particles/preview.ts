/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Anchor, DataKind, Emitter, Facing, ParticleEffect, Piece} from '~/api/generated/payload'
import {
    around, between, betweenEnds, facing, frameOf, inWorld, length, minus, normalized, plus, shapeOf, times, travelled, vec,
    type Vec,
} from '~/util/particles/shapes'

/** A figure the preview plays an effect on: where its feet are, how tall it is and where its body faces. */
export interface Body {
    feet: Vec
    height: number
    /** Minecraft yaw in degrees. */
    yaw: number
}

/** One particle drawn by the preview. */
export interface Dot {
    position: Vec
    velocity: Vec
    particle: string
    color: string | undefined
    fade: string | undefined
    size: number
    age: number
    life: number
}

/** One display piece drawn by the preview: an item or block at a point, or a bar from a point to another. */
export interface Shown {
    position: Vec
    end: Vec | undefined
    piece: Piece
}

const LIFE = 16
const EYES = 0.85
const ABOVE = 0.3

/**
 * An effect played on figures, one tick at a time, the way the server plays it. Particles move and fade only roughly
 * as the game draws them: the game decides that on each player's client.
 */
export class Simulation {
    readonly dots: Dot[] = []
    pieces: Shown[] = []
    private tick = 0

    /**
     * @param effect the effect
     * @param source the figure it plays on
     * @param target the other end, or none
     * @param kinds the data kind of each particle, from the server's choices
     * @param random a source of chance
     */
    constructor(
        private readonly effect: ParticleEffect,
        private readonly source: Body,
        private readonly target: Body | null,
        private readonly kinds: Readonly<Record<string, DataKind>>,
        private readonly random: () => number = Math.random,
    ) {}

    /** @returns whether the play is over, its particles and pieces gone */
    get done(): boolean {
        return this.tick > this.duration && this.dots.length === 0 && this.pieces.length === 0
    }

    private get duration(): number {
        return Math.max(this.effect.duration ?? 20, 0)
    }

    /** Moves everything one tick on and draws the tick's emissions. */
    step(): void {
        for (const dot of this.dots) {
            dot.position = plus(dot.position, dot.velocity)
            dot.age++
        }
        this.dots.splice(0, this.dots.length, ...this.dots.filter(dot => dot.age < dot.life))
        this.pieces = (this.effect.pieces ?? []).map(piece => this.piece(piece)).filter((shown): shown is Shown => shown !== null)
        if (this.tick <= this.duration) (this.effect.emitters ?? []).forEach(emitter => this.emit(emitter))
        this.tick++
    }

    private emit(emitter: Emitter): void {
        const since = this.tick - (emitter.delay ?? 0)
        const every = Math.max(emitter.every ?? 1, 1)
        if (since < 0 || since % every !== 0 || this.random() >= (emitter.chance ?? 1)) return
        const own = emitter.anchor?.on === 'TARGET' ? this.target : this.source
        if (!own) return
        const other = own === this.source ? this.target : this.source
        const origin = emitter.anchor?.on === 'PIECE' ? this.pieces[emitter.anchor.piece ?? 0]?.position : point(own, emitter.anchor)
        if (!origin) return
        const far = other ? aimedPoint(other, emitter.anchor) : undefined
        const shape = shapeOf(emitter.shape)
        const frame = frameOf(origin, forward(shape.facing, own, origin, far))
        let base = inWorld(frame, emitter.offset ?? vec(0, 0, 0))
        if (emitter.motion && far) base = travelled(emitter.motion, base, far, since)
        const moment = {progress: this.duration === 0 ? 1 : this.tick / this.duration, emission: Math.floor(since / every), tick: this.tick}
        const points = betweenEnds(shape)
            ? between(shape, base, far ?? inWorld({...frame, origin: base}, shape.to), moment, this.random)
            : around(shape, {...frame, origin: base}, moment)
        for (const at of points) this.spawn(emitter, at)
    }

    private spawn(emitter: Emitter, at: Vec): void {
        const particle = emitter.particle ?? 'minecraft:flame'
        const kind = this.kinds[particle] ?? 'NONE'
        const spread = emitter.spread ?? vec(0, 0, 0)
        const speed = emitter.speed ?? 0
        for (let index = 0; index < Math.max(emitter.count ?? 1, 1); index++) {
            const jitter = vec(gauss(this.random) * spread.x, gauss(this.random) * spread.y, gauss(this.random) * spread.z)
            const drift = times(normalized(vec(this.random() - 0.5, this.random() - 0.5, this.random() - 0.5), vec(0, 1, 0)), speed)
            const colored = ['DUST', 'DUST_TRANSITION', 'SPELL', 'COLOR', 'TRAIL'].includes(kind)
            this.dots.push({
                position: plus(at, jitter),
                velocity: drift,
                particle,
                color: colored ? emitter.data?.color ?? '#ffffff' : undefined,
                fade: kind === 'DUST_TRANSITION' ? emitter.data?.to : undefined,
                size: kind === 'DUST' || kind === 'DUST_TRANSITION' ? emitter.data?.size ?? 1 : 1,
                age: 0,
                life: LIFE,
            })
        }
    }

    private piece(piece: Piece): Shown | null {
        const own = piece.anchor?.on === 'TARGET' && this.target ? this.target : this.source
        const other = own === this.source ? this.target : this.source
        const anchor = point(own, piece.anchor)
        const far = other ? aimedPoint(other, piece.anchor) : undefined
        const start = inWorld(frameOf(anchor, forward(piece.facing ?? 'PATH', own, anchor, far)), piece.offset ?? vec(0, 0, 0))
        if (piece.kind === 'SPAN') {
            if (!far || this.tick > this.duration) return null
            const reach = (piece.grow ?? 0) === 0 ? 1 : Math.min(1, this.tick / (piece.grow ?? 1))
            return {position: start, end: plus(start, times(minus(far, start), reach)), piece}
        }
        const motion = piece.motion
        if (!motion || !far) return this.tick <= Math.max(this.duration, 1) ? {position: start, end: undefined, piece} : null
        const distance = length(minus(far, start))
        const ticks = Math.max(motion.ticks ?? 10, 1)
        const flight = ticks + Math.ceil(ticks * (motion.through ?? 0) / Math.max(distance, 1e-6))
        if (this.tick > flight + (piece.stick ?? 20)) return null
        return {position: travelled(motion, start, far, this.tick), end: far, piece}
    }
}

/**
 * @returns where an anchor is on a figure
 */
export function point(body: Body, anchor: Anchor | undefined): Vec {
    const lift = {FEET: 0, CENTER: body.height / 2, EYES: body.height * EYES, ABOVE: body.height + ABOVE}[anchor?.at ?? 'CENTER']
    return plus(body.feet, vec(0, lift, 0))
}

/**
 * @returns where an anchor aims at on the other end's figure: its own aim point, else its own point
 */
export function aimedPoint(body: Body, anchor: Anchor | undefined): Vec {
    return point(body, anchor?.aim ? {...anchor, at: anchor.aim} : anchor)
}

function forward(face: Facing, body: Body, from: Vec, other: Vec | undefined): Vec {
    const ahead = facing(body.yaw, 0)
    if (face === 'PATH' && other) return normalized(minus(other, from), ahead)
    return ahead
}

function gauss(random: () => number): number {
    return (random() + random() + random() - 1.5) / 1.5
}
