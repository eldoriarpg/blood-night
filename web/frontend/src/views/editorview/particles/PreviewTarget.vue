/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import SliderInput from '@/components/input/range/SliderInput.vue'

/** Where the preview's player figure stands: how far ahead, how high it is lifted, how far aside and how it is turned. */
interface TargetPlace {
  distance: number
  lift: number
  side: number
  turn: number
}

const model = defineModel<TargetPlace>({required: true})

const {t} = useI18n()

function set(key: keyof TargetPlace, value: number): void {
  model.value = {...model.value, [key]: value}
}
</script>

<template>
  <div class="grid max-w-2xl grid-cols-2 gap-x-4 gap-y-2">
    <LabelledField :label="t('editor.particles.preview.distance', {blocks: model.distance})">
      <SliderInput :label="t('editor.particles.preview.distance', {blocks: model.distance})" :max="16" :min="2" :model-value="model.distance" :step="1" @update:model-value="value => set('distance', value)"/>
    </LabelledField>
    <LabelledField :label="t('editor.particles.preview.lift', {blocks: model.lift})">
      <SliderInput :label="t('editor.particles.preview.lift', {blocks: model.lift})" :max="20" :min="0" :model-value="model.lift" :step="1" @update:model-value="value => set('lift', value)"/>
    </LabelledField>
    <LabelledField :label="t('editor.particles.preview.side', {blocks: model.side})">
      <SliderInput :label="t('editor.particles.preview.side', {blocks: model.side})" :max="8" :min="-8" :model-value="model.side" :step="1" @update:model-value="value => set('side', value)"/>
    </LabelledField>
    <LabelledField :label="t('editor.particles.preview.turn', {degrees: model.turn})">
      <SliderInput :label="t('editor.particles.preview.turn', {degrees: model.turn})" :max="359" :min="0" :model-value="model.turn" :step="15" @update:model-value="value => set('turn', value)"/>
    </LabelledField>
  </div>
</template>
