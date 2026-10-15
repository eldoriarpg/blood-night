/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/** Ticks in a Minecraft day. */
export const DAY_TICKS = 24000

/**
 * @param ticks ticks after sunrise, Minecraft's time of day
 * @returns the time on Minecraft's clock, which shows 06:00 at sunrise, e.g. `19:30` for 13500
 */
export function clockTime(ticks: number): string {
    const minutes = Math.round((((ticks % DAY_TICKS) + DAY_TICKS) % DAY_TICKS) * 60 / 1000) + 6 * 60
    const hours = Math.floor(minutes / 60) % 24
    return `${String(hours).padStart(2, '0')}:${String(minutes % 60).padStart(2, '0')}`
}

/**
 * @param clock a time on Minecraft's clock, e.g. `19:30`
 * @returns ticks after sunrise, or none for a time that is not one
 */
export function ticksOf(clock: string): number | undefined {
    const match = /^(\d{1,2}):(\d{2})$/.exec(clock.trim())
    if (!match) return undefined
    const hours = Number(match[1])
    const minutes = Number(match[2])
    if (hours > 23 || minutes > 59) return undefined
    return ((((hours - 6) * 60 + minutes) * 1000 / 60) % DAY_TICKS + DAY_TICKS) % DAY_TICKS
}
