/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {abbreviates, matchWord, searchTexts, wordsOf} from '~/util/search/textMatch'

describe('text match', () => {
    it('finds words by the starts of the words they stand for', () => {
        const sword = wordsOf('Diamond Sword')
        expect(['ds', 'dsword', 'diasw', 'diamondsword', 'sword'].filter(word => abbreviates(word, sword))).toHaveLength(5)
        expect(abbreviates('sd', sword)).toBe(false)
        expect(abbreviates('dx', sword)).toBe(false)
        expect(abbreviates('gapple', wordsOf('Enchanted Golden Apple'))).toBe(true)
        expect(abbreviates('ds', wordsOf('minecraft:diamond_sword'))).toBe(true)
    })

    it('tells contained words from abbreviated ones', () => {
        expect(matchWord('dia', 'Diamond Sword')).toBe('contained')
        expect(matchWord('dsword', 'Diamond Sword')).toBe('abbreviated')
        expect(matchWord('d-s', 'Diamond Sword')).toBe('abbreviated')
        expect(matchWord('xyz', 'Diamond Sword')).toBeNull()
    })

    it('ranks names starting with the search, then contained words, then abbreviations', () => {
        const names = ['Dark Oak Sign', 'Diamond Sword', 'Sword of Dawn', 'Stone']
        const search = (query: string) => searchTexts(names, name => [name], query)
        expect(search('ds')).toEqual(['Diamond Sword'])
        expect(search('dos')).toEqual(['Dark Oak Sign'])
        expect(search('dia sword')).toEqual(['Diamond Sword'])
        expect(search('DSword')).toEqual(['Diamond Sword'])
        expect(search('sword')).toEqual(['Sword of Dawn', 'Diamond Sword'])
        expect(search('')).toEqual(names)
    })
})
