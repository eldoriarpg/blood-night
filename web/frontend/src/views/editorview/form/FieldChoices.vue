/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import CheckboxInput from '@/components/input/toggle/CheckboxInput.vue'
import FieldLabel from '@/components/typography/FieldLabel.vue'

/** A set of choices, e.g. the spawn reasons a world ignores: one checkbox per choice, named in the editor's language. */
const props = defineProps<{
  label: string
  options: readonly string[]
  /** The choices' names by key; a key without one is shown as it is. */
  labels: Readonly<Record<string, string>>
  value: readonly string[] | undefined
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: string[]]
}>()

const chosen = computed(() => new Set(props.value ?? []))

function flip(option: string, on: boolean): void {
  emit('save', props.options.filter(candidate => candidate === option ? on : chosen.value.has(candidate)))
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div class="grid max-h-56 grid-cols-[repeat(auto-fill,minmax(14rem,1fr))] gap-1 overflow-y-auto">
      <label v-for="option in options" :key="option" :title="option" class="flex min-h-9 items-center gap-2 text-sm">
        <CheckboxInput :disabled="readOnly" :model-value="chosen.has(option)" @update:model-value="on => flip(option, on)"/>
        {{ labels[option] ?? option }}
      </label>
    </div>
  </fieldset>
</template>
