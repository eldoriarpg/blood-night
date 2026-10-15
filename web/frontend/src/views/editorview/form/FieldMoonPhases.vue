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
import MoonIcon from '@/components/display/MoonIcon.vue'
import {MOON_PHASES, type MoonPhaseName} from '~/util/moon/moonPhases'

/**
 * The chance of a blood night for every phase of the moon: each phase with its picture, its name and a slider from 0 to
 * 100 %. Every phase is always listed; one left at 0 never brings a blood night. All phases are saved together once a
 * slider is let go or a number left.
 */
const props = defineProps<{
  label: string
  value: Record<string, unknown> | undefined
  help?: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: Record<MoonPhaseName, number>]
}>()

const {t} = useI18n()
const drafts = ref<Record<MoonPhaseName, number>>(Object.fromEntries(MOON_PHASES.map(phase => [phase, 0])) as Record<MoonPhaseName, number>)

watch(() => props.value, value => {
  drafts.value = Object.fromEntries(MOON_PHASES.map(phase => [phase, typeof value?.[phase] === 'number' ? value[phase] as number : 0])) as Record<MoonPhaseName, number>
}, {immediate: true, deep: true})

function save(): void {
  const cleaned = Object.fromEntries(MOON_PHASES.map(phase => [phase, Math.min(100, Math.max(0, Math.round(drafts.value[phase] ?? 0)))])) as Record<MoonPhaseName, number>
  emit('save', cleaned)
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div v-for="phase in MOON_PHASES" :key="phase" class="flex items-center gap-3">
      <MoonIcon :phase="phase"/>
      <span class="w-36 shrink-0 text-sm">{{ t(`editor.moonPhases.${phase}`) }}</span>
      <SliderInput
          v-model="drafts[phase]"
          :disabled="readOnly"
          :label="t(`editor.moonPhases.${phase}`)"
          :max="100"
          :min="0"
          :step="5"
          class="min-w-0 flex-1"
          @commit="save"
      />
      <div class="flex w-24 shrink-0 items-center gap-1">
        <NumberInput v-model="drafts[phase]" :aria-label="t('editor.moonPhases.chance', {phase: t(`editor.moonPhases.${phase}`)})" :disabled="readOnly" @focusout="save"/>
        <span class="text-sm text-ink-muted">%</span>
      </div>
    </div>
    <MutedText v-if="help" size="sm" tag="p">{{ help }}</MutedText>
  </fieldset>
</template>
