/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import MutedText from '@/components/typography/MutedText.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'

/**
 * A cycle of nights with a chance each, drawn as an equalizer: one bar per night, pulled up and down with the pointer
 * (a drag across several bars paints them in one go) or the arrow keys, with a line through the bars showing the curve.
 * How many nights the cycle has is set beside it; new nights start at the last one's chance. Saved once a drag is let
 * go, a key pressed or the number of nights changed.
 */
const props = defineProps<{
  label: string
  value: readonly number[] | undefined
  help?: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: number[]]
}>()

const {t} = useI18n()
const MAX_NIGHTS = 100
const STEP = 5
const chances = ref<number[]>([])
const nights = ref<number | undefined>(undefined)
const active = ref<number | undefined>(undefined)
const chart = ref<HTMLElement | null>(null)
const dragging = ref(false)

watch(() => props.value, value => {
  chances.value = [...(value?.length ? value : [50])]
  nights.value = chances.value.length
}, {immediate: true})

const points = computed(() => chances.value
  .map((chance, index) => `${(index + 0.5) / chances.value.length * 100},${100 - chance}`)
  .join(' '))
const shown = computed(() => active.value === undefined ? undefined : {night: active.value + 1, chance: chances.value[active.value]})
const labelled = (index: number) => chances.value.length <= 20 || index === 0 || (index + 1) % 5 === 0

function save(): void {
  emit('save', chances.value.map(chance => Math.min(100, Math.max(0, Math.round(chance)))))
}

function resize(count: number | undefined): void {
  const wanted = Math.min(MAX_NIGHTS, Math.max(1, Math.round(count ?? chances.value.length)))
  const last = chances.value.at(-1) ?? 50
  chances.value = Array.from({length: wanted}, (_, index) => chances.value[index] ?? last)
  nights.value = wanted
  save()
}

function at(event: PointerEvent): {index: number, chance: number} | undefined {
  const box = chart.value?.getBoundingClientRect()
  if (!box || box.width === 0) return undefined
  const index = Math.min(chances.value.length - 1, Math.max(0, Math.floor((event.clientX - box.left) / box.width * chances.value.length)))
  const raw = (1 - (event.clientY - box.top) / box.height) * 100
  return {index, chance: Math.min(100, Math.max(0, Math.round(raw / STEP) * STEP))}
}

function paint(event: PointerEvent): void {
  const hit = at(event)
  if (!hit) return
  active.value = hit.index
  chances.value[hit.index] = hit.chance
}

function press(event: PointerEvent): void {
  if (props.readOnly) return
  dragging.value = true
  chart.value?.setPointerCapture(event.pointerId)
  paint(event)
}

function move(event: PointerEvent): void {
  if (dragging.value) paint(event)
  else active.value = at(event)?.index
}

function release(): void {
  if (!dragging.value) return
  dragging.value = false
  save()
}

function key(index: number, event: KeyboardEvent): void {
  const changes: Record<string, (chance: number) => number> = {
    ArrowUp: chance => chance + STEP,
    ArrowRight: chance => chance + STEP,
    ArrowDown: chance => chance - STEP,
    ArrowLeft: chance => chance - STEP,
    PageUp: chance => chance + 25,
    PageDown: chance => chance - 25,
    Home: () => 0,
    End: () => 100,
  }
  const change = changes[event.key]
  if (!change || props.readOnly) return
  event.preventDefault()
  chances.value[index] = Math.min(100, Math.max(0, change(chances.value[index] ?? 0)))
  active.value = index
  save()
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div class="flex items-center gap-2">
      <span class="text-sm">{{ t('editor.phaseCycle.nights') }}</span>
      <MutedIconButton :disabled="readOnly || chances.length <= 1" :label="t('editor.phaseCycle.fewer')" icon="minus" @click="resize(chances.length - 1)"/>
      <div class="w-20">
        <NumberInput v-model="nights" :aria-label="t('editor.phaseCycle.nights')" :disabled="readOnly" @change="resize(nights)"/>
      </div>
      <MutedIconButton :disabled="readOnly || chances.length >= MAX_NIGHTS" :label="t('editor.phaseCycle.more')" icon="plus" @click="resize(chances.length + 1)"/>
      <span class="ml-auto font-mono text-sm text-ink-muted">
        {{ shown ? t('editor.phaseCycle.night', {night: shown.night, chance: shown.chance}) : t('editor.phaseCycle.hint') }}
      </span>
    </div>
    <div class="rounded-panel border border-line-soft bg-ground p-3">
      <div
          ref="chart"
          :class="readOnly ? '' : 'cursor-ns-resize'"
          class="relative h-44 touch-none select-none"
          @pointercancel="release"
          @pointerdown="press"
          @pointerleave="dragging || (active = undefined)"
          @pointermove="move"
          @pointerup="release"
      >
        <div v-for="line in [25, 50, 75]" :key="line" :style="{bottom: `${line}%`}" class="pointer-events-none absolute inset-x-0 border-t border-dashed border-line-soft"/>
        <div class="absolute inset-0 flex items-end gap-0.5">
          <div
              v-for="(chance, index) in chances"
              :key="index"
              :aria-label="t('editor.phaseCycle.nightLabel', {night: index + 1})"
              :aria-valuenow="chance"
              :aria-valuetext="`${chance} %`"
              :class="index === active ? 'bg-accent' : 'bg-accent/60'"
              :style="{height: `${Math.max(chance, 1)}%`}"
              aria-valuemax="100"
              aria-valuemin="0"
              class="min-w-0 flex-1 rounded-t-sm transition-colors focus-visible:outline-2 focus-visible:outline-focus"
              role="slider"
              tabindex="0"
              @focus="active = index"
              @keydown="key(index, $event)"
          />
        </div>
        <svg class="pointer-events-none absolute inset-0 h-full w-full overflow-visible" preserveAspectRatio="none" viewBox="0 0 100 100">
          <polyline :points="points" class="fill-none stroke-ink" stroke-linejoin="round" stroke-width="1.5" vector-effect="non-scaling-stroke"/>
        </svg>
      </div>
      <div class="mt-1 flex gap-0.5 text-[10px] text-ink-faint">
        <span v-for="(_, index) in chances" :key="index" class="min-w-0 flex-1 text-center">{{ labelled(index) ? index + 1 : '' }}</span>
      </div>
    </div>
    <MutedText v-if="help" size="sm" tag="p">{{ help }}</MutedText>
  </fieldset>
</template>
