/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import Alert from '@/components/feedback/Alert.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import MutedText from '@/components/typography/MutedText.vue'
import EffectNumber from '~/views/editorview/particles/EffectNumber.vue'
import type {ParticleEffect} from '~/api/generated/payload'
import {COSTLY, LIMITS, effectCost, limitProblems} from '~/util/particles/particleEffects'

/**
 * An effect's name, how long one play lasts, whether a player it plays on sees it, about how many particles it sends,
 * and what breaks the server's limits.
 */
const props = defineProps<{
  effect: ParticleEffect
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [change: Partial<ParticleEffect>]
}>()

const {t} = useI18n()
const name = ref('')
const cost = computed(() => effectCost(props.effect))
const problems = computed(() => limitProblems(props.effect))

function ownView(own: boolean): void {
  emit('save', {viewDistance: own ? LIMITS.startView : undefined})
}

watch(() => props.effect.name, value => {
  name.value = value ?? ''
}, {immediate: true})

function saveName(): void {
  const trimmed = name.value.trim()
  if (!props.readOnly && trimmed !== '' && trimmed !== (props.effect.name ?? '')) emit('save', {name: trimmed})
}
</script>

<template>
  <div class="flex flex-col gap-3">
    <div class="flex flex-wrap items-end gap-3">
      <LabelledField v-slot="{id}" :label="t('editor.particles.name')" class="min-w-56 flex-1">
        <TextInput :id="id" v-model="name" :disabled="readOnly" @focusout="saveName"/>
      </LabelledField>
      <EffectNumber :help="t('editor.particles.durationHelp')" :label="t('editor.particles.duration')" :max="LIMITS.duration" :min="0" :read-only="readOnly" :value="effect.duration" class="w-44" whole @save="duration => emit('save', {duration})"/>
    </div>
    <ToggleSetting :disabled="readOnly" :hint="t('editor.particles.ownViewHint')" :label="t('editor.particles.ownView')" :model-value="effect.viewDistance !== undefined" @update:model-value="ownView"/>
    <EffectNumber v-if="effect.viewDistance !== undefined" :label="t('editor.particles.viewDistance')" :max="LIMITS.maxView" :min="LIMITS.minView" :read-only="readOnly" :value="effect.viewDistance" class="w-44" whole @save="viewDistance => emit('save', {viewDistance})"/>
    <ToggleSetting :disabled="readOnly" :hint="t('editor.particles.selfHint')" :label="t('editor.particles.self')" :model-value="effect.self ?? true" @update:model-value="self => emit('save', {self})"/>
    <MutedText size="sm" tag="p">{{ t(cost.total ? 'editor.particles.inTotal' : 'editor.particles.perSecond', {count: cost.count}) }}</MutedText>
    <Alert v-if="cost.count > COSTLY" variant="held">{{ t(cost.total ? 'editor.particles.costlyTotal' : 'editor.particles.costly', {count: cost.count}) }}</Alert>
    <Alert v-for="problem in problems" :key="problem.key + JSON.stringify(problem.values)" variant="error">{{ t(problem.key, problem.values) }}</Alert>
  </div>
</template>
