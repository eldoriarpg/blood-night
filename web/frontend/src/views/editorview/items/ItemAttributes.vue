/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SectionHeader from '@/components/typography/SectionHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'
import DecimalInput from '@/components/input/number/DecimalInput.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import type {EditorItem, ItemAttribute, ItemSlot, MaterialInfo, Operation} from '~/api/generated/payload'
import {attributeLabels} from '~/util/settings/serverSuggestions'
import {addAttribute} from '~/util/items/itemLook'

/**
 * The attribute modifiers an item gives: a row each with the attribute, the amount, how it applies and where the item
 * has to be. Without rows the item gives what its material gives by itself, which is listed; the first row added
 * brings those along as rows, since Minecraft drops them once an item has any.
 */
const props = defineProps<{
  item: EditorItem
  info: MaterialInfo
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [key: keyof EditorItem, value: unknown]
}>()

const {t} = useI18n()
const session = useEditorSession()
const OPERATIONS: readonly Operation[] = ['ADD_NUMBER', 'ADD_SCALAR', 'MULTIPLY_SCALAR_1']
const SLOTS: readonly ItemSlot[] = ['ANY', 'MAINHAND', 'OFFHAND', 'HAND', 'HEAD', 'CHEST', 'LEGS', 'FEET', 'ARMOR', 'BODY', 'SADDLE']
const amounts = ref<(number | undefined)[]>([])

const rows = computed(() => props.item.attributes ?? [])
const attributes = computed(() => session.payload.value?.choices?.attributes ?? [])
const names = computed(() => attributeLabels(attributes.value))
const operations = computed(() => Object.fromEntries(OPERATIONS.map(key => [key, t(`editor.items.attributes.operation.${key}`)])))
const slots = computed(() => Object.fromEntries(SLOTS.map(key => [key, t(`editor.items.attributes.slot.${key}`)])))

watch(rows, value => {
  amounts.value = value.map(row => row.amount)
}, {immediate: true, deep: true})

function change(index: number, part: Partial<ItemAttribute>): void {
  emit('save', 'attributes', rows.value.map((row, at) => at === index ? {...row, ...part} : row))
}

function saveAmount(index: number): void {
  const amount = amounts.value[index]
  if (amount !== undefined && amount !== rows.value[index]?.amount) change(index, {amount})
}

function add(): void {
  const attribute = attributes.value.includes('minecraft:attack_damage') ? 'minecraft:attack_damage' : attributes.value[0] ?? 'minecraft:attack_damage'
  emit('save', 'attributes', addAttribute(props.item, props.info, {attribute, amount: 1, operation: 'ADD_NUMBER', slot: 'MAINHAND'}))
}

function remove(index: number): void {
  emit('save', 'attributes', rows.value.filter((_, at) => at !== index))
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend class="pb-1"><SectionHeader>{{ t('editor.items.attributes.title') }}</SectionHeader></legend>
    <MutedText v-if="rows.length === 0" size="sm" tag="p">
      {{ info.attributes.length > 0 ? t('editor.items.attributes.own') : t('editor.items.attributes.none') }}
    </MutedText>
    <div v-for="(row, index) in rows" :key="index" class="flex flex-wrap items-center gap-2 border-b border-line-soft pb-2">
      <ChoicePicker :label="t('editor.items.attributes.attribute')" :labels="names" :model-value="row.attribute" :options="attributes" :read-only="readOnly" class="min-w-44 flex-1" @update:model-value="key => key && change(index, {attribute: key})"/>
      <div class="w-24"><DecimalInput v-model="amounts[index]" :aria-label="t('editor.items.attributes.amount')" :disabled="readOnly" @focusout="saveAmount(index)"/></div>
      <ChoicePicker :label="t('editor.items.attributes.operationLabel')" :labels="operations" :model-value="row.operation" :options="[...OPERATIONS]" :read-only="readOnly" class="w-48" @update:model-value="key => key && change(index, {operation: key as Operation})"/>
      <ChoicePicker :label="t('editor.items.attributes.slotLabel')" :labels="slots" :model-value="row.slot" :options="[...SLOTS]" :read-only="readOnly" class="w-40" @update:model-value="key => key && change(index, {slot: key as ItemSlot})"/>
      <MutedIconButton v-if="!readOnly" :label="t('editor.items.attributes.remove')" hover="error" icon="xmark" @click="remove(index)"/>
    </div>
    <div v-if="!readOnly" class="flex flex-col gap-1">
      <div><SecondaryButton compact icon="plus" @click="add">{{ t('editor.items.attributes.add') }}</SecondaryButton></div>
      <MutedText v-if="rows.length === 0 && info.attributes.length > 0" size="sm" tag="p">{{ t('editor.items.attributes.addHint') }}</MutedText>
    </div>
  </fieldset>
</template>
