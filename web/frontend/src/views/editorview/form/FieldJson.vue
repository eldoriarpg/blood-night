/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import TextAreaInput from '@/components/input/text/TextAreaInput.vue'
import FieldHint from '@/components/typography/FieldHint.vue'

/** A value the form has no field for, edited as JSON and saved when it reads and the field is left. */
const props = defineProps<{
  label: string
  value: unknown
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: unknown]
}>()

const {t} = useI18n()
const draft = ref('')
const invalid = ref(false)

watch(() => props.value, value => {
  draft.value = JSON.stringify(value ?? null, null, 2)
}, {immediate: true})

function save(): void {
  try {
    emit('save', JSON.parse(draft.value))
    invalid.value = false
  } catch {
    invalid.value = true
  }
}
</script>

<template>
  <LabelledField v-slot="{id}" :label="label">
    <TextAreaInput :id="id" v-model="draft" :label="label" :disabled="readOnly" :rows="4" mono @focusout="save"/>
    <FieldHint v-if="invalid" class="text-problem">{{ t('editor.form.badJson') }}</FieldHint>
  </LabelledField>
</template>
