/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorPayload, LootAmount, LootEntry, LootTable} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import type {PatchOperation} from '~/util/patch/jsonPatch'

/** A loot table of the payload with its id. */
export interface TableEntry {
    id: string
    table: LootTable
}

/** Where a loot table is assigned: a mob, a world or the server, for special mobs or for ordinary enemies. */
export interface TableUse {
    kind: 'mob' | 'world' | 'server'
    /** The mob's id or the world's UID; empty for the server. */
    id: string
    ordinary: boolean
    /** The pointer to the list of table ids that holds it. */
    path: string
    tables: readonly string[]
}

/** One level of a kill's loot: the mob, its world or the server. */
export interface LootLevel {
    source: 'mob' | 'world' | 'server'
    tables: readonly string[]
    slots: Readonly<Record<string, number>> | null | undefined
    replaceSame: boolean
}

/** An entry in the pool of a kill, with the table it came from. */
export interface Pooled {
    table: string
    index: number
    entry: LootEntry
}

/** An entry picked in a kill, with the amount it rolled. */
export interface Rolled extends Pooled {
    amount: number
}

/** A source of chance: a number from 0 (inclusive) to 1 (exclusive). */
export type Random = () => number

const ID_CHARACTERS = /[^a-z0-9]+/g

/**
 * @param payload the payload
 * @returns its loot tables, sorted by name
 */
export function tableEntries(payload: EditorPayload | null | undefined): TableEntry[] {
    return Object.entries(payload?.lootTables ?? {})
        .map(([id, table]) => ({id, table}))
        .sort((a, b) => (a.table.name || a.id).localeCompare(b.table.name || b.id))
}

/**
 * @param name a new table's name
 * @param taken the ids in use
 * @returns an id made from the name: lower case letters, digits and underscores, numbered when taken
 */
export function tableId(name: string, taken: readonly string[]): string {
    const base = name.trim().toLowerCase().replaceAll(ID_CHARACTERS, '_').replaceAll(/^_+|_+$/g, '') || 'table'
    let id = base
    for (let number = 2; taken.includes(id); number++) id = `${base}_${number}`
    return id
}

/**
 * @param name the table's name
 * @returns a table without entries
 */
export function newTable(name: string): LootTable {
    return {name: name.trim(), entries: []}
}

/**
 * @param item the item id or material key
 * @returns an entry dropping one of the item, as likely as other new entries, at most once per kill
 */
export function newEntry(item: string): LootEntry {
    return {item, weight: 10, amount: {mode: 'RANGE', min: 1, max: 1, choices: {}}, repeatable: false}
}

/**
 * @param payload the payload
 * @param id the new table's id
 * @param table the new table
 * @returns the operations adding it
 */
export function addTable(payload: EditorPayload, id: string, table: LootTable): PatchOperation[] {
    const operations: PatchOperation[] = payload.lootTables ? [] : [{op: 'add', path: '/lootTables', value: {}}]
    return [...operations, {op: 'add', path: pointer('lootTables', id), value: table}]
}

/**
 * @param payload the payload
 * @returns every list of assigned tables: each mob's, each world's and the server's, for special mobs and for
 *     ordinary enemies
 */
export function assignments(payload: EditorPayload | null | undefined): TableUse[] {
    const uses: TableUse[] = []
    for (const [id, mob] of Object.entries(payload?.mobs ?? {})) {
        uses.push({kind: 'mob', id, ordinary: false, path: pointer('mobs', id, 'loot', 'tables'), tables: mob.loot?.tables ?? []})
    }
    for (const [uid, world] of Object.entries(payload?.settings?.worlds ?? {})) {
        const loot = world.settings?.loot
        uses.push({kind: 'world', id: uid, ordinary: false, path: pointer('settings', 'worlds', uid, 'settings', 'loot', 'tables'), tables: loot?.tables ?? []})
        uses.push({kind: 'world', id: uid, ordinary: true, path: pointer('settings', 'worlds', uid, 'settings', 'loot', 'ordinary', 'tables'), tables: loot?.ordinary?.tables ?? []})
    }
    const server = payload?.settings?.main?.loot
    if (server) {
        uses.push({kind: 'server', id: '', ordinary: false, path: pointer('settings', 'main', 'loot', 'tables'), tables: server.tables ?? []})
        uses.push({kind: 'server', id: '', ordinary: true, path: pointer('settings', 'main', 'loot', 'ordinary', 'tables'), tables: server.ordinary?.tables ?? []})
    }
    return uses
}

