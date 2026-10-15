/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/**
 * @param range a range as the settings store it, possibly missing or with its ends swapped
 * @returns the range as a slider takes it, lower end first; a missing end is 1, Minecraft's normal pitch and volume
 */
export function rangeOf(range: {min?: number, max?: number} | undefined): [number, number] {
    const min = range?.min ?? 1
    const max = range?.max ?? 1
    return [Math.min(min, max), Math.max(min, max)]
}

/**
 * @param range a range, lower end first
 * @returns a value within it, the way the plugin picks one each time a sound plays
 */
export function randomIn(range: readonly [number, number]): number {
    return range[0] + Math.random() * (range[1] - range[0])
}
