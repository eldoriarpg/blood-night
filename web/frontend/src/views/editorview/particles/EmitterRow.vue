/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import AppIcon from '@/components/display/AppIcon.vue'
import BareButton from '@/components/button/BareButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import AnchorFields from '~/views/editorview/particles/AnchorFields.vue'
import EffectNumber from '~/views/editorview/particles/EffectNumber.vue'
import MotionFields from '~/views/editorview/particles/MotionFields.vue'
import ParticleDataFields from '~/views/editorview/particles/ParticleDataFields.vue'
import ParticlePicker from '~/views/editorview/particles/ParticlePicker.vue'
import ShapeFields from '~/views/editorview/particles/ShapeFields.vue'
import VecFields from '~/views/editorview/particles/VecFields.vue'
import type {Emitter} from '~/api/generated/payload'

/**
 * One emitter of a particle effect, folding: the particle and its data, where it sits, its shape, how it travels, and
 * how many particles it draws how often. Each change is saved as the whole emitter.
 */
const props = defineProps<{
  emitter: Emitter
  index: number
  pieces: number
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [emitter: Emitter]
  remove: []
}>()

const {t} = useI18n()
const session = useEditorSession()
const open = ref(props.index === 0)
const particle = computed(() => props.emitter.particle ?? 'minecraft:flame')
const kind = computed(() => session.payload.value?.choices?.particles?.[particle.value] ?? 'NONE')
const axes = computed(() => [t('editor.particles.axes.right'), t('editor.particles.axes.up'), t('editor.particles.axes.forward')] as const)

function save(change: Partial<Emitter>): void {
  emit('save', {...props.emitter, ...change})
}
</script>

<template>
  <li :aria-label="t('editor.particles.emitter', {number: index + 1})" class="flex flex-col gap-3 rounded-panel border border-line-soft p-3">
    <div class="flex items-center gap-2">
      <BareButton :aria-expanded="open" class="flex items-center gap-2" @click="open = !open">
        <AppIcon :class="open ? '' : '-rotate-90'" class="text-xs text-ink-faint transition-transform" icon="chevron-down"/>
        <SectionLabel>{{ t('editor.particles.emitter', {number: index + 1}) }}</SectionLabel>
      </BareButton>
      <ParticlePicker :label="t('editor.particles.particle')" :model-value="particle" :read-only="readOnly" class="min-w-56 flex-1" @update:model-value="particle => save({particle, data: {}})"/>
      <MutedIconButton v-if="!readOnly" :label="t('editor.particles.removeEmitter')" hover="error" icon="trash" @click="emit('remove')"/>
    </div>
    <template v-if="open">
      <ParticleDataFields :data="emitter.data" :kind="kind" :read-only="readOnly" @save="data => save({data})"/>
      <AnchorFields :anchor="emitter.anchor" :pieces="pieces" :read-only="readOnly" @save="anchor => save({anchor})"/>
      <VecFields :axes="axes" :label="t('editor.particles.offset')" :read-only="readOnly" :value="emitter.offset" @save="offset => save({offset})"/>
      <ShapeFields :read-only="readOnly" :shape="emitter.shape" @save="shape => save({shape})"/>
      <MotionFields :motion="emitter.motion" :read-only="readOnly" @save="motion => save({motion})"/>
      <div class="flex flex-wrap items-end gap-3">
        <EffectNumber :help="t('editor.particles.countHelp')" :label="t('editor.particles.count')" :max="256" :min="1" :read-only="readOnly" :value="emitter.count" class="w-32" whole @save="count => save({count})"/>
        <EffectNumber :label="t('editor.particles.speed')" :min="0" :read-only="readOnly" :value="emitter.speed" class="w-32" @save="speed => save({speed})"/>
        <EffectNumber :label="t('editor.particles.every')" :max="600" :min="1" :read-only="readOnly" :value="emitter.every" class="w-32" whole @save="every => save({every})"/>
        <EffectNumber :label="t('editor.particles.delay')" :max="600" :min="0" :read-only="readOnly" :value="emitter.delay" class="w-32" whole @save="delay => save({delay})"/>
        <EffectNumber :label="t('editor.particles.chance')" :max="100" :min="0" :read-only="readOnly" :value="Math.round((emitter.chance ?? 1) * 100)" class="w-32" whole @save="percent => save({chance: percent / 100})"/>
      </div>
      <VecFields :axes="[t('editor.particles.axes.east'), t('editor.particles.axes.up'), t('editor.particles.axes.south')]" :label="t('editor.particles.spread')" :read-only="readOnly" :value="emitter.spread" @save="spread => save({spread})"/>
    </template>
  </li>
</template>
