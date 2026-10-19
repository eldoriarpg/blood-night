/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Motion, Shape} from '~/api/generated/payload'

/** A point or direction in blocks: x east, y up, z south. */
export interface Vec {
    x: number
    y: number
    z: number
}

/** Where a shape sits and how it is turned: x right, y up, z forward. */
export interface Frame {
    origin: Vec
    right: Vec
    up: Vec
    forward: Vec
}

/** When in a play a shape is drawn. */
export interface Moment {
    /** From 0 at the play's start to 1 at its end. */
    progress: number
    /** Emissions of the emitter before this one. */
    emission: number
    /** Ticks since the play started. */
    tick: number
}

/** The most points one shape has, as on the server. */
export const MAX_POINTS = 64
/** The longest beam or arc in blocks. */
export const MAX_SPAN = 32
/** The length of a beam or arc without its own end that the cost estimate assumes, since the play's ends are only known in game. */
export const ESTIMATED_SPAN = 16
/** The largest picture, in cells each way. */
export const MAX_CELLS = 16

const GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5))

export const vec = (x: number, y: number, z: number): Vec => ({x, y, z})
export const plus = (a: Vec, b: Vec): Vec => vec(a.x + b.x, a.y + b.y, a.z + b.z)
export const minus = (a: Vec, b: Vec): Vec => vec(a.x - b.x, a.y - b.y, a.z - b.z)
export const times = (a: Vec, factor: number): Vec => vec(a.x * factor, a.y * factor, a.z * factor)
export const length = (a: Vec): number => Math.hypot(a.x, a.y, a.z)
export const lerp = (a: Vec, b: Vec, share: number): Vec => plus(a, times(minus(b, a), share))
const cross = (a: Vec, b: Vec): Vec => vec(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x)

/**
 * @returns the vector with length 1, or the fallback when it has none
 */
export function normalized(a: Vec, fallback: Vec): Vec {
    const size = length(a)
    return size < 1e-9 ? fallback : times(a, 1 / size)
}

/**
 * @param yaw Minecraft yaw in degrees: 0 faces south, 90 west
 * @param pitch Minecraft pitch in degrees: negative looks up
 * @returns the direction faced
 */
export function facing(yaw: number, pitch: number): Vec {
    const yawRad = yaw * Math.PI / 180
    const pitchRad = pitch * Math.PI / 180
    const level = Math.cos(pitchRad)
    return vec(-Math.sin(yawRad) * level, -Math.sin(pitchRad), Math.cos(yawRad) * level)
}

/**
 * @param origin the centre
 * @param forward where it faces; straight up or down faces north instead of turning over
 * @returns an upright frame facing forward
 */
export function frameOf(origin: Vec, forward: Vec): Frame {
    const ahead = normalized(forward, vec(0, 0, 1))
    const hint = Math.abs(ahead.y) > 0.999 ? vec(0, 0, -Math.sign(ahead.y)) : vec(0, 1, 0)
    const right = normalized(cross(ahead, hint), vec(-1, 0, 0))
    const up = normalized(cross(right, ahead), vec(0, 1, 0))
    return {origin, right, up, forward: ahead}
}

/**
 * @returns a point given in the frame, in the world
 */
export function inWorld(frame: Frame, local: Vec): Vec {
    return plus(plus(plus(frame.origin, times(frame.right, local.x)), times(frame.up, local.y)), times(frame.forward, local.z))
}

/**
 * @returns the shape with every missing field at the server's default
 */
export function shapeOf(shape: Shape | undefined): Required<Omit<Shape, 'flap'>> & Pick<Shape, 'flap'> {
    return {
        type: shape?.type ?? 'POINT',
        radius: Math.max(0, shape?.radius ?? 1),
        points: Math.min(Math.max(shape?.points ?? 16, 1), MAX_POINTS),
        to: shape?.to ?? vec(0, 1, 0),
        height: shape?.height ?? 2,
        turns: shape?.turns ?? 2,
        strands: Math.min(Math.max(shape?.strands ?? 1, 1), 8),
        density: Math.min(Math.max(shape?.density ?? 4, 0.1), 16),
        jitter: Math.max(0, shape?.jitter ?? 0),
        bend: shape?.bend ?? 1,
        twist: shape?.twist ?? 0,
        rows: shape?.rows ?? [],
        scale: Math.max(0.01, shape?.scale ?? 0.15),
        mirror: shape?.mirror ?? false,
        flap: shape?.flap,
        spin: shape?.spin ?? 0,
        grow: shape?.grow ?? false,
        facing: shape?.facing ?? 'UP',
    }
}

