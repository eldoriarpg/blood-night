/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import EffectNumber from '~/views/editorview/particles/EffectNumber.vue'
import type {Vec3} from '~/api/generated/payload'

/**
 * Three numbers of a particle effect that belong together, such as an offset: right, up and forward of the shape's
 * facing, or east, up and south where the labels say so.
 */
const props = defineProps<{
  label: string
  value: Vec3 | undefined
  readOnly: boolean
  /** The names of the three axes. */
  axes: readonly [string, string, string]
}>()

const emit = defineEmits<{
  save: [value: Vec3]
}>()

const current = computed(() => props.value ?? {x: 0, y: 0, z: 0})

function save(axis: 'x' | 'y' | 'z', value: number): void {
  emit('save', {...current.value, [axis]: value})
}
</script>

<template>
  <fieldset class="flex flex-col gap-1">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div class="grid grid-cols-3 gap-2">
      <EffectNumber :label="axes[0]" :read-only="readOnly" :value="current.x" @save="value => save('x', value)"/>
      <EffectNumber :label="axes[1]" :read-only="readOnly" :value="current.y" @save="value => save('y', value)"/>
      <EffectNumber :label="axes[2]" :read-only="readOnly" :value="current.z" @save="value => save('z', value)"/>
    </div>
  </fieldset>
</template>