/**
 * @param payload the payload
 * @param id a table's id
 * @returns where it is assigned
 */
export function usesOf(payload: EditorPayload | null | undefined, id: string): TableUse[] {
    return assignments(payload).filter(use => use.tables.includes(id))
}

/**
 * @param payload the payload
 * @param id the table's id
 * @returns the operations deleting the table and taking it out of every list that assigns it
 */
export function removeTable(payload: EditorPayload, id: string): PatchOperation[] {
    return [
        ...usesOf(payload, id).map(use => ({op: 'replace' as const, path: use.path, value: use.tables.filter(table => table !== id)})),
        {op: 'remove', path: pointer('lootTables', id)},
    ]
}

/**
 * @param table a table
 * @returns each entry's chance to fill a slot, from 0 to 1, in the order of the entries
 */
export function entryShares(table: LootTable): number[] {
    const total = table.entries.reduce((sum, entry) => sum + Math.max(0, entry.weight), 0)
    return table.entries.map(entry => total > 0 ? Math.max(0, entry.weight) / total : 0)
}

/**
 * @param amount an entry's amount
 * @returns the amounts it can roll, each with its chance from 0 to 1
 */
export function amountChances(amount: LootAmount): {amount: number, chance: number}[] {
    if (amount.mode === 'CHOICES') {
        const choices = Object.entries(amount.choices)
            .map(([value, weight]) => ({amount: Number(value), weight: Math.max(0, weight)}))
            .filter(choice => choice.amount >= 1 && choice.weight > 0)
            .sort((a, b) => a.amount - b.amount)
        const total = choices.reduce((sum, choice) => sum + choice.weight, 0)
        if (total > 0) return choices.map(choice => ({amount: choice.amount, chance: choice.weight / total}))
        return [{amount: 1, chance: 1}]
    }
    const min = Math.max(1, amount.min)
    const max = Math.max(min, amount.max)
    return Array.from({length: max - min + 1}, (_, offset) => ({amount: min + offset, chance: 1 / (max - min + 1)}))
}

/**
 * @param amount an entry's amount
 * @returns the amount it drops on average, rounded to one decimal
 */
export function usualAmount(amount: LootAmount): number {
    const average = amountChances(amount).reduce((sum, choice) => sum + choice.amount * choice.chance, 0)
    return Math.round(average * 10) / 10
}

/**
 * @param levels the levels of a kill, most specific first
 * @param tables the tables by id
 * @returns every entry that can drop: each table counted once, at its most specific level, and entries a more
 *     specific level with `replaceSame` has the item of left out
 */
export function pool(levels: readonly LootLevel[], tables: Readonly<Record<string, LootTable>>): Pooled[] {
    const replaced = new Set<string>()
    const used = new Set<string>()
    const pooled: Pooled[] = []
    for (const level of levels) {
        const own: Pooled[] = []
        for (const id of level.tables) {
            if (used.has(id)) continue
            used.add(id)
            tables[id]?.entries.forEach((entry, index) => own.push({table: id, index, entry}))
        }
        own.filter(entry => !replaced.has(entry.entry.item)).forEach(entry => pooled.push(entry))
        if (level.replaceSame) own.forEach(entry => replaced.add(entry.entry.item))
    }
    return pooled
}

/**
 * @param levels the levels of a kill, most specific first
 * @returns the weights of how many entries drop, from the most specific level that sets them, and that level
 */
export function slotsOf(levels: readonly LootLevel[]): {slots: Readonly<Record<string, number>>, source: LootLevel['source'] | null} {
    const level = levels.find(candidate => candidate.slots !== null && candidate.slots !== undefined)
    return level?.slots ? {slots: level.slots, source: level.source} : {slots: {}, source: null}
}

/**
 * @param slots weights by number of drops
 * @returns each number of drops with its chance from 0 to 1, by number
 */
export function slotChances(slots: Readonly<Record<string, number>>): {count: number, chance: number}[] {
    const counts = Object.entries(slots)
        .map(([count, weight]) => ({count: Number(count), weight: Math.max(0, weight)}))
        .filter(entry => Number.isFinite(entry.count) && entry.weight > 0)
        .sort((a, b) => a.count - b.count)
    const total = counts.reduce((sum, entry) => sum + entry.weight, 0)
    return counts.map(entry => ({count: entry.count, chance: total > 0 ? entry.weight / total : 0}))
}

