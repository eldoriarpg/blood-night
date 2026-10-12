/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorItem, EditorPayload} from '~/api/generated/payload'
import {ItemKind} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import type {PatchOperation} from '~/util/patch/jsonPatch'

/** The fields of mobs that name an item: the equipment slots and a drop's item. */
const ITEM_FIELDS: ReadonlySet<string> = new Set(['head', 'chest', 'legs', 'feet', 'mainHand', 'offHand', 'itemId'])

/** An item of the payload with its id. */
export interface ItemEntry {
    id: string
    item: EditorItem
}

/**
 * @param payload the payload
 * @returns its items, sorted by id
 */
export function itemEntries(payload: EditorPayload | null | undefined): ItemEntry[] {
    return Object.entries(payload?.items ?? {})
        .map(([id, item]) => ({id, item}))
        .sort((a, b) => a.id.localeCompare(b.id))
}

/**
 * @param material the material key, e.g. `minecraft:golden_axe`
 * @returns a new item defined in the editor, without name, lore or enchantments
 */
export function newItem(material: string): EditorItem {
    return {kind: ItemKind.DEFINED, material, lore: [], enchantments: {}}
}

/**
 * @param payload the payload
 * @param id the new item's id
 * @param item the new item
 * @returns the operations adding it
 */
export function addItem(payload: EditorPayload, id: string, item: EditorItem): PatchOperation[] {
    const operations: PatchOperation[] = payload.items ? [] : [{op: 'add', path: '/items', value: {}}]
    return [...operations, {op: 'add', path: pointer('items', id), value: item}]
}

/**
 * The operation setting an item's name; an empty name removes it, which leaves the material's own.
 *
 * @param id the item's id
 * @param item the item
 * @param name the MiniMessage name
 * @returns the operations, none when nothing changes
 */
export function nameItem(id: string, item: EditorItem, name: string): PatchOperation[] {
    const trimmed = name.trim()
    if (trimmed === (item.name ?? '')) return []
    const path = pointer('items', id, 'name')
    if (trimmed === '') return [{op: 'remove', path}]
    return [{op: 'add', path, value: trimmed}]
}

/**
 * Whether a field of a mob names an item, so the editor can offer the payload's items and the server's materials.
 *
 * @param key the field's key
 * @returns whether it names an item
 */
export function isItemField(key: string): boolean {
    return ITEM_FIELDS.has(key)
}

