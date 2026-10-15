/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MutedText from '@/components/typography/MutedText.vue'

/**
 * How many drops fall: a row per number of drops with its weight, and the chance that weight gives, so "1 drop: 90,
 * 2 drops: 50" reads as 64 % and 36 %. A row is added for the smallest number of drops not listed yet and removed with its button; a
 * weight is saved once it is left.
 */
const props = defineProps<{
  label: string
  value: Record<string, unknown> | undefined
  help?: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: Record<string, number>]
}>()

const {t} = useI18n()
const drafts = ref<{count: number, weight: number | undefined}[]>([])

watch(() => props.value, value => {
  drafts.value = Object.entries(value ?? {})
    .map(([count, weight]) => ({count: Number(count), weight: typeof weight === 'number' ? weight : 0}))
    .filter(entry => Number.isFinite(entry.count))
    .sort((a, b) => a.count - b.count)
}, {immediate: true, deep: true})

const total = computed(() => drafts.value.reduce((sum, entry) => sum + Math.max(0, entry.weight ?? 0), 0))
const share = (weight: number | undefined) => total.value > 0 ? Math.round(Math.max(0, weight ?? 0) / total.value * 100) : 0

function save(): void {
  emit('save', Object.fromEntries(drafts.value.map(entry => [String(entry.count), Math.max(0, Math.round(entry.weight ?? 0))])))
}

function add(): void {
  const taken = new Set(drafts.value.map(entry => entry.count))
  let next = 1
  while (taken.has(next)) next++
  drafts.value.push({count: next, weight: 10})
  drafts.value.sort((a, b) => a.count - b.count)
  save()
}

function remove(index: number): void {
  drafts.value.splice(index, 1)
  save()
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div v-for="(entry, index) in drafts" :key="entry.count" class="flex items-center gap-3">
      <span class="w-24 shrink-0 text-sm">{{ entry.count === 1 ? t('editor.dropWeights.oneDrop') : t('editor.dropWeights.drops', {count: entry.count}) }}</span>
      <div class="w-28 shrink-0">
        <NumberInput v-model="entry.weight" :aria-label="t('editor.dropWeights.weight', {count: entry.count})" :disabled="readOnly" @focusout="save"/>
      </div>
      <span class="text-sm text-ink-muted">{{ t('editor.dropWeights.share', {percent: share(entry.weight)}) }}</span>
      <MutedIconButton v-if="!readOnly" :label="t('editor.dropWeights.remove', {count: entry.count})" class="ml-auto" hover="error" icon="xmark" @click="remove(index)"/>
    </div>
    <MutedText v-if="help" size="sm" tag="p">{{ help }}</MutedText>
    <div v-if="!readOnly"><SecondaryButton compact icon="plus" @click="add">{{ t('editor.dropWeights.add') }}</SecondaryButton></div>
  </fieldset>
</template>
