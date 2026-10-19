/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import EffectPreview from '~/views/editorview/particles/EffectPreview.vue'
import {effectLabels} from '~/util/particles/particleEffects'

/**
 * A particle effect a mob wears, picked from the library by name, with a small preview of it and a link to open it.
 */
const props = defineProps<{
  label: string
  help?: string
  value: string | undefined
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: string | undefined]
}>()

const session = useEditorSession()
const {openEffect} = useEditorNavigation()
const {t} = useI18n()
const labels = computed(() => effectLabels(session.payload.value))
const options = computed(() => Object.keys(labels.value))
const effect = computed(() => props.value
  ? session.payload.value?.particleEffects?.[props.value] ?? session.payload.value?.builtInEffects?.[props.value]
  : undefined)
</script>

<template>
  <LabelledField :help="help" :label="label">
    <div class="flex flex-wrap items-start gap-3">
      <div class="flex min-w-56 flex-1 flex-col gap-1">
        <ChoicePicker :label="label" :labels="labels" :model-value="value" :options="options" :placeholder="t('editor.particles.none')" :read-only="readOnly" clearable @update:model-value="emit('save', $event)"/>
        <BareButton v-if="value && effect" class="self-start text-sm text-accent hover:underline" @click="openEffect(value)">{{ t('editor.particles.open', {name: labels[value] ?? value}) }}</BareButton>
      </div>
      <EffectPreview v-if="effect" :effect="effect" compact/>
    </div>
  </LabelledField>
</template>
