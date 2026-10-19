/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import SelectInput from '@/components/input/select/SelectInput.vue'
import EffectNumber from '~/views/editorview/particles/EffectNumber.vue'
import type {Anchor, BodyPoint, EffectEnd} from '~/api/generated/payload'

/**
 * Where an emitter or a piece sits: a point on the body of the source or the target, or one of the effect's pieces,
 * and the point on the other end's body it aims at, e.g. from the eyes to the chest.
 */
const props = defineProps<{
  anchor: Anchor | undefined
  readOnly: boolean
  /** How many pieces the effect has; emitters can sit on one of them. None for pieces themselves. */
  pieces?: number
}>()

const emit = defineEmits<{
  save: [anchor: Anchor]
}>()

const {t} = useI18n()
const POINTS: readonly BodyPoint[] = ['FEET', 'CENTER', 'EYES', 'ABOVE']
const current = computed<Anchor>(() => ({at: 'CENTER', on: 'SOURCE', piece: 0, ...props.anchor}))
const ends = computed<EffectEnd[]>(() => (props.pieces ?? 0) > 0 ? ['SOURCE', 'TARGET', 'PIECE'] : ['SOURCE', 'TARGET'])

function save(change: Partial<Anchor>): void {
  emit('save', {...current.value, ...change})
}
</script>

<template>
  <div class="flex flex-wrap items-end gap-3">
    <LabelledField v-slot="{id}" :label="t('editor.particles.anchor.on')" class="w-40">
      <SelectInput :id="id" :disabled="readOnly" :model-value="current.on" @update:model-value="on => save({on: on as EffectEnd})">
        <option v-for="end in ends" :key="end" :value="end">{{ t(`editor.particles.anchor.ends.${end}`) }}</option>
      </SelectInput>
    </LabelledField>
    <LabelledField v-if="current.on !== 'PIECE'" v-slot="{id}" :label="t('editor.particles.anchor.at')" class="w-40">
      <SelectInput :id="id" :disabled="readOnly" :model-value="current.at" @update:model-value="at => save({at: at as BodyPoint})">
        <option v-for="point in POINTS" :key="point" :value="point">{{ t(`editor.particles.anchor.points.${point}`) }}</option>
      </SelectInput>
    </LabelledField>
    <LabelledField v-if="current.on !== 'PIECE'" v-slot="{id}" :label="t('editor.particles.anchor.aim')" class="w-40">
      <SelectInput :id="id" :disabled="readOnly" :model-value="current.aim ?? ''" @update:model-value="aim => save({aim: aim === '' ? undefined : aim as BodyPoint})">
        <option value="">{{ t('editor.particles.anchor.sameAim') }}</option>
        <option v-for="point in POINTS" :key="point" :value="point">{{ t(`editor.particles.anchor.points.${point}`) }}</option>
      </SelectInput>
    </LabelledField>
    <EffectNumber
        v-if="current.on === 'PIECE'"
        :label="t('editor.particles.anchor.piece')"
        :max="pieces ?? 1"
        :min="1"
        :read-only="readOnly"
        :value="(current.piece ?? 0) + 1"
        class="w-28"
        whole
        @save="piece => save({piece: piece - 1})"
    />
  </div>
</template>
