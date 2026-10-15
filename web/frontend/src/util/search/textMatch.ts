/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/** How a searched word was found in a text: inside it, or as the starts of its words in a row. */
export type WordMatch = 'contained' | 'abbreviated'

const WORD_BREAK = /[^\p{L}\p{N}]+/u

/**
 * @param text a text, e.g. `Diamond Sword` or `minecraft:diamond_sword`
 * @returns its words in lower case
 */
export function wordsOf(text: string): string[] {
    return text.toLowerCase().split(WORD_BREAK).filter(word => word !== '')
}

/**
 * Whether a searched word is made of the starts of words of a text that follow each other, beginning at any word:
 * `ds`, `dsword` and `diasw` all abbreviate `Diamond Sword`, `gapple` abbreviates `Enchanted Golden Apple`.
 *
 * @param word the searched word, in lower case
 * @param words the text's words, in lower case
 * @returns whether it abbreviates them
 */
export function abbreviates(word: string, words: readonly string[]): boolean {
    const tried = new Set<string>()
    const from = (position: number, index: number): boolean => {
        if (position === word.length) return true
        if (index >= words.length) return false
        const key = `${position}:${index}`
        if (tried.has(key)) return false
        tried.add(key)
        const current = words[index]!
        for (let length = Math.min(current.length, word.length - position); length >= 1; length--) {
            if (word.startsWith(current.slice(0, length), position) && from(position + length, index + 1)) return true
        }
        return false
    }
    return words.some((_, index) => from(0, index))
}

/**
 * @param word a searched word, in lower case
 * @param text a text to search
 * @returns how the word is found in it, none when it is not
 */
export function matchWord(word: string, text: string): WordMatch | null {
    const lower = text.toLowerCase()
    if (lower.includes(word)) return 'contained'
    return abbreviates(word.replace(WORD_BREAK, ''), wordsOf(lower)) ? 'abbreviated' : null
}

/**
 * Searches entries by the texts each has: every word of the search must be found in one of an entry's texts, inside
 * it or abbreviating it (see {@link abbreviates}). Entries whose names start with the search come first, then those
 * holding every word, then those only abbreviated; the order is kept otherwise.
 *
 * @param entries the entries
 * @param texts the texts of an entry, its names first
 * @param query what was typed
 * @param names how many of the first texts are names, which rank an entry first when they start with the search
 * @returns the matching entries
 */
export function searchTexts<T>(entries: readonly T[], texts: (entry: T) => readonly string[], query: string, names = 1): T[] {
    const needle = query.trim().toLowerCase()
    if (needle === '') return [...entries]
    const words = needle.split(/\s+/)
    const ranked: T[][] = [[], [], []]
    for (const entry of entries) {
        const own = texts(entry)
        const found = words.map(word => {
            const kinds = own.map(text => matchWord(word, text))
            if (kinds.includes('contained')) return 'contained'
            return kinds.includes('abbreviated') ? 'abbreviated' : null
        })
        if (found.includes(null)) continue
        const named = own.slice(0, names).some(name => name.toLowerCase().startsWith(needle))
        const rank = named ? 0 : found.every(kind => kind === 'contained') ? 1 : 2
        ranked[rank]!.push(entry)
    }
    return ranked.flat()
}
