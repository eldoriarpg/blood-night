/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import TextAreaInput from '@/components/input/text/TextAreaInput.vue'
import FieldHint from '@/components/typography/FieldHint.vue'

/** A list of texts, e.g. commands: one per line, saved when the field is left. */
const props = defineProps<{
  label: string
  value: readonly string[] | undefined
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: string[]]
}>()

const {t} = useI18n()
const draft = ref('')

watch(() => props.value, value => {
  draft.value = (value ?? []).join('\n')
}, {immediate: true})

function save(): void {
  emit('save', draft.value.split('\n').map(line => line.trim()).filter(line => line !== ''))
}
</script>

<template>
  <LabelledField v-slot="{id}" :label="label">
    <TextAreaInput :id="id" v-model="draft" :label="label" :disabled="readOnly" mono @focusout="save"/>
    <FieldHint>{{ t('editor.form.onePerLine') }}</FieldHint>
  </LabelledField>
</template>
