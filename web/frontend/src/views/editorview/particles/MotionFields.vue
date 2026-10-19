/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import SelectInput from '@/components/input/select/SelectInput.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import EffectNumber from '~/views/editorview/particles/EffectNumber.vue'
import type {Ease, Motion} from '~/api/generated/payload'

/**
 * Whether an emitter or a piece travels from its end to the other one, how long it takes and how far it goes past.
 */
const props = defineProps<{
  motion: Motion | undefined
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [motion: Motion | undefined]
}>()

const {t} = useI18n()
const EASES: readonly Ease[] = ['LINEAR', 'IN', 'OUT']

function travel(on: boolean): void {
  emit('save', on ? {type: 'TRAVEL', ticks: 10, through: 0, ease: 'LINEAR'} : undefined)
}

function save(change: Partial<Motion>): void {
  if (props.motion) emit('save', {...props.motion, ...change})
}
</script>

<template>
  <div class="flex flex-col gap-3">
    <ToggleSetting :disabled="readOnly" :hint="t('editor.particles.motion.hint')" :label="t('editor.particles.motion.travels')" :model-value="motion !== undefined" @update:model-value="travel"/>
    <div v-if="motion" class="flex flex-wrap items-end gap-3">
      <EffectNumber :label="t('editor.particles.motion.ticks')" :max="600" :min="1" :read-only="readOnly" :value="motion.ticks" class="w-28" whole @save="ticks => save({ticks})"/>
      <EffectNumber :label="t('editor.particles.motion.through')" :min="0" :read-only="readOnly" :value="motion.through" class="w-28" @save="through => save({through})"/>
      <LabelledField v-slot="{id}" :label="t('editor.particles.motion.ease')" class="w-44">
        <SelectInput :id="id" :disabled="readOnly" :model-value="motion.ease ?? 'LINEAR'" @update:model-value="ease => save({ease: ease as Ease})">
          <option v-for="ease in EASES" :key="ease" :value="ease">{{ t(`editor.particles.motion.eases.${ease}`) }}</option>
        </SelectInput>
      </LabelledField>
    </div>
  </div>
</template>
