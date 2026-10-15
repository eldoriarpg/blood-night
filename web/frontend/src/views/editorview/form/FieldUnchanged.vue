/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import NumberInput from '@/components/input/number/NumberInput.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import LabelledField from '@/components/input/LabelledField.vue'

/**
 * A number the world otherwise keeps as it is: a switch to change it at all, and the number only while it is on.
 * Off is stored as -1. With `ticks`, the number is stored in ticks and shown in seconds.
 */
const props = defineProps<{
  label: string
  value: number | undefined
  help?: string
  readOnly: boolean
  /** Whether the value is stored in ticks and shown in seconds. */
  ticks?: boolean
}>()

const emit = defineEmits<{
  save: [value: number]
}>()

const {t} = useI18n()
const changed = computed(() => (props.value ?? -1) >= 0)
const draft = ref<number | undefined>(undefined)

watch(() => props.value, value => {
  draft.value = value === undefined || value < 0 ? undefined : props.ticks ? value / 20 : value
}, {immediate: true})

function toggle(on: boolean): void {
  emit('save', on ? (props.ticks ? 20 : 1) * (draft.value ?? 1) : -1)
}

function save(): void {
  if (draft.value === undefined || draft.value < 0) return
  emit('save', Math.round(props.ticks ? draft.value * 20 : draft.value))
}
</script>

<template>
  <div class="flex flex-col gap-2">
    <ToggleSetting :disabled="readOnly" :hint="help" :label="label" :model-value="changed" @update:model-value="toggle"/>
    <LabelledField v-if="changed" v-slot="{id}" :label="ticks ? t('editor.unchanged.seconds') : t('editor.unchanged.value')">
      <NumberInput :id="id" v-model="draft" :disabled="readOnly" @focusout="save"/>
    </LabelledField>
  </div>
</template>
