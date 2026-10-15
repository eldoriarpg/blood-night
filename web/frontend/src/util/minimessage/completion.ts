/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {COLORS} from '~/util/minimessage/preview'

/** The tags offered while typing, those taking arguments ending in `:`. */
export const TAGS: readonly string[] = [
    ...Object.keys(COLORS).filter(name => !name.includes('grey')),
    'bold', 'italic', 'underlined', 'strikethrough', 'obfuscated',
    'color:', 'gradient:', 'rainbow', 'reset', 'newline',
]

/** A completed text and where the caret goes in it. */
export interface Completed {
    text: string
    caret: number
}

/**
 * The tags the tag being typed before the caret could become: any known tag or placeholder after `<`, and after `</`
 * the tags still open, the innermost first.
 *
 * @param text the whole text
 * @param caret where the caret is
 * @param placeholders placeholders the text may use, e.g. `name`
 * @returns the typed part and the tags it could become, or undefined where no tag is being typed
 */
export function tagCandidates(text: string, caret: number, placeholders: readonly string[] = []): {typed: string, closing: boolean, candidates: string[]} | undefined {
    const match = /<(\/?)([a-z_]*)$/i.exec(text.slice(0, caret))
    if (!match) return undefined
    const closing = match[1] === '/'
    const typed = (match[2] ?? '').toLowerCase()
    const pool = closing ? openTags(text.slice(0, match.index)) : [...TAGS, ...placeholders]
    return {typed, closing, candidates: [...new Set(pool)].filter(tag => tag.startsWith(typed))}
}

/**
 * Completes the tag being typed before the caret: to the only tag it can become, closed with `>` unless it takes
 * arguments; to what all candidates share where that adds something; else to the first candidate.
 *
 * @returns the completed text, or undefined where there is nothing to complete
 */
export function completeTag(text: string, caret: number, placeholders: readonly string[] = []): Completed | undefined {
    const found = tagCandidates(text, caret, placeholders)
    if (!found || found.candidates.length === 0) return undefined
    const shared = commonPrefix(found.candidates)
    const tag = found.candidates.length > 1 && shared.length > found.typed.length ? shared : found.candidates[0] ?? ''
    const whole = found.candidates.includes(tag) && !tag.endsWith(':')
    const insert = tag.slice(found.typed.length) + (whole ? '>' : '')
    if (insert === '') return undefined
    return {text: text.slice(0, caret) + insert + text.slice(caret), caret: caret + insert.length}
}

function openTags(text: string): string[] {
    const open: string[] = []
    for (const match of text.matchAll(/<(\/?)([a-z_#0-9]+)[^>]*>/gi)) {
        const tag = (match[2] ?? '').toLowerCase()
        if (match[1] === '/') {
            const index = open.lastIndexOf(tag)
            if (index >= 0) open.splice(index, 1)
        } else if (tag !== 'reset' && tag !== 'newline') {
            open.push(tag)
        }
    }
    return open.reverse()
}

function commonPrefix(words: readonly string[]): string {
    return words.reduce((shared, word) => {
        let length = 0
        while (length < shared.length && shared[length] === word[length]) length++
        return shared.slice(0, length)
    })
}
