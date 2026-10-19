/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Body, Dot, Shown} from '~/util/particles/preview'
import type {Vec} from '~/util/particles/shapes'

/** Where the camera looks from: around the scene's centre, tilted down, at a scale in pixels per block. */
export interface Camera {
    centre: Vec
    /** Turn about the up axis, in radians. */
    angle: number
    /** Pixels per block. */
    scale: number
    width: number
    height: number
}

/** Images by URL, drawn once loaded. */
export interface Images {
    image(url: string | undefined): HTMLImageElement | undefined
    tinted(url: string | undefined, color: string): HTMLCanvasElement | HTMLImageElement | undefined
}

/** What one frame shows. */
export interface Scene {
    bodies: Body[]
    dots: readonly Dot[]
    pieces: readonly Shown[]
    sprite: (particle: string) => string | undefined
    icon: (item: string | undefined) => string | undefined
}

const TILT = 0.45
const DEPTH = 18
const GRID = 8

/**
 * @returns where a point shows on the canvas, and how much nearer points are drawn larger
 */
export function project(camera: Camera, point: Vec): {x: number, y: number, depth: number, zoom: number} {
    const dx = point.x - camera.centre.x
    const dy = point.y - camera.centre.y
    const dz = point.z - camera.centre.z
    const cos = Math.cos(camera.angle)
    const sin = Math.sin(camera.angle)
    const x = dx * cos - dz * sin
    const z = dx * sin + dz * cos
    const y = dy * Math.cos(TILT) - z * Math.sin(TILT)
    const depth = dy * Math.sin(TILT) + z * Math.cos(TILT)
    const zoom = camera.scale * DEPTH / Math.max(DEPTH + depth, 1)
    return {x: camera.width / 2 + x * zoom, y: camera.height / 2 - y * zoom, depth, zoom}
}

/**
 * Draws the ground, the figures, the pieces and the particles, the farthest first.
 */
export function draw(context: CanvasRenderingContext2D, camera: Camera, frame: Scene, images: Images, ink: string): void {
    context.clearRect(0, 0, camera.width, camera.height)
    ground(context, camera, ink)
    for (const body of frame.bodies) figure(context, camera, body, ink)
    for (const shown of frame.pieces) piece(context, camera, shown, images, frame.icon)
    const sorted = [...frame.dots].sort((a, b) => project(camera, b.position).depth - project(camera, a.position).depth)
    for (const dot of sorted) particle(context, camera, dot, images, frame.sprite(dot.particle))
    context.globalAlpha = 1
}

function ground(context: CanvasRenderingContext2D, camera: Camera, ink: string): void {
    context.strokeStyle = ink
    context.globalAlpha = 0.12
    context.lineWidth = 1
    for (let step = -GRID; step <= GRID; step++) {
        line(context, camera, {x: camera.centre.x + step, y: 0, z: camera.centre.z - GRID}, {x: camera.centre.x + step, y: 0, z: camera.centre.z + GRID})
        line(context, camera, {x: camera.centre.x - GRID, y: 0, z: camera.centre.z + step}, {x: camera.centre.x + GRID, y: 0, z: camera.centre.z + step})
    }
    context.globalAlpha = 1
}

function figure(context: CanvasRenderingContext2D, camera: Camera, body: Body, ink: string): void {
    context.strokeStyle = ink
    context.globalAlpha = 0.5
    context.lineWidth = 1.5
    const half = 0.3
    const corners = [[-half, -half], [half, -half], [half, half], [-half, half]] as const
    corners.forEach(([x, z], index) => {
        const [nextX, nextZ] = corners[(index + 1) % corners.length] ?? corners[0]
        const at = (dx: number, dz: number, dy: number) => ({x: body.feet.x + dx, y: body.feet.y + dy, z: body.feet.z + dz})
        line(context, camera, at(x, z, 0), at(nextX, nextZ, 0))
        line(context, camera, at(x, z, body.height), at(nextX, nextZ, body.height))
        line(context, camera, at(x, z, 0), at(x, z, body.height))
    })
    context.globalAlpha = 1
}

