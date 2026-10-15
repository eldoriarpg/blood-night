/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
/**
 * Two handles on one track that pick a range, e.g. "from 0.8 to 1.2". Each handle is a range input of its own, so the
 * keyboard and screen readers work as on any slider. A handle pulled past the other one moves that one instead and
 * stays where it is, so handles that meet can still be pulled apart either way. Where they meet, the lower one is on
 * top in the upper half of the track and the upper one below. Words under the track say what its ends and middle
 * mean. The range is shown while dragging and reported with `commit` once it is let go,
 * as last moved rather than as the model still holds it, since a key press moves and lets go before the model updates.
 */
const model = defineModel<[number, number]>({required: true})

const props = defineProps<{
  min: number
  max: number
  step: number
  /** Names the lower handle for screen readers, e.g. "Lowest pitch". */
  fromLabel: string
  /** Names the upper handle. */
  toLabel: string
  /** Words under the track at the values they name, e.g. "normal" at 1. */
  marks?: readonly {value: number, label: string}[]
  /** How a value is shown, e.g. as a percentage. */
  format?: (value: number) => string
  disabled?: boolean
}>()

const emit = defineEmits<{
  commit: [value: [number, number]]
}>()

const shown = (value: number) => props.format ? props.format(value) : String(value)
const percent = (value: number) => (value - props.min) / (props.max - props.min) * 100
const lowerOnTop = computed(() => percent(model.value[0]) > 50)
let moved: [number, number] | undefined

function move(index: 0 | 1, event: Event): void {
  const value = Number((event.target as HTMLInputElement).value)
  const [low, high] = model.value
  const next: [number, number] = index === 0
    ? value > high ? [low, value] : [value, high]
    : value < low ? [value, high] : [low, value]
  model.value = next
  moved = next
  if (next[index] !== value) (event.target as HTMLInputElement).value = String(next[index])
}

function commit(): void {
  emit('commit', moved ?? model.value)
  moved = undefined
}
</script>

<template>
  <div class="flex flex-col gap-1">
    <span class="text-sm text-ink-muted">
      {{ model[0] === model[1] ? shown(model[0]) : `${shown(model[0])} - ${shown(model[1])}` }}
    </span>
    <div class="range relative h-6">
      <div class="absolute inset-x-0 top-1/2 h-1 -translate-y-1/2 rounded-full bg-line"/>
      <div
          :style="{left: `${percent(model[0])}%`, right: `${100 - percent(model[1])}%`}"
          class="absolute top-1/2 h-1 -translate-y-1/2 rounded-full bg-accent"
      />
      <input
          :aria-label="fromLabel"
          :class="{'z-10': lowerOnTop}"
          :disabled="disabled"
          :max="max"
          :min="min"
          :step="step"
          :value="model[0]"
          type="range"
          @change="commit"
          @input="move(0, $event)"
      />
      <input
          :aria-label="toLabel"
          :disabled="disabled"
          :max="max"
          :min="min"
          :step="step"
          :value="model[1]"
          type="range"
          @change="commit"
          @input="move(1, $event)"
      />
    </div>
    <div v-if="marks?.length" class="relative h-4 text-xs text-ink-faint">
      <span
          v-for="mark in marks"
          :key="mark.label"
          :class="percent(mark.value) <= 0 ? '' : percent(mark.value) >= 100 ? '-translate-x-full' : '-translate-x-1/2'"
          :style="{left: `${percent(mark.value)}%`}"
          class="absolute whitespace-nowrap"
      >{{ mark.label }}</span>
    </div>
  </div>
</template>

<style scoped>
.range input {
  position: absolute;
  inset: 0;
  width: 100%;
  margin: 0;
  appearance: none;
  background: transparent;
  pointer-events: none;
}

.range input::-webkit-slider-thumb {
  appearance: none;
  width: 1.1rem;
  height: 1.1rem;
  border-radius: 9999px;
  background: var(--color-ink);
  border: 2px solid var(--color-accent);
  cursor: grab;
  pointer-events: auto;
}

.range input::-moz-range-thumb {
  width: 1rem;
  height: 1rem;
  border-radius: 9999px;
  background: var(--color-ink);
  border: 2px solid var(--color-accent);
  cursor: grab;
  pointer-events: auto;
}

.range input:focus-visible::-webkit-slider-thumb {
  outline: 2px solid var(--color-focus);
  outline-offset: 2px;
}

.range input:focus-visible::-moz-range-thumb {
  outline: 2px solid var(--color-focus);
  outline-offset: 2px;
}

.range input:disabled::-webkit-slider-thumb {
  cursor: not-allowed;
  opacity: 0.5;
}
</style>
