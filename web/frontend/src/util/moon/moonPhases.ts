/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/** The moon's phases in the order Minecraft's moon goes through them, starting at the full moon. */
export const MOON_PHASES = [
    'FULL_MOON', 'WANING_GIBBOUS', 'LAST_QUARTER', 'WANING_CRESCENT', 'NEW_MOON', 'WAXING_CRESCENT', 'FIRST_QUARTER', 'WAXING_GIBBOUS',
] as const

export type MoonPhaseName = typeof MOON_PHASES[number]

/**
 * The lit part of the moon in a phase, as an SVG path for a moon of radius `r` at the centre of a `2r` square: the
 * waxing moon is lit on the right, the waning one on the left, as seen from the north.
 *
 * @param phase the phase
 * @param r the moon's radius
 * @returns the path, empty at the new moon
 */
export function litPath(phase: MoonPhaseName, r: number): string {
    const index = MOON_PHASES.indexOf(phase)
    const lit = Math.abs(index - 4) / 4
    if (lit === 0) return ''
    if (lit === 1) return `M ${r} 0 A ${r} ${r} 0 1 1 ${r} ${2 * r} A ${r} ${r} 0 1 1 ${r} 0 Z`
    const waxing = index > 4
    const terminator = r * Math.abs(1 - 2 * lit)
    const outer = waxing ? 1 : 0
    const inner = (lit > 0.5) === waxing ? 1 : 0
    return `M ${r} 0 A ${r} ${r} 0 0 ${outer} ${r} ${2 * r} A ${terminator} ${r} 0 0 ${inner} ${r} 0 Z`
}
