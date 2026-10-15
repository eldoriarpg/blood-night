/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/** The curve night selection's settings: chance on a cycle's first night, chance halfway, nights per cycle. */
export interface CurveSettings {
    min: number
    max: number
    period: number
}

/**
 * The chance of a blood night on each of the first {@code nights} nights, as the plugin's {@code Curve} computes it:
 * cosine eased from {@code min} up to {@code max} halfway through the cycle and back. A period below 2 counts as 2.
 */
export function curveChances(curve: CurveSettings, nights: number): number[] {
    const length = Math.max(Math.round(curve.period), 2)
    const half = length / 2
    const smooth = (from: number, to: number, progress: number) => from + (to - from) * (1 - Math.cos(Math.PI * progress)) / 2
    return Array.from({length: Math.max(nights, 0)}, (_, night) => {
        const position = night % length
        const value = position <= half
            ? smooth(curve.min, curve.max, position / half)
            : smooth(curve.max, curve.min, (position - half) / (length - half))
        return Math.min(100, Math.max(0, Math.trunc(value)))
    })
}
