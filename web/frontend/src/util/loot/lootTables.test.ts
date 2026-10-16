/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {EditorPayload, LootEntry, LootTable} from '~/api/generated/payload'
import {
    amountChances,
    entryShares,
    exceedsStack,
    mobLevels,
    pool,
    removeTable,
    roll,
    simulate,
    stackSize,
    slotsOf,
    tableId,
    usesOf,
    usualAmount,
    withinStack,
    type LootLevel,
} from '~/util/loot/lootTables'

function entry(item: string, weight = 1, repeatable = false): LootEntry {
    return {item, weight, amount: {mode: 'RANGE', min: 1, max: 1, choices: {}}, repeatable}
}

function table(...entries: LootEntry[]): LootTable {
    return {name: 'Table', entries}
}

function level(source: LootLevel['source'], tables: string[], slots: Record<string, number> | null = null, replaceSame = false): LootLevel {
    return {source, tables, slots, replaceSame}
}

function sequence(...values: number[]): () => number {
    let index = 0
    return () => values[index++ % values.length]!
}

const payload = {
    mobs: {zombie: {loot: {tables: ['gems', 'bones'], slots: {1: 1}, inherit: true, replaceSame: true}}},
    lootTables: {gems: table(entry('minecraft:diamond')), bones: table(entry('minecraft:bone'))},
    settings: {
        main: {loot: {tables: ['gems'], slots: {1: 9}, ordinary: {tables: [], slots: {1: 1}}}},
        worlds: {w: {name: 'world', settings: {loot: {tables: [], inherit: false, ordinary: {tables: ['bones']}}}}},
    },
} as unknown as EditorPayload

describe('tableId', () => {
    it('makes an id from the name and numbers taken ones', () => {
        expect(tableId('Zombie Scraps!', [])).toBe('zombie_scraps')
        expect(tableId('Gems', ['gems', 'gems_2'])).toBe('gems_3')
        expect(tableId('???', [])).toBe('table')
    })
})

describe('uses', () => {
    it('finds and removes every assignment', () => {
        expect(usesOf(payload, 'bones').map(use => `${use.kind}:${use.id}:${use.ordinary}`)).toEqual(['mob:zombie:false', 'world:w:true'])
        expect(removeTable(payload, 'gems')).toEqual([
            {op: 'replace', path: '/mobs/zombie/loot/tables', value: ['bones']},
            {op: 'replace', path: '/settings/main/loot/tables', value: []},
            {op: 'remove', path: '/lootTables/gems'},
        ])
    })
})

describe('shares and amounts', () => {
    it('gives each entry its share of the table', () => {
        expect(entryShares(table(entry('a', 1), entry('b', 3), entry('c', 0)))).toEqual([0.25, 0.75, 0])
    })

    it('reads ranges and weighted amounts', () => {
        expect(amountChances({mode: 'RANGE', min: 2, max: 4, choices: {}}).map(choice => choice.amount)).toEqual([2, 3, 4])
        expect(amountChances({mode: 'CHOICES', min: 1, max: 1, choices: {10: 1, 20: 3}})).toEqual([{amount: 10, chance: 0.25}, {amount: 20, chance: 0.75}])
        expect(usualAmount({mode: 'CHOICES', min: 1, max: 1, choices: {10: 1, 20: 3}})).toBe(17.5)
    })
})

describe('pool', () => {
    const tables = {mob: table(entry('diamond', 5)), server: table(entry('diamond', 1), entry('gold', 1))}

    it('lets a replacing level take the same items out of broader ones', () => {
        expect(pool([level('mob', ['mob'], null, true), level('server', ['server'])], tables).map(entry => `${entry.table}:${entry.entry.item}`))
            .toEqual(['mob:diamond', 'server:gold'])
        expect(pool([level('mob', ['mob']), level('server', ['server'])], tables)).toHaveLength(3)
    })

    it('counts a table once and skips unknown ones', () => {
        expect(pool([level('mob', ['server', 'gone']), level('server', ['server'])], tables)).toHaveLength(2)
    })

    it('takes the slots of the most specific level that sets them', () => {
        expect(slotsOf([level('mob', []), level('world', [], {2: 1}), level('server', [], {1: 1})])).toEqual({slots: {2: 1}, source: 'world'})
        expect(slotsOf([level('mob', [])]).source).toBeNull()
    })
})

describe('roll', () => {
    it('picks entries once unless they repeat', () => {
        const tables = {once: table(entry('a')), again: table(entry('b', 1, true))}
        expect(roll([level('mob', ['once'], {3: 1})], tables, sequence(0))).toHaveLength(1)
        expect(roll([level('mob', ['again'], {3: 1})], tables, sequence(0))).toHaveLength(3)
    })

    it('simulates the chance of each entry in a kill', () => {
        const chances = simulate([level('mob', ['gems'], {1: 1})], {gems: table(entry('a'), entry('b'))}, 2, sequence(0, 0, 0, 0, 0.9, 0))
        expect(chances).toEqual(new Map([['gems/0', 0.5], ['gems/1', 0.5]]))
    })
})

describe('mobLevels', () => {
    it('stops where a level does not inherit', () => {
        expect(mobLevels(payload, 'zombie', undefined).map(entry => entry.source)).toEqual(['mob', 'world', 'server'])
        expect(mobLevels(payload, 'zombie', 'w').map(entry => entry.source)).toEqual(['mob', 'world'])
    })
})

describe('stack sizes', () => {
    const stacked = {
        items: {blade: {kind: 'REGISTERED', material: 'minecraft:stick', lore: [], enchantments: {}, stackSize: 1}, wand: {kind: 'DEFINED', material: 'minecraft:diamond_sword', lore: [], enchantments: {}}},
        choices: {materialInfo: {'minecraft:diamond_sword': {stackSize: 1}, 'minecraft:ender_pearl': {stackSize: 16}}},
    } as unknown as EditorPayload

    it('finds an item\'s stack size', () => {
        expect(stackSize('blade', stacked)).toBe(1)
        expect(stackSize('wand', stacked)).toBe(1)
        expect(stackSize('minecraft:ender_pearl', stacked)).toBe(16)
        expect(stackSize('minecraft:stone', stacked)).toBe(64)
    })

    it('keeps amounts within one stack', () => {
        expect(withinStack({mode: 'RANGE', min: 4, max: 40, choices: {}}, 16)).toEqual({mode: 'RANGE', min: 4, max: 16, choices: {}})
        expect(withinStack({mode: 'CHOICES', min: 1, max: 1, choices: {8: 3, 32: 1}}, 16).choices).toEqual({8: 3})
        expect(withinStack({mode: 'CHOICES', min: 1, max: 1, choices: {5: 1}}, 1).choices).toEqual({1: 1})
        expect(exceedsStack({mode: 'RANGE', min: 1, max: 3, choices: {}}, 1)).toBe(true)
        expect(exceedsStack({mode: 'RANGE', min: 1, max: 1, choices: {}}, 1)).toBe(false)
    })
})
