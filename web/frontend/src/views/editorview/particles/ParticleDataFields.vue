/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import ColorInput from '@/components/input/color/ColorInput.vue'
import MutedText from '@/components/typography/MutedText.vue'
import ItemPicker from '~/views/editorview/form/ItemPicker.vue'
import EffectNumber from '~/views/editorview/particles/EffectNumber.vue'
import VecFields from '~/views/editorview/particles/VecFields.vue'
import type {DataKind, ParticleDataSpec} from '~/api/generated/payload'
import {DATA_FIELDS} from '~/util/particles/particleEffects'

/**
 * What a particle is drawn with, for the particles that take data: only the fields its kind of data uses.
 */
const props = defineProps<{
  kind: DataKind
  data: ParticleDataSpec | undefined
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [data: ParticleDataSpec]
}>()

const {t} = useI18n()
const assets = useItemAssets()
const items = assets.choices(true)
const blocks = assets.choices(false)
const fields = computed(() => DATA_FIELDS[props.kind])
const current = computed(() => props.data ?? {})

function save(change: Partial<ParticleDataSpec>): void {
  emit('save', {...current.value, ...change})
}

function towardOther(other: boolean): void {
  save({toward: other ? undefined : {x: 0, y: 1, z: 0}})
}

function color(key: 'color' | 'to', value: string | undefined): void {
  if (value) save({[key]: value})
}
</script>

<template>
  <MutedText v-if="kind === 'UNSUPPORTED'" size="sm" tag="p">{{ t('editor.particles.data.unsupported') }}</MutedText>
  <div v-else-if="fields.length > 0" class="flex flex-wrap items-end gap-3">
    <LabelledField v-if="fields.includes('color')" v-slot="{id}" :label="t('editor.particles.data.color')">
      <ColorInput :id="id" :disabled="readOnly" :model-value="current.color ?? '#ffffff'" @update:model-value="value => color('color', value)"/>
    </LabelledField>
    <LabelledField v-if="fields.includes('to')" v-slot="{id}" :label="t('editor.particles.data.to')">
      <ColorInput :id="id" :disabled="readOnly" :model-value="current.to ?? '#000000'" @update:model-value="value => color('to', value)"/>
    </LabelledField>
    <EffectNumber v-if="fields.includes('size')" :label="t('editor.particles.data.size')" :max="4" :min="0.01" :read-only="readOnly" :value="current.size ?? 1" class="w-28" @save="size => save({size})"/>
    <EffectNumber v-if="fields.includes('power')" :label="t('editor.particles.data.power')" :read-only="readOnly" :value="current.power ?? 1" class="w-28" @save="power => save({power})"/>
    <EffectNumber v-if="fields.includes('delay')" :label="t('editor.particles.data.delay')" :min="0" :read-only="readOnly" :value="current.delay ?? 0" class="w-28" whole @save="delay => save({delay})"/>
    <EffectNumber v-if="fields.includes('ticks')" :label="t('editor.particles.data.ticks')" :min="1" :read-only="readOnly" :value="current.ticks ?? 20" class="w-28" whole @save="ticks => save({ticks})"/>
    <LabelledField v-if="fields.includes('block')" :label="t('editor.particles.data.block')" class="min-w-56 flex-1">
      <ItemPicker :choices="blocks" :label="t('editor.particles.data.block')" :model-value="current.block" :read-only="readOnly" @update:model-value="block => save({block})"/>
    </LabelledField>
    <LabelledField v-if="fields.includes('item')" :label="t('editor.particles.data.item')" class="min-w-56 flex-1">
      <ItemPicker :choices="items" :label="t('editor.particles.data.item')" :model-value="current.item" :read-only="readOnly" @update:model-value="item => save({item})"/>
    </LabelledField>
  </div>
  <ToggleSetting
      v-if="fields.includes('ticks')"
      :disabled="readOnly"
      :hint="t('editor.particles.data.towardOtherHint')"
      :label="t('editor.particles.data.towardOther')"
      :model-value="current.toward === undefined"
      @update:model-value="towardOther"
  />
  <VecFields
      v-if="fields.includes('ticks') && current.toward !== undefined"
      :axes="[t('editor.particles.axes.right'), t('editor.particles.axes.up'), t('editor.particles.axes.forward')]"
      :label="t('editor.particles.data.toward')"
      :read-only="readOnly"
      :value="current.toward"
      @save="toward => save({toward})"
  />
</template>