function piece(
    context: CanvasRenderingContext2D, camera: Camera, shown: Shown, images: Images, icon: (item: string | undefined) => string | undefined,
): void {
    const at = project(camera, shown.position)
    const color = shown.piece.glow ? shown.piece.color ?? '#ffffff' : '#c03030'
    if (shown.piece.kind === 'SPAN' && shown.end) {
        const end = project(camera, shown.end)
        context.strokeStyle = color
        context.globalAlpha = 0.8
        context.lineWidth = Math.max(2, (shown.piece.width ?? 0.1) * at.zoom)
        context.beginPath()
        context.moveTo(at.x, at.y)
        context.lineTo(end.x, end.y)
        context.stroke()
        context.globalAlpha = 1
        return
    }
    const size = 0.7 * (shown.piece.scale ?? 1) * at.zoom
    const image = images.image(icon(shown.piece.kind === 'ITEM' ? shown.piece.item : shown.piece.block))
    if (shown.piece.glow) {
        context.shadowColor = color
        context.shadowBlur = 8
    }
    if (image) context.drawImage(image, at.x - size / 2, at.y - size / 2, size, size)
    else {
        context.fillStyle = color
        context.fillRect(at.x - size / 4, at.y - size / 4, size / 2, size / 2)
    }
    context.shadowBlur = 0
}

function particle(context: CanvasRenderingContext2D, camera: Camera, dot: Dot, images: Images, sprite: string | undefined): void {
    const at = project(camera, dot.position)
    const share = dot.age / dot.life
    const size = Math.max(3, 0.25 * dot.size * at.zoom)
    const color = dot.fade && dot.color ? mix(dot.color, dot.fade, share) : dot.color
    const image = color ? images.tinted(sprite, color) : images.image(sprite)
    context.globalAlpha = Math.max(0, 1 - share * share)
    if (image) {
        context.drawImage(image, at.x - size / 2, at.y - size / 2, size, size)
        return
    }
    context.fillStyle = color ?? '#f0e0a0'
    context.fillRect(at.x - size / 4, at.y - size / 4, size / 2, size / 2)
}

function line(context: CanvasRenderingContext2D, camera: Camera, from: Vec, to: Vec): void {
    const start = project(camera, from)
    const end = project(camera, to)
    context.beginPath()
    context.moveTo(start.x, start.y)
    context.lineTo(end.x, end.y)
    context.stroke()
}

/**
 * @returns the colour a share of the way from one `#rrggbb` colour to another
 */
export function mix(from: string, to: string, share: number): string {
    const channel = (color: string, at: number) => Number.parseInt(color.slice(at, at + 2), 16) || 0
    const parts = [1, 3, 5].map(at => Math.round(channel(from, at) + (channel(to, at) - channel(from, at)) * share))
    return `#${parts.map(part => part.toString(16).padStart(2, '0')).join('')}`
}

/**
 * @returns images loaded on first use and grey sprites tinted to a colour, each kept once made
 */
export function imageCache(): Images {
    const images = new Map<string, HTMLImageElement>()
    const tints = new Map<string, HTMLCanvasElement>()

    function image(url: string | undefined): HTMLImageElement | undefined {
        if (!url) return undefined
        let found = images.get(url)
        if (!found) {
            found = new Image()
            found.src = url
            images.set(url, found)
        }
        return found.complete && found.naturalWidth > 0 ? found : undefined
    }

    function tinted(url: string | undefined, color: string): HTMLCanvasElement | HTMLImageElement | undefined {
        const source = image(url)
        if (!source || !url) return undefined
        const key = `${url}|${color}`
        const known = tints.get(key)
        if (known) return known
        const canvas = document.createElement('canvas')
        canvas.width = source.naturalWidth
        canvas.height = source.naturalHeight
        const context = canvas.getContext('2d')
        if (!context) return source
        context.imageSmoothingEnabled = false
        context.drawImage(source, 0, 0)
        context.globalCompositeOperation = 'multiply'
        context.fillStyle = color
        context.fillRect(0, 0, canvas.width, canvas.height)
        context.globalCompositeOperation = 'destination-in'
        context.drawImage(source, 0, 0)
        tints.set(key, canvas)
        return canvas
    }

    return {image, tinted}
}
