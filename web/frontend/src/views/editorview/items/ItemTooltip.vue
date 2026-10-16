/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import MiniMessageText from '@/components/display/MiniMessageText.vue'
import type {EditorItem, ItemSlot, MaterialInfo} from '~/api/generated/payload'
import {enchantmentLines} from '~/util/items/enchantmentLines'
import {attributeLines, keyWords, trimLines, type TooltipLine} from '~/util/items/itemLook'
import {attributeLabels} from '~/util/settings/serverSuggestions'

/**
 * An item's tooltip as players see it: the name, the armour trim, the enchantments, the lore, the attribute modifiers
 * (the material's own when the item sets none) and whether it is unbreakable, without the parts the item hides.
 */
const props = defineProps<{
  item: EditorItem
  info: MaterialInfo
  /** The name as it is being typed, the material's name when empty. */
  name: string
  /** The lore as it is being typed. */
  lore: readonly string[]
}>()

/** A tooltip line: MiniMessage the user typed, or plain text in a colour. */
interface Line extends TooltipLine {
  mini: boolean
}

const {t} = useI18n()
const session = useEditorSession()
const assets = useItemAssets()
const NAME_COLOR = '#ffffff'
const LORE_COLOR = '#aa00aa'
const ENCHANTMENT_COLOR = '#aaaaaa'
const CURSE_COLOR = '#ff5555'
const UNBREAKABLE_COLOR = '#5555ff'

const hidden = computed(() => new Set(props.item.hidden ?? []))
const enchanted = computed(() => hidden.value.has('ENCHANTMENTS')
  ? []
  : enchantmentLines(props.item.enchantments, key => assets.enchantment(key), session.payload.value?.choices?.enchantments ?? {}))
const trim = computed<TooltipLine[]>(() => props.item.trim && !hidden.value.has('TRIM')
  ? trimLines(props.item.trim, {
      upgrade: t('editor.items.tooltipText.upgrade'),
      pattern: name => t('editor.items.tooltipText.pattern', {name}),
      material: name => t('editor.items.tooltipText.material', {name}),
    })
  : [])
const attributes = computed<TooltipLine[]>(() => {
  if (hidden.value.has('ATTRIBUTES')) return []
  const rows = (props.item.attributes ?? []).length > 0 ? props.item.attributes : props.info.attributes
  const names = attributeLabels(rows.map(row => row.attribute))
  return attributeLines(rows, (slot: ItemSlot) => t(`editor.items.tooltipText.slot.${slot}`), key => names[key] ?? keyWords(key))
})
const unbreakable = computed(() => props.item.unbreakable && !hidden.value.has('UNBREAKABLE'))
const lines = computed<Line[]>(() => [
  {text: props.name, color: NAME_COLOR, mini: true},
  ...trim.value.map(line => ({...line, mini: false})),
  ...enchanted.value.map(line => ({text: line.text, color: line.curse ? CURSE_COLOR : ENCHANTMENT_COLOR, mini: false})),
  ...props.lore.map(text => ({text, color: LORE_COLOR, mini: true})),
  ...(attributes.value.length > 0 ? [{text: ' ', color: NAME_COLOR, mini: false}] : []),
  ...attributes.value.map(line => ({...line, mini: false})),
  ...(unbreakable.value ? [{text: t('editor.items.tooltipText.unbreakable'), color: UNBREAKABLE_COLOR, mini: false}] : []),
])
</script>

<template>
  <figure :aria-label="t('editor.items.tooltip')" class="flex flex-col gap-1">
    <figcaption class="text-xs text-ink-muted">{{ t('editor.items.tooltip') }}</figcaption>
    <div class="w-fit min-w-48 max-w-full rounded-sm border-2 border-[#25015b] bg-[#100010f0] px-2 py-1.5 font-[Minecraft,monospace] text-sm leading-snug">
      <template v-for="(line, index) in lines" :key="index">
        <div v-if="line.mini"><MiniMessageText :base="line.color" :text="line.text"/></div>
        <div v-else :style="{color: line.color}" class="whitespace-pre">{{ line.text }}</div>
      </template>
    </div>
  </figure>
</template>
