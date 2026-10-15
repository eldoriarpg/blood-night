/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import MutedText from '@/components/typography/MutedText.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import SliderInput from '@/components/input/range/SliderInput.vue'
import {curveChances, type CurveSettings} from '~/util/nights/curve'

/**
 * The curve night selection: the chance on the cycle's first night, the chance halfway through and how many nights a
 * cycle lasts, above a graph of the chance for each night over two cycles, so what the numbers do is seen while
 * they change. Saved once a slider is let go or a number left.
 */
const props = defineProps<{
  label: string
  value: Readonly<Record<string, unknown>> | undefined
  help?: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: CurveSettings]
}>()

const {t} = useI18n()
const SHOWN_CYCLES = 2
const MAX_SHOWN = 100
const draft = reactive<CurveSettings>({min: 20, max: 80, period: 10})

watch(() => props.value, value => {
  const read = (key: keyof CurveSettings, fallback: number) => typeof value?.[key] === 'number' ? value[key] : fallback
  draft.min = read('min', 20)
  draft.max = read('max', 80)
  draft.period = read('period', 10)
}, {immediate: true})

const chances = computed(() => curveChances(draft, Math.min(MAX_SHOWN, Math.max(draft.period, 2) * SHOWN_CYCLES)))
const points = computed(() => chances.value
  .map((chance, index) => `${(index + 0.5) / chances.value.length * 100},${100 - chance}`)
  .join(' '))
const cycleStarts = computed(() => chances.value.flatMap((_, index) => index > 0 && index % Math.max(draft.period, 2) === 0 ? [index] : []))
const labelled = (index: number) => chances.value.length <= 30 || index === 0 || (index + 1) % 5 === 0

function save(): void {
  const percent = (value: number) => Math.min(100, Math.max(0, Math.round(value)))
  emit('save', {min: percent(draft.min), max: percent(draft.max), period: Math.max(2, Math.round(draft.period))})
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div v-for="end in (['min', 'max'] as const)" :key="end" class="flex items-center gap-3">
      <span class="w-48 shrink-0 text-sm">{{ t(`editor.curve.${end}`) }}</span>
      <SliderInput v-model="draft[end]" :disabled="readOnly" :label="t(`editor.curve.${end}`)" :max="100" :min="0" :step="5" class="min-w-0 flex-1" @commit="save"/>
      <div class="flex w-24 shrink-0 items-center gap-1">
        <NumberInput v-model="draft[end]" :aria-label="t(`editor.curve.${end}`)" :disabled="readOnly" @focusout="save"/>
        <span class="text-sm text-ink-muted">%</span>
      </div>
    </div>
    <div class="flex items-center gap-3">
      <span class="w-48 shrink-0 text-sm">{{ t('editor.curve.period') }}</span>
      <div class="w-24">
        <NumberInput v-model="draft.period" :aria-label="t('editor.curve.period')" :disabled="readOnly" @change="save"/>
      </div>
    </div>
    <figure :aria-label="t('editor.curve.graph')" class="rounded-panel border border-line-soft bg-ground p-3">
      <div class="relative h-36">
        <div v-for="line in [25, 50, 75]" :key="line" :style="{bottom: `${line}%`}" class="absolute inset-x-0 border-t border-dashed border-line-soft"/>
        <div v-for="start in cycleStarts" :key="start" :style="{left: `${start / chances.length * 100}%`}" class="absolute inset-y-0 border-l border-line"/>
        <div class="absolute inset-0 flex items-end gap-0.5">
          <div
              v-for="(chance, index) in chances"
              :key="index"
              :style="{height: `${Math.max(chance, 1)}%`}"
              :title="t('editor.phaseCycle.night', {night: index + 1, chance})"
              class="min-w-0 flex-1 rounded-t-sm bg-accent/50"
          />
        </div>
        <svg class="pointer-events-none absolute inset-0 h-full w-full overflow-visible" preserveAspectRatio="none" viewBox="0 0 100 100">
          <polyline :points="points" class="fill-none stroke-ink" stroke-linejoin="round" stroke-width="1.5" vector-effect="non-scaling-stroke"/>
        </svg>
      </div>
      <div class="mt-1 flex gap-0.5 text-[10px] text-ink-faint">
        <span v-for="(_, index) in chances" :key="index" class="min-w-0 flex-1 text-center">{{ labelled(index) ? index + 1 : '' }}</span>
      </div>
      <figcaption class="mt-1 text-xs text-ink-muted">{{ t('editor.curve.caption', {cycles: SHOWN_CYCLES}) }}</figcaption>
    </figure>
    <MutedText v-if="help" size="sm" tag="p">{{ help }}</MutedText>
  </fieldset>
</template>
