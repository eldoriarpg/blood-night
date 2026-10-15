/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import RangeSlider from '@/components/input/range/RangeSlider.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import type {LootAmount} from '~/api/generated/payload'
import {amountChances} from '~/util/loot/lootTables'

/**
 * How many of an entry's item drop when it is picked: any amount from one number to another, drawn as a range slider
 * up to a stack (further when the range already reaches beyond), or one of a few amounts, each with a weight and the
 * chance it gives. Saved once a handle is let go or a number left.
 */
const props = defineProps<{
  value: LootAmount
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: LootAmount]
}>()

const {t} = useI18n()
const STACK = 64
const range = ref<[number, number]>([1, 1])
const choices = ref<{amount: number | undefined, weight: number | undefined}[]>([])

watch(() => props.value, value => {
  range.value = [Math.max(1, value.min), Math.max(value.min, value.max)]
  choices.value = Object.entries(value.choices)
    .map(([amount, weight]) => ({amount: Number(amount), weight}))
    .sort((a, b) => a.amount - b.amount)
}, {immediate: true, deep: true})

const top = computed(() => Math.max(STACK, range.value[1]))
const modes = computed(() => ({RANGE: t('editor.loot.amount.range'), CHOICES: t('editor.loot.amount.choices')}))
const chances = computed(() => new Map(amountChances({...props.value, mode: 'CHOICES', choices: chosen()}).map(choice => [choice.amount, choice.chance])))

function chosen(): Record<string, number> {
  return Object.fromEntries(choices.value
    .filter(choice => (choice.amount ?? 0) >= 1 && (choice.weight ?? 0) >= 1)
    .map(choice => [String(Math.round(choice.amount!)), Math.round(choice.weight!)]))
}

function mode(next: string | undefined): void {
  if (next !== 'RANGE' && next !== 'CHOICES') return
  if (next === props.value.mode) return
  const seeded = next === 'CHOICES' && Object.keys(props.value.choices).length === 0 ? {[String(props.value.min)]: 1} : props.value.choices
  emit('save', {...props.value, mode: next, choices: seeded})
}

function saveRange(value: [number, number]): void {
  const [min, max] = [Math.max(1, Math.round(value[0])), Math.max(1, Math.round(value[1]))]
  emit('save', {...props.value, min: Math.min(min, max), max: Math.max(min, max)})
}

function saveChoices(): void {
  emit('save', {...props.value, choices: chosen()})
}

function addChoice(): void {
  const taken = new Set(choices.value.map(choice => choice.amount))
  let next = 1
  while (taken.has(next)) next++
  choices.value.push({amount: next, weight: 1})
  saveChoices()
}

function removeChoice(index: number): void {
  choices.value.splice(index, 1)
  saveChoices()
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend class="contents"><FieldLabel>{{ t('editor.loot.amount.label') }}</FieldLabel></legend>
    <ChoicePicker :label="t('editor.loot.amount.label')" :labels="modes" :model-value="value.mode" :options="['RANGE', 'CHOICES']" :read-only="readOnly" class="max-w-64" @update:model-value="mode"/>
    <div v-if="value.mode === 'RANGE'" class="flex flex-wrap items-center gap-3">
      <RangeSlider
          v-model="range"
          :disabled="readOnly"
          :from-label="t('editor.loot.amount.from')"
          :max="top"
          :min="1"
          :step="1"
          :to-label="t('editor.loot.amount.to')"
          class="min-w-48 flex-1"
          @commit="saveRange"
      />
      <div class="flex items-center gap-1 text-sm">
        <div class="w-20"><NumberInput v-model="range[0]" :aria-label="t('editor.loot.amount.from')" :disabled="readOnly" @focusout="saveRange(range)"/></div>
        <span class="text-ink-muted">{{ t('editor.loot.amount.toWord') }}</span>
        <div class="w-20"><NumberInput v-model="range[1]" :aria-label="t('editor.loot.amount.to')" :disabled="readOnly" @focusout="saveRange(range)"/></div>
      </div>
    </div>
    <template v-else>
      <div v-for="(choice, index) in choices" :key="index" class="flex items-center gap-3">
        <div class="w-20"><NumberInput v-model="choice.amount" :aria-label="t('editor.loot.amount.amount')" :disabled="readOnly" @focusout="saveChoices"/></div>
        <span class="text-sm text-ink-muted">{{ t('editor.loot.amount.weight') }}</span>
        <div class="w-20"><NumberInput v-model="choice.weight" :aria-label="t('editor.loot.amount.weightOf', {amount: choice.amount ?? 0})" :disabled="readOnly" @focusout="saveChoices"/></div>
        <span class="text-sm text-ink-muted">{{ t('editor.dropWeights.share', {percent: Math.round((chances.get(choice.amount ?? 0) ?? 0) * 100)}) }}</span>
        <MutedIconButton v-if="!readOnly" :label="t('editor.loot.amount.remove', {amount: choice.amount ?? 0})" class="ml-auto" hover="error" icon="xmark" @click="removeChoice(index)"/>
      </div>
      <div v-if="!readOnly"><SecondaryButton compact icon="plus" @click="addChoice">{{ t('editor.loot.amount.add') }}</SecondaryButton></div>
    </template>
  </fieldset>
</template>
