/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import NewIdDialog from '~/views/editorview/form/NewIdDialog.vue'
import type {EditorPayload, ParticleEffect} from '~/api/generated/payload'
import {addEffect, effectEntries, effectLabels, newEffect} from '~/util/particles/particleEffects'

/**
 * Creates a particle effect by id and name, blank or as a copy of another effect, e.g. a built-in one to change.
 */
const open = defineModel<boolean>({required: true})

const props = defineProps<{
  /** The effect to start from, when opened to copy one. */
  from?: ParticleEffect
}>()

const emit = defineEmits<{
  created: [id: string]
}>()

const session = useEditorSession()
const {t} = useI18n()
const name = ref('')
const start = ref<string | undefined>(undefined)
const entries = computed(() => effectEntries(session.payload.value))
const labels = computed(() => effectLabels(session.payload.value))
const taken = computed(() => entries.value.map(entry => entry.id))

watch(open, now => {
  if (!now) return
  name.value = props.from ? t('editor.particles.copyName', {name: props.from.name}) : ''
  start.value = undefined
})

function operations(payload: EditorPayload, id: string) {
  const base = props.from ?? entries.value.find(entry => entry.id === start.value)?.effect
  return addEffect(payload, id, newEffect(name.value, base))
}

function created(id: string): void {
  name.value = ''
  emit('created', id)
}
</script>

<template>
  <NewIdDialog v-model="open" :operations="operations" :ready="name.trim() !== ''" :taken="taken" :title="t('editor.particles.new')" @created="created">
    <LabelledField v-slot="{id}" :label="t('editor.particles.name')">
      <TextInput :id="id" v-model="name" :placeholder="t('editor.particles.namePlaceholder')"/>
    </LabelledField>
    <LabelledField v-if="!from" :label="t('editor.particles.startFrom')">
      <ChoicePicker v-model="start" :label="t('editor.particles.startFrom')" :labels="labels" :options="taken" :placeholder="t('editor.particles.blank')" clearable/>
    </LabelledField>
  </NewIdDialog>
</template>
