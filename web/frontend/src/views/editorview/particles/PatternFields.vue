/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import FieldLabel from '@/components/typography/FieldLabel.vue'
import MutedText from '@/components/typography/MutedText.vue'
import EffectNumber from '~/views/editorview/particles/EffectNumber.vue'
import type {Shape} from '~/api/generated/payload'
import {MAX_CELLS} from '~/util/particles/shapes'

/**
 * A picture drawn with particles, for wings, runes and sigils: a grid of up to 16 by 16 cells to click points on, how
 * far apart the cells are, whether the picture is copied to the other side, and how its halves flap.
 */
const props = defineProps<{
  shape: Shape
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [change: Partial<Shape>]
}>()

const {t} = useI18n()
const SIZE = MAX_CELLS
const rows = computed(() => Array.from({length: SIZE}, (_, row) => (props.shape.rows?.[row] ?? '').padEnd(SIZE, '.').slice(0, SIZE)))

function filled(row: number, column: number): boolean {
  const cell = rows.value[row]?.[column]
  return cell !== undefined && cell !== '.' && cell !== ' '
}

function toggle(row: number, column: number): void {
  if (props.readOnly) return
  const next = rows.value.map((line, index) => index !== row
    ? line
    : line.slice(0, column) + (filled(row, column) ? '.' : 'x') + line.slice(column + 1))
  emit('save', {rows: trimmed(next)})
}

function trimmed(lines: string[]): string[] {
  const width = Math.max(0, ...lines.map(line => line.replace(/\.+$/, '').length))
  let height = lines.length
  while (height > 0 && !lines[height - 1]?.includes('x')) height--
  return lines.slice(0, height).map(line => line.slice(0, width))
}

function flap(on: boolean): void {
  emit('save', {flap: on ? {degrees: 25, ticks: 16} : undefined})
}
</script>

<template>
  <div class="flex flex-wrap gap-4">
    <div class="flex flex-col gap-1">
      <FieldLabel>{{ t('editor.particles.pattern.picture') }}</FieldLabel>
      <div :aria-label="t('editor.particles.pattern.picture')" class="grid w-fit gap-px rounded-control border border-line bg-line-soft p-px" role="grid" style="grid-template-columns: repeat(16, 1rem)">
        <template v-for="(line, row) in rows" :key="row">
          <BareButton
              v-for="column in SIZE"
              :key="column"
              :aria-label="t('editor.particles.pattern.cell', {row: row + 1, column})"
              :aria-pressed="filled(row, column - 1)"
              :class="filled(row, column - 1) ? 'bg-accent' : 'bg-surface hover:bg-raised'"
              :disabled="readOnly"
              class="h-4 w-4"
              @click="toggle(row, column - 1)"
          />
        </template>
      </div>
      <MutedText size="sm" tag="p">{{ t(shape.mirror ? 'editor.particles.pattern.mirroredHint' : 'editor.particles.pattern.hint') }}</MutedText>
    </div>
    <div class="flex min-w-56 flex-1 flex-col gap-3">
      <EffectNumber :label="t('editor.particles.pattern.scale')" :min="0.01" :read-only="readOnly" :value="shape.scale" @save="scale => emit('save', {scale})"/>
      <ToggleSetting :disabled="readOnly" :label="t('editor.particles.pattern.mirror')" :model-value="shape.mirror ?? false" @update:model-value="mirror => emit('save', {mirror})"/>
      <ToggleSetting :disabled="readOnly" :hint="t('editor.particles.pattern.flapHint')" :label="t('editor.particles.pattern.flap')" :model-value="shape.flap !== undefined" @update:model-value="flap"/>
      <div v-if="shape.flap" class="grid grid-cols-2 gap-2">
        <EffectNumber :label="t('editor.particles.pattern.degrees')" :read-only="readOnly" :value="shape.flap.degrees" @save="degrees => emit('save', {flap: {ticks: 16, ...shape.flap, degrees}})"/>
        <EffectNumber :label="t('editor.particles.pattern.ticks')" :min="2" :read-only="readOnly" :value="shape.flap.ticks" whole @save="ticks => emit('save', {flap: {degrees: 25, ...shape.flap, ticks}})"/>
      </div>
    </div>
  </div>
</template>
