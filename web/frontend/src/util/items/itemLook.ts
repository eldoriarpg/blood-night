/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorItem, EditorPayload, ItemAttribute, ItemSlot, ItemTrim, MaterialInfo, TooltipPart} from '~/api/generated/payload'

/** What a material gives when the server sent nothing about it: a full stack and nothing else. */
const PLAIN: MaterialInfo = {stackSize: 64, durability: 0, dyeable: false, trimmable: false, attributes: []}

/** A line of the item tooltip preview, with its colour. */
export interface TooltipLine {
    text: string
    color: string
}

const GRAY = '#aaaaaa'
const BLUE = '#5555ff'
const RED = '#ff5555'
const ATTRIBUTE_ORDER: readonly ItemSlot[] = ['MAINHAND', 'OFFHAND', 'HAND', 'HEAD', 'CHEST', 'LEGS', 'FEET', 'BODY', 'ARMOR', 'SADDLE', 'ANY']

/**
 * @param material a material key
 * @param payload the payload
 * @returns what the server said about the material, a plain item when nothing
 */
export function materialInfo(material: string, payload: EditorPayload | null | undefined): MaterialInfo {
    return payload?.choices?.materialInfo?.[material] ?? PLAIN
}

/**
 * The rows of an item's attribute modifiers after one is added. Minecraft drops a material's own modifiers once an
 * item has any, so the first row added brings the material's own along, where they can be seen and changed.
 *
 * @param item the item
 * @param info its material
 * @param row the row to add
 * @returns every row afterwards
 */
export function addAttribute(item: EditorItem, info: MaterialInfo, row: ItemAttribute): ItemAttribute[] {
    const own = item.attributes ?? []
    return own.length === 0 ? [...info.attributes, row] : [...own, row]
}

/**
 * @param item the item
 * @param info its material
 * @returns the tooltip parts it shows that can be hidden
 */
export function hideableParts(item: EditorItem, info: MaterialInfo): TooltipPart[] {
    const parts: TooltipPart[] = ['ENCHANTMENTS', 'ATTRIBUTES']
    if (item.unbreakable) parts.push('UNBREAKABLE')
    if (info.dyeable) parts.push('DYE')
    if (info.trimmable) parts.push('TRIM')
    return parts
}

/**
 * @param key a key, e.g. `minecraft:snout`
 * @returns its name in words, e.g. `Snout`
 */
export function keyWords(key: string): string {
    return key.slice(key.indexOf(':') + 1).split(/[._/]/).filter(Boolean).map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')
}

/**
 * @param trim an armour trim
 * @param texts the words around it: the heading and the pattern and material suffixes
 * @returns the lines Minecraft shows for it
 */
export function trimLines(trim: ItemTrim, texts: {upgrade: string, pattern: (name: string) => string, material: (name: string) => string}): TooltipLine[] {
    return [
        {text: texts.upgrade, color: GRAY},
        {text: ` ${texts.pattern(keyWords(trim.pattern))}`, color: GRAY},
        {text: ` ${texts.material(keyWords(trim.material))}`, color: GRAY},
    ]
}

/**
 * The attribute block of an item's tooltip, as Minecraft shows it: a heading per slot, then a line per modifier, added
 * amounts as numbers and the others as percentages, losses in red.
 *
 * @param rows the attribute modifiers
 * @param heading the heading of a slot, e.g. "When in Main Hand:"
 * @param name the name of an attribute by its key
 * @returns the lines
 */
export function attributeLines(rows: readonly ItemAttribute[], heading: (slot: ItemSlot) => string, name: (key: string) => string): TooltipLine[] {
    const lines: TooltipLine[] = []
    for (const slot of ATTRIBUTE_ORDER) {
        const inSlot = rows.filter(row => row.slot === slot)
        if (inSlot.length === 0) continue
        lines.push({text: heading(slot), color: GRAY})
        for (const row of inSlot) {
            const value = row.operation === 'ADD_NUMBER' ? round(row.amount) : `${round(row.amount * 100)}%`
            const sign = row.amount < 0 ? '' : '+'
            lines.push({text: ` ${sign}${value} ${name(row.attribute)}`, color: row.amount < 0 ? RED : BLUE})
        }
    }
    return lines
}

function round(value: number): string {
    return String(Math.round(value * 100) / 100)
}
