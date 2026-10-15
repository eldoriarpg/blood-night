/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {ItemAsset} from '~/api/generated/schema'
import {translated} from '~/util/items/itemChoices'
import {searchTexts} from '~/util/search/textMatch'

/** What a choice belongs to: a graph type by its catalog id, or an enum of the payload schema by its definition name. */
export interface ChoiceOf {
    /** A graph type's id, e.g. `org.bukkit.entity.EntityType`. */
    typeId?: string
    /** A payload schema definition, e.g. `SpawnReason`. */
    enumName?: string
}

/** Minecraft's names, as the web backend serves them in the item index; empty while not loaded. */
export interface ChoiceNames {
    items: Readonly<Record<string, ItemAsset>>
    names: Readonly<Record<string, Readonly<Record<string, Readonly<Record<string, string>>>>>>
}

/** Looks an i18n key up: its message, or none when there is no such key. */
export type Translate = (key: string) => string | undefined

/** The Minecraft registry naming the values of each graph type whose values are registry keys. */
const TYPE_REGISTRIES: ReadonlyMap<string, string> = new Map([
    ['org.bukkit.entity.EntityType', 'entity_type'],
    ['org.bukkit.potion.PotionEffectType', 'effect'],
    ['org.bukkit.enchantments.Enchantment', 'enchantment'],
    ['org.bukkit.block.Biome', 'biome'],
    ['org.bukkit.attribute.Attribute', 'attribute'],
    ['org.bukkit.potion.PotionType', 'potion'],
])

/** The graph types whose values are items or blocks, named by the item index's items. */
const ITEM_TYPES: ReadonlySet<string> = new Set(['org.bukkit.inventory.ItemType', 'org.bukkit.block.BlockType'])

/** The Minecraft registry naming the values of each payload schema enum whose values are registry entries. */
const ENUM_REGISTRIES: ReadonlyMap<string, string> = new Map([['EntityType', 'entity_type']])

/**
 * @param typeId a graph type's id, e.g. `org.bukkit.event.entity.EntityDamageEvent$DamageCause`
 * @returns its simple class name, e.g. `DamageCause`
 */
export function simpleName(typeId: string): string {
    return typeId.split(/[.$]/).at(-1) ?? typeId
}

/**
 * @param value a registry key or enum constant, e.g. `minecraft:creeper` or `CREEPER`
 * @returns the registry key, e.g. `minecraft:creeper`
 */
export function registryKey(value: string): string {
    return value.includes(':') ? value : `minecraft:${value.toLowerCase()}`
}

/**
 * A readable name made from a value when nothing names it: a key in title case, a constant in sentence case.
 *
 * @param value e.g. `minecraft:foo_bar` or `FOO_BAR`
 * @returns e.g. `Foo Bar` or `Foo bar`
 */
export function humanise(value: string): string {
    const words = (value.split(':').at(-1) ?? value).split('_').filter(word => word !== '')
    if (value === value.toUpperCase() && !value.includes(':')) {
        const sentence = words.join(' ').toLowerCase()
        return sentence.charAt(0).toUpperCase() + sentence.slice(1)
    }
    return words.map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')
}

/**
 * @param of what the choice belongs to
 * @returns the Minecraft registry naming its values, if any
 */
export function registryOf(of: ChoiceOf): string | undefined {
    if (of.typeId !== undefined) return TYPE_REGISTRIES.get(of.typeId)
    return of.enumName === undefined ? undefined : ENUM_REGISTRIES.get(of.enumName)
}

/**
 * @param of what the choice belongs to
 * @returns the name its translations are kept under in `editor.enums`, e.g. `DamageCause`
 */
export function enumNameOf(of: ChoiceOf): string | undefined {
    return of.enumName ?? (of.typeId === undefined ? undefined : simpleName(of.typeId))
}

function minecraftName(value: string, of: ChoiceOf, names: ChoiceNames, locale: string): string | undefined {
    const key = registryKey(value)
    if (of.typeId !== undefined && ITEM_TYPES.has(of.typeId)) return translated(names.items[key]?.names, locale)
    const registry = registryOf(of)
    return registry === undefined ? undefined : translated(names.names[registry]?.[key], locale)
}

/**
 * The name of a choice in the editor's language: Minecraft's own name for registry entries, items and blocks, else
 * the translation under `editor.enums.<Name>.<VALUE>`, else one made from the value.
 *
 * @param value the stored value, e.g. `minecraft:creeper`, `CREEPER` or `FALL`
 * @param of what it belongs to
 * @param names Minecraft's names, empty while not loaded
 * @param locale the editor language, e.g. `de`
 * @param translate looks an i18n key up
 * @returns the name
 */
export function choiceLabel(value: string, of: ChoiceOf, names: ChoiceNames, locale: string, translate: Translate): string {
    const enumName = enumNameOf(of)
    return minecraftName(value, of, names, locale)
        ?? (enumName === undefined ? undefined : translate(`editor.enums.${enumName}.${value}`))
        ?? humanise(value)
}

/**
 * The choices matching a search: every word of it must be found in a choice's label or key, inside them or
 * abbreviating them (see {@link searchTexts}). Labels starting with the search come first; the order is kept
 * otherwise.
 *
 * @param options the choices' keys
 * @param labels their labels by key; a key without one is matched by itself
 * @param query what was typed
 * @returns the matching keys
 */
export function searchLabelled(options: readonly string[], labels: Readonly<Record<string, string>>, query: string): string[] {
    return searchTexts(options, option => [labels[option] ?? option, option], query)
}