/**
 * @returns whether the shape runs from source to target
 */
export function betweenEnds(shape: Shape | undefined): boolean {
    return shape?.type === 'BEAM' || shape?.type === 'ARC'
}

/**
 * @param given a shape that does not need both ends
 * @param frame where it sits and how it is turned
 * @param moment when in the play it is drawn
 * @returns its points in the world
 */
export function around(given: Shape | undefined, frame: Frame, moment: Moment): Vec[] {
    const shape = shapeOf(given)
    const size = shape.grow ? clamp01(moment.progress) : 1
    if (shape.type === 'LINE') return line(shape.points, shape.to, frame, size)
    const spin = shape.spin * moment.emission * Math.PI / 180
    return plane(shape, moment).map(point => {
        const turned = times(turn(point, spin), size)
        const local = shape.facing === 'UP' ? vec(turned.x, turned.z, turned.y) : turned
        return inWorld(frame, local)
    })
}

/**
 * @param given a beam or an arc
 * @param from the source end
 * @param to the target end
 * @param moment when in the play it is drawn
 * @param random where the jitter comes from
 * @returns its points in the world; none when the ends are too far apart
 */
export function between(given: Shape | undefined, from: Vec, to: Vec, moment: Moment, random: () => number = Math.random): Vec[] {
    const shape = shapeOf(given)
    const span = minus(to, from)
    const distance = length(span)
    if (distance < 1e-6 || distance > MAX_SPAN) return []
    const reach = shape.grow ? clamp01(moment.progress) : 1
    const count = Math.min(Math.max(Math.ceil(distance * reach * shape.density) + 1, 2), MAX_POINTS)
    const path = frameOf(from, span)
    const angle = (shape.twist + shape.spin * moment.emission) * Math.PI / 180
    const lift = plus(times(path.up, Math.cos(angle)), times(path.right, Math.sin(angle)))
    const control = plus(lerp(from, to, 0.5), times(lift, 2 * shape.bend))
    const points: Vec[] = []
    for (let index = 0; index < count; index++) {
        const share = reach * index / (count - 1)
        const point = shape.type === 'ARC' ? curve(from, control, to, share) : lerp(from, to, share)
        points.push(jitter(point, path, shape.jitter, random))
    }
    return points
}

/**
 * @returns the length a beam or arc is taken to have: its own end when it has one, else {@link ESTIMATED_SPAN}
 */
function spanOf(shape: Shape | undefined): number {
    const to = shape?.to
    return to ? Math.min(Math.hypot(to.x, to.y, to.z), MAX_SPAN) : ESTIMATED_SPAN
}

/**
 * @returns how many points the shape has, for limits and cost estimates
 */
export function pointCount(given: Shape | undefined): number {
    const shape = shapeOf(given)
    switch (shape.type) {
        case 'POINT': return 1
        case 'RING': case 'SPHERE': case 'LINE': return shape.points
        case 'SPIRAL': return Math.min(MAX_POINTS, shape.points * shape.strands)
        case 'BEAM': case 'ARC': return Math.min(Math.ceil(spanOf(given) * shape.density) + 1, MAX_POINTS)
        case 'PATTERN': return Math.min(MAX_POINTS, cells(shape.rows).length * (shape.mirror ? 2 : 1))
    }
}

/**
 * @returns the picture's cells holding a point, as column and row, cut to the largest picture
 */
export function cells(rows: readonly string[]): [number, number][] {
    const found: [number, number][] = []
    rows.slice(0, MAX_CELLS).forEach((line, row) => {
        for (let column = 0; column < Math.min(line.length, MAX_CELLS); column++) {
            const cell = line[column]
            if (cell !== '.' && cell !== ' ') found.push([column, row])
        }
    })
    return found
}

/**
 * @param motion how it travels
 * @param from where it starts
 * @param to the other end
 * @param elapsed ticks since it started
 * @returns where it is
 */
