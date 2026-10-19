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
import VecFields from '~/views/editorview/particles/VecFields.vue'
import PatternFields from '~/views/editorview/particles/PatternFields.vue'
import type {Facing, Shape, ShapeType} from '~/api/generated/payload'
import {MAX_POINTS, shapeOf} from '~/util/particles/shapes'

/**
 * Where an emitter's particles appear around its anchor: the kind of shape, the fields that kind uses, and how it is
 * turned, spun and grown.
 */
const props = defineProps<{
  shape: Shape | undefined
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [shape: Shape]
}>()

const {t} = useI18n()
const TYPES: readonly ShapeType[] = ['POINT', 'RING', 'SPHERE', 'LINE', 'SPIRAL', 'BEAM', 'ARC', 'PATTERN']
const FACINGS: readonly Facing[] = ['UP', 'LOOK', 'BODY', 'PATH']
const current = computed(() => shapeOf(props.shape))
const uses = computed(() => FIELDS[current.value.type])

const FIELDS: Readonly<Record<ShapeType, readonly string[]>> = {
    POINT: [],
    RING: ['radius', 'points'],
    SPHERE: ['radius', 'points'],
    LINE: ['to', 'points'],
    SPIRAL: ['radius', 'height', 'turns', 'points', 'strands'],
    BEAM: ['density', 'jitter', 'to'],
    ARC: ['density', 'jitter', 'bend', 'twist', 'to'],
    PATTERN: ['pattern'],
}

function save(change: Partial<Shape>): void {
  emit('save', {...current.value, ...change})
}
</script>

<template>
  <div class="flex flex-col gap-3">
    <div class="flex flex-wrap items-end gap-3">
      <LabelledField v-slot="{id}" :label="t('editor.particles.shape.type')" class="w-44">
        <SelectInput :id="id" :disabled="readOnly" :model-value="current.type" @update:model-value="type => save({type: type as ShapeType})">
          <option v-for="type in TYPES" :key="type" :value="type">{{ t(`editor.particles.shape.types.${type}`) }}</option>
        </SelectInput>
      </LabelledField>
      <LabelledField v-slot="{id}" :label="t('editor.particles.shape.facing')" class="w-48">
        <SelectInput :id="id" :disabled="readOnly" :model-value="current.facing" @update:model-value="facing => save({facing: facing as Facing})">
          <option v-for="facing in FACINGS" :key="facing" :value="facing">{{ t(`editor.particles.shape.facings.${facing}`) }}</option>
        </SelectInput>
      </LabelledField>
      <EffectNumber v-if="uses.includes('radius')" :label="t('editor.particles.shape.radius')" :min="0" :read-only="readOnly" :value="current.radius" class="w-28" @save="radius => save({radius})"/>
      <EffectNumber v-if="uses.includes('points')" :label="t('editor.particles.shape.points')" :max="MAX_POINTS" :min="1" :read-only="readOnly" :value="current.points" class="w-28" whole @save="points => save({points})"/>
      <EffectNumber v-if="uses.includes('height')" :label="t('editor.particles.shape.height')" :read-only="readOnly" :value="current.height" class="w-28" @save="height => save({height})"/>
      <EffectNumber v-if="uses.includes('turns')" :label="t('editor.particles.shape.turns')" :read-only="readOnly" :value="current.turns" class="w-28" @save="turns => save({turns})"/>
      <EffectNumber v-if="uses.includes('strands')" :label="t('editor.particles.shape.strands')" :max="8" :min="1" :read-only="readOnly" :value="current.strands" class="w-28" whole @save="strands => save({strands})"/>
      <EffectNumber v-if="uses.includes('density')" :label="t('editor.particles.shape.density')" :max="16" :min="0.1" :read-only="readOnly" :value="current.density" class="w-28" @save="density => save({density})"/>
      <EffectNumber v-if="uses.includes('jitter')" :label="t('editor.particles.shape.jitter')" :min="0" :read-only="readOnly" :value="current.jitter" class="w-28" @save="jitter => save({jitter})"/>
      <EffectNumber v-if="uses.includes('bend')" :label="t('editor.particles.shape.bend')" :read-only="readOnly" :value="current.bend" class="w-28" @save="bend => save({bend})"/>
      <EffectNumber v-if="uses.includes('twist')" :label="t('editor.particles.shape.twist')" :read-only="readOnly" :value="current.twist" class="w-28" @save="twist => save({twist})"/>
      <EffectNumber v-if="current.type !== 'POINT'" :label="t('editor.particles.shape.spin')" :read-only="readOnly" :value="current.spin" class="w-28" @save="spin => save({spin})"/>
    </div>
    <VecFields
        v-if="uses.includes('to')"
        :axes="[t('editor.particles.axes.right'), t('editor.particles.axes.up'), t('editor.particles.axes.forward')]"
        :label="t(current.type === 'LINE' ? 'editor.particles.shape.to' : 'editor.particles.shape.toAlone')"
        :read-only="readOnly"
        :value="current.to"
        @save="to => save({to})"
    />
    <PatternFields v-if="uses.includes('pattern')" :read-only="readOnly" :shape="current" @save="save"/>
    <ToggleSetting v-if="current.type !== 'POINT'" :disabled="readOnly" :hint="t('editor.particles.shape.growHint')" :label="t('editor.particles.shape.grow')" :model-value="current.grow" @update:model-value="grow => save({grow})"/>
  </div>
</template>
