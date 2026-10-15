/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import SliderInput from '@/components/input/range/SliderInput.vue'
import {clockTime, DAY_TICKS} from '~/util/time/dayTime'

/**
 * A time of the Minecraft day, shown as the in-game clock shows it (06:00 at sunrise) and picked on a slider marked
 * with sunrise, noon, sunset and midnight. Stored in ticks after sunrise, saved once the slider is let go.
 */
const props = defineProps<{
  label: string
  value: number | undefined
  help?: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: number]
}>()

const {t} = useI18n()
const draft = ref(0)

watch(() => props.value, value => {
  draft.value = value ?? 0
}, {immediate: true})

const marks = computed(() => [
  {at: 0, label: t('editor.dayTime.sunrise')},
  {at: 6000, label: t('editor.dayTime.noon')},
  {at: 12000, label: t('editor.dayTime.sunset')},
  {at: 18000, label: t('editor.dayTime.midnight')},
])
</script>

<template>
  <LabelledField :help="help" :label="label">
    <div class="flex flex-col gap-1">
      <span class="font-mono text-lg">{{ clockTime(draft) }}</span>
      <SliderInput
          v-model="draft"
          :disabled="readOnly"
          :label="label"
          :max="DAY_TICKS - 250"
          :min="0"
          :step="250"
          :value-text="clockTime(draft)"
          @commit="emit('save', draft)"
      />
      <div class="relative h-4 text-xs text-ink-faint">
        <span
            v-for="mark in marks"
            :key="mark.at"
            :class="mark.at === 0 ? '' : '-translate-x-1/2'"
            :style="{left: `${mark.at / DAY_TICKS * 100}%`}"
            class="absolute whitespace-nowrap"
        >{{ mark.label }}</span>
      </div>
    </div>
  </LabelledField>
</template>

