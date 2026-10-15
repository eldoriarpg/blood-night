/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {ItemAsset} from '~/api/generated/schema'
import type {EditorItem} from '~/api/generated/payload'
import {withFallbacks} from '~/util/minimessage/translations'
import {searchTexts} from '~/util/search/textMatch'

/** One entry of an item picker: a material, or an item of the payload shown with its material's icon. */
export interface ItemChoice {
    key: string
    name: string
    english: string
    texture: string | undefined
    custom: boolean
    /** For an item of the payload: its type and enchantments, e.g. `Diamond Sword · Sharpness 5`. */
    details: string
    /** Everything a search may match, lower case: key, names, type, lore, enchantments. */
    terms: string[]
}

/** Minecraft's names, as the web backend serves them. */
export interface MinecraftNames {
    items: Readonly<Record<string, ItemAsset>>
    enchantments: Readonly<Record<string, Readonly<Record<string, string>>>>
}

/** The Minecraft language file of each editor language. */
const LANGUAGES: Readonly<Record<string, string>> = {
    en: 'en_us', de: 'de_de', es: 'es_es', fr: 'fr_fr', ms: 'ms_my', tr: 'tr_tr', zh: 'zh_cn',
}

/**
 * @param key a key, e.g. `minecraft:diamond_sword`
 * @returns a readable fallback name, e.g. `Diamond Sword`
 */
export function keyName(key: string): string {
    return (key.split(':').at(-1) ?? key).split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')
}

/**
 * @param text a MiniMessage text, e.g. `<red>Blood <bold>Blade`
 * @returns the plain text, e.g. `Blood Blade`, with texts in the player's language as their English fallback
 */
export function plainText(text: string): string {
    return withFallbacks(text).replaceAll(/<[^<>]*>/g, '').trim()
}

/**
 * @param names a name of Minecraft's by language, e.g. `{en_us: 'Creeper'}`
 * @param locale the editor language, e.g. `de`
 * @returns the name in that language, else in English, else none
 */
export function translated(names: Readonly<Record<string, string>> | undefined, locale: string): string | undefined {
    return names?.[LANGUAGES[locale] ?? 'en_us'] ?? names?.en_us
}

/**
 * The name of a material in an editor language: Minecraft's own translation, else English, else one made from the
 * key.
 *
 * @param key the material key
 * @param asset its asset, if known
 * @param locale the editor language, e.g. `de`
 * @returns the name
 */
export function materialName(key: string, asset: ItemAsset | undefined, locale: string): string {
    return translated(asset?.names, locale) ?? keyName(key)
}

/**
 * @param key an enchantment key, e.g. `minecraft:sharpness`
 * @param names its names by language, if Minecraft has it
 * @param locale the editor language
 * @returns its name, e.g. `Schärfe`; data pack enchantments get one made from the key
 */
export function enchantmentName(key: string, names: Readonly<Record<string, string>> | undefined, locale: string): string {
    return translated(names, locale) ?? keyName(key)
}

function customChoice(key: string, item: EditorItem, names: MinecraftNames, locale: string): ItemChoice {
    const asset = names.items[item.material]
    const type = materialName(item.material, asset, locale)
    const typeEnglish = materialName(item.material, asset, 'en')
    const enchantments = Object.entries(item.enchantments ?? {}).sort(([a], [b]) => a.localeCompare(b))
    const enchantmentNames = enchantments.map(([enchantment, level]) => `${enchantmentName(enchantment, names.enchantments[enchantment], locale)} ${level}`)
    const lore = (item.lore ?? []).map(plainText)
    return {
        key,
        name: item.name ? plainText(item.name) : key,
        english: typeEnglish,
        texture: asset?.texture,
        custom: true,
        details: [type, ...enchantmentNames].join(' · '),
        terms: [
            key,
            item.name ? plainText(item.name) : '',
            item.material,
            type,
            typeEnglish,
            ...lore,
            ...enchantments.flatMap(([enchantment]) => [
                enchantment,
                enchantmentName(enchantment, names.enchantments[enchantment], locale),
                enchantmentName(enchantment, names.enchantments[enchantment], 'en'),
            ]),
        ].filter(term => term !== '').map(term => term.toLowerCase()),
    }
}

/**
 * Everything an item picker offers: the payload's items first, then the materials, each with icon, name and what a
 * search can match.
 *
 * @param materials the server's material keys
 * @param items the payload's items, or none for a pure material picker
 * @param names Minecraft's item and enchantment names, empty while not loaded
 * @param locale the editor language
 * @returns the choices
 */
export function itemChoices(
    materials: readonly string[],
    items: Readonly<Record<string, EditorItem>>,
    names: MinecraftNames,
    locale: string,
): ItemChoice[] {
    const custom = Object.entries(items).sort(([a], [b]) => a.localeCompare(b)).map(([key, item]) => customChoice(key, item, names, locale))
    const plain = materials.map(key => {
        const name = materialName(key, names.items[key], locale)
        const english = materialName(key, names.items[key], 'en')
        return {key, name, english, texture: names.items[key]?.texture, custom: false, details: '', terms: [key, name, english].map(term => term.toLowerCase())}
    })
    return [...custom, ...plain]
}

/**
 * The choices matching a search: every word of it must be found in the choice's name, English name, key, type, lore or
 * enchantments, inside them or abbreviating them (see {@link searchTexts}), so `sword sharp` finds a sharpened sword
 * and `ds` or `dsword` a diamond sword. Names starting with the search come first.
 *
 * @param choices the choices
 * @param query what was typed
 * @returns the matching choices
 */
export function searchChoices(choices: readonly ItemChoice[], query: string): ItemChoice[] {
    return searchTexts(choices, choice => [choice.name, choice.english, ...choice.terms], query, 2)
}
