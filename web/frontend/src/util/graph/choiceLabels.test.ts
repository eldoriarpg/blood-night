/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {choiceLabel, enumNameOf, humanise, registryKey, searchLabelled, simpleName, type ChoiceNames} from '~/util/graph/choiceLabels'

const names: ChoiceNames = {
    items: {'minecraft:stone': {texture: 'block/stone', names: {en_us: 'Stone', de_de: 'Stein'}}},
    names: {
        entity_type: {'minecraft:creeper': {en_us: 'Creeper', de_de: 'Creeper'}, 'minecraft:cave_spider': {en_us: 'Cave Spider', de_de: 'Höhlenspinne'}},
        effect: {'minecraft:speed': {en_us: 'Speed', de_de: 'Schnelligkeit'}},
        enchantment: {'minecraft:sharpness': {en_us: 'Sharpness'}},
    },
}

const messages: Readonly<Record<string, string>> = {
    'editor.enums.DamageCause.FALL': 'Fallschaden',
    'editor.enums.SpawnReason.NATURAL': 'Natürlich',
}

function translate(key: string): string | undefined {
    return messages[key]
}

describe('choiceLabel', () => {
    it('names registry entries of graph types', () => {
        expect(choiceLabel('minecraft:cave_spider', {typeId: 'org.bukkit.entity.EntityType'}, names, 'de', translate)).toBe('Höhlenspinne')
        expect(choiceLabel('minecraft:speed', {typeId: 'org.bukkit.potion.PotionEffectType'}, names, 'de', translate)).toBe('Schnelligkeit')
        expect(choiceLabel('minecraft:sharpness', {typeId: 'org.bukkit.enchantments.Enchantment'}, names, 'de', translate)).toBe('Sharpness')
    })

    it('names items and blocks by the item index', () => {
        expect(choiceLabel('minecraft:stone', {typeId: 'org.bukkit.block.BlockType'}, names, 'de', translate)).toBe('Stein')
        expect(choiceLabel('minecraft:stone', {typeId: 'org.bukkit.inventory.ItemType'}, names, 'en', translate)).toBe('Stone')
    })

    it('names entity types of the payload by their registry entry', () => {
        expect(choiceLabel('CAVE_SPIDER', {enumName: 'EntityType'}, names, 'de', translate)).toBe('Höhlenspinne')
    })

    it('takes translations of enums by definition or simple class name', () => {
        expect(choiceLabel('NATURAL', {enumName: 'SpawnReason'}, names, 'de', translate)).toBe('Natürlich')
        expect(choiceLabel('FALL', {typeId: 'org.bukkit.event.entity.EntityDamageEvent$DamageCause'}, names, 'de', translate)).toBe('Fallschaden')
    })

    it('falls back to a name made from the value', () => {
        expect(choiceLabel('minecraft:foo_bar', {typeId: 'org.bukkit.block.Biome'}, names, 'de', translate)).toBe('Foo Bar')
        expect(choiceLabel('FOO_BAR', {enumName: 'Unknown'}, names, 'de', translate)).toBe('Foo bar')
        expect(choiceLabel('BAT', {enumName: 'EntityType'}, {items: {}, names: {}}, 'de', translate)).toBe('Bat')
        expect(choiceLabel('x', {}, names, 'de', translate)).toBe('X')
    })
})

describe('helpers', () => {
    it('finds simple names, registry keys and enum names', () => {
        expect(simpleName('org.bukkit.event.entity.EntityDamageEvent$DamageCause')).toBe('DamageCause')
        expect(simpleName('org.bukkit.inventory.EquipmentSlot')).toBe('EquipmentSlot')
        expect(registryKey('CREEPER')).toBe('minecraft:creeper')
        expect(registryKey('pack:thing')).toBe('pack:thing')
        expect(enumNameOf({typeId: 'de.eldoria.graph.std.runtime.value.TimeUnit'})).toBe('TimeUnit')
        expect(enumNameOf({enumName: 'Color'})).toBe('Color')
        expect(enumNameOf({})).toBeUndefined()
    })

    it('humanises keys in title case and constants in sentence case', () => {
        expect(humanise('minecraft:foo_bar')).toBe('Foo Bar')
        expect(humanise('FOO_BAR')).toBe('Foo bar')
        expect(humanise('NOTCHED_10')).toBe('Notched 10')
    })
})

describe('searchLabelled', () => {
    const options = ['CAVE_SPIDER', 'SPIDER', 'CREEPER']
    const labels = {CAVE_SPIDER: 'Höhlenspinne', SPIDER: 'Spinne', CREEPER: 'Creeper'}

    it('matches labels and keys, labels starting with the search first', () => {
        expect(searchLabelled(options, labels, 'spinne')).toEqual(['SPIDER', 'CAVE_SPIDER'])
        expect(searchLabelled(options, labels, 'cave')).toEqual(['CAVE_SPIDER'])
        expect(searchLabelled(options, labels, '')).toEqual(options)
        expect(searchLabelled(options, {}, 'creep')).toEqual(['CREEPER'])
    })
})