/**
 * @param weighted values with their weights, whole numbers or shares alike
 * @param random the source of chance
 * @returns a value picked by weight, none when no weight is above 0
 */
export function pick<T>(weighted: readonly {value: T, weight: number}[], random: Random): T | undefined {
    const total = weighted.reduce((sum, entry) => sum + Math.max(0, entry.weight), 0)
    if (total <= 0) return undefined
    let roll = random() * total
    for (const entry of weighted) {
        if (entry.weight <= 0) continue
        roll -= entry.weight
        if (roll < 0) return entry.value
    }
    return undefined
}

/**
 * @param amount an entry's amount
 * @param random the source of chance
 * @returns a rolled amount
 */
export function rollAmount(amount: LootAmount, random: Random): number {
    return pick(amountChances(amount).map(choice => ({value: choice.amount, weight: choice.chance})), () => random()) ?? 1
}

/**
 * Rolls one kill the way the plugin does: the number of drops by the slot weights, then each drop by its entry's
 * weight from the pool, where an entry that cannot drop more than once leaves the pool once picked.
 *
 * @param levels the levels of a kill, most specific first
 * @param tables the tables by id
 * @param random the source of chance
 * @returns the picked entries with their amounts
 */
export function roll(levels: readonly LootLevel[], tables: Readonly<Record<string, LootTable>>, random: Random): Rolled[] {
    const count = pick(slotChances(slotsOf(levels).slots).map(entry => ({value: entry.count, weight: entry.chance})), random) ?? 0
    const remaining = [...pool(levels, tables)]
    const rolled: Rolled[] = []
    for (let slot = 0; slot < count; slot++) {
        const picked = pick(remaining.map((entry, index) => ({value: index, weight: entry.entry.weight})), random)
        if (picked === undefined) break
        const entry = remaining[picked]!
        if (!entry.entry.repeatable) remaining.splice(picked, 1)
        rolled.push({...entry, amount: rollAmount(entry.entry.amount, random)})
    }
    return rolled
}

/**
 * @param levels the levels of a kill, most specific first
 * @param tables the tables by id
 * @param kills how many kills to roll
 * @param random the source of chance
 * @returns for each pooled entry, by `table/index`, the share of kills that dropped it
 */
export function simulate(levels: readonly LootLevel[], tables: Readonly<Record<string, LootTable>>, kills: number, random: Random): Map<string, number> {
    const counts = new Map<string, number>()
    for (let kill = 0; kill < kills; kill++) {
        new Set(roll(levels, tables, random).map(entry => pooledKey(entry))).forEach(key => counts.set(key, (counts.get(key) ?? 0) + 1))
    }
    return new Map([...counts].map(([key, count]) => [key, count / kills]))
}

/**
 * @param entry a pooled entry
 * @returns the key {@link simulate} uses for it
 */
export function pooledKey(entry: Pooled): string {
    return `${entry.table}/${entry.index}`
}

/**
 * The levels a kill of a special mob in a world rolls, as the plugin collects them: the mob's, the world's unless the
 * mob does not inherit, the server's unless the mob or the world does not.
 *
 * @param payload the payload
 * @param mobId the mob's id
 * @param worldUid the world's UID, none for a world with default settings
 * @returns the levels, most specific first
 */
export function mobLevels(payload: EditorPayload | null | undefined, mobId: string, worldUid: string | undefined): LootLevel[] {
    const mob = payload?.mobs?.[mobId]?.loot
    const world = worldUid ? payload?.settings?.worlds?.[worldUid]?.settings?.loot : undefined
    const server = payload?.settings?.main?.loot
    const levels: LootLevel[] = [{source: 'mob', tables: mob?.tables ?? [], slots: mob?.slots, replaceSame: mob?.replaceSame !== false}]
    if (mob?.inherit === false) return levels
    levels.push({source: 'world', tables: world?.tables ?? [], slots: world?.slots, replaceSame: world?.replaceSame === true})
    if (world?.inherit === false) return levels
    levels.push({source: 'server', tables: server?.tables ?? [], slots: server?.slots, replaceSame: false})
    return levels
}