export function travelled(motion: Motion, from: Vec, to: Vec, elapsed: number): Vec {
    const ticks = Math.min(Math.max(motion.ticks ?? 10, 1), 600)
    const through = Math.max(0, motion.through ?? 0)
    const distance = length(minus(to, from))
    if (distance < 1e-6) return to
    const share = elapsed / ticks
    const furthest = 1 + through / distance
    const ease = motion.ease ?? 'LINEAR'
    const eased = (value: number) => ease === 'IN' ? value * value : ease === 'OUT' ? 1 - (1 - value) * (1 - value) : value
    return lerp(from, to, share <= 1 ? eased(Math.max(share, 0)) : Math.min(share, furthest))
}

function clamp01(value: number): number {
    return Math.min(Math.max(value, 0), 1)
}

function line(count: number, to: Vec, frame: Frame, size: number): Vec[] {
    const points: Vec[] = []
    for (let index = 0; index < count; index++) {
        const share = count === 1 ? 0 : index / (count - 1)
        points.push(inWorld(frame, times(to, share * size)))
    }
    return points
}

function plane(shape: ReturnType<typeof shapeOf>, moment: Moment): Vec[] {
    switch (shape.type) {
        case 'RING': return ring(shape.radius, shape.points)
        case 'SPHERE': return sphere(shape.radius, shape.points)
        case 'SPIRAL': return spiral(shape)
        case 'PATTERN': return pattern(shape, moment)
        default: return [vec(0, 0, 0)]
    }
}

function ring(radius: number, count: number): Vec[] {
    return Array.from({length: count}, (_, index) => {
        const angle = 2 * Math.PI * index / count
        return vec(radius * Math.cos(angle), radius * Math.sin(angle), 0)
    })
}

function sphere(radius: number, count: number): Vec[] {
    return Array.from({length: count}, (_, index) => {
        const axis = 1 - 2 * (index + 0.5) / count
        const across = Math.sqrt(1 - axis * axis)
        const angle = GOLDEN_ANGLE * index
        return times(vec(across * Math.cos(angle), across * Math.sin(angle), axis), radius)
    })
}

function spiral(shape: ReturnType<typeof shapeOf>): Vec[] {
    const perStrand = Math.max(1, Math.min(shape.points, Math.floor(MAX_POINTS / shape.strands)))
    const points: Vec[] = []
    for (let strand = 0; strand < shape.strands; strand++) {
        for (let index = 0; index < perStrand; index++) {
            const share = perStrand === 1 ? 0 : index / (perStrand - 1)
            const angle = 2 * Math.PI * (shape.turns * share + strand / shape.strands)
            points.push(vec(shape.radius * Math.cos(angle), shape.radius * Math.sin(angle), shape.height * share))
        }
    }
    return points
}

function pattern(shape: ReturnType<typeof shapeOf>, moment: Moment): Vec[] {
    const found = cells(shape.rows)
    const width = Math.max(0, ...found.map(([column]) => column)) + 1
    const height = Math.min(shape.rows.length, MAX_CELLS)
    const flap = shape.flap
    const swing = flap ? flap.degrees * Math.PI / 180 * Math.sin(2 * Math.PI * moment.tick / Math.max(2, flap.ticks)) : 0
    const points: Vec[] = []
    for (const [column, row] of found) {
        const up = ((height - 1) / 2 - row) * shape.scale
        if (shape.mirror) {
            const out = (column + 0.5) * shape.scale
            points.push(fold(out, up, swing), fold(-out, up, swing))
        } else {
            points.push(fold((column - (width - 1) / 2) * shape.scale, up, swing))
        }
        if (points.length >= MAX_POINTS) break
    }
    return points.slice(0, MAX_POINTS)
}

function fold(across: number, up: number, swing: number): Vec {
    const out = Math.abs(across)
    return vec(Math.sign(across) * out * Math.cos(swing), up, -out * Math.sin(swing))
}

function turn(point: Vec, angle: number): Vec {
    if (angle === 0) return point
    const cos = Math.cos(angle)
    const sin = Math.sin(angle)
    return vec(point.x * cos - point.y * sin, point.x * sin + point.y * cos, point.z)
}

function curve(from: Vec, control: Vec, to: Vec, share: number): Vec {
    const rest = 1 - share
    return plus(plus(times(from, rest * rest), times(control, 2 * rest * share)), times(to, share * share))
}

function jitter(point: Vec, path: Frame, amount: number, random: () => number): Vec {
    if (amount <= 0) return point
    const offset = () => (random() * 2 - 1) * amount
    return plus(plus(point, times(path.right, offset())), times(path.up, offset()))
}
