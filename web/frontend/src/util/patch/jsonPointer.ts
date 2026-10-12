/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/**
 * Builds a JSON pointer (RFC 6901) from its segments, escaping `~` and `/`.
 *
 * @param segments the path's segments, numbers for list indexes
 * @returns the pointer, `""` for the whole document
 */
export function pointer(...segments: (string | number)[]): string {
    return segments.map(segment => '/' + String(segment).replaceAll('~', '~0').replaceAll('/', '~1')).join('')
}

/**
 * Splits a JSON pointer into its unescaped segments.
 *
 * @param path the pointer
 * @returns the segments, empty for the whole document
 */
export function segments(path: string): string[] {
    if (path === '') return []
    if (!path.startsWith('/')) throw new PatchError(`Bad pointer ${path}`)
    return path.slice(1).split('/').map(segment => segment.replaceAll('~1', '/').replaceAll('~0', '~'))
}

/** A patch operation that does not fit the document. */
export class PatchError extends Error {}
