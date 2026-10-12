/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
/** A choice between exactly two options, named on either side of the switch. */
const model = defineModel<string>({required: true})

const props = defineProps<{
  optionA: string
  optionB: string
  labelA: string
  labelB: string
  disabled?: boolean
}>()

/** Moves the choice to the other option. */
function toggle() {
  model.value = model.value === props.optionA ? props.optionB : props.optionA
}
</script>

<template>
  <div class="inline-flex items-center gap-2">
    <span class="text-xs font-medium transition-colors" :class="model === optionA ? 'text-accent-text' : 'text-ink-faint'">{{ labelA }}</span>
    <button
        type="button"
        role="switch"
        :aria-checked="model === optionB"
        :aria-label="`${labelA} / ${labelB}`"
        :disabled="disabled"
        class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center rounded-full border border-line bg-raised transition-colors duration-150 disabled:opacity-50 disabled:cursor-not-allowed"
        @click="toggle"
    >
      <span
          :class="model === optionB ? 'translate-x-4' : 'translate-x-0.5'"
          class="pointer-events-none inline-block h-3.5 w-3.5 rounded-full bg-accent transition-transform duration-150"
      />
    </button>
    <span class="text-xs font-medium transition-colors" :class="model === optionB ? 'text-accent-text' : 'text-ink-faint'">{{ labelB }}</span>
  </div>
</template>
