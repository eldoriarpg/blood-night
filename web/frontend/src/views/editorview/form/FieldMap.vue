/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SchemaField from '~/views/editorview/form/SchemaField.vue'
import {pointer} from '~/util/patch/jsonPointer'
import type {Field} from '~/util/settings/schemaFields'

/** Values by key, e.g. a world's mob weights: one row per key, rows added by key and removed. */
const props = defineProps<{
  label: string
  item: Field
  value: Record<string, unknown> | undefined
  path: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: Record<string, unknown>]
}>()

const {t} = useI18n()
const newKey = ref('')

const entries = computed(() => Object.entries(props.value ?? {}))

function add(): void {
  const key = newKey.value.trim()
  if (key === '' || props.value?.[key] !== undefined) return
  emit('save', {...props.value, [key]: props.item.defaultValue ?? (props.item.kind === 'integer' || props.item.kind === 'number' ? 0 : null)})
  newKey.value = ''
}

function remove(key: string): void {
  emit('save', Object.fromEntries(entries.value.filter(([candidate]) => candidate !== key)))
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div v-for="[key, entry] in entries" :key="key" class="flex items-start gap-3 border-b border-line-soft">
      <span class="w-48 shrink-0 py-5 font-mono text-sm">{{ key }}</span>
      <div class="min-w-0 flex-1">
        <SchemaField :field="{...item, key: 'value'}" :path="`${path}${pointer(key)}`" :read-only="readOnly" :value="entry"/>
      </div>
      <MutedIconButton v-if="!readOnly" :label="t('editor.form.removeKey', {key})" class="mt-4" icon="xmark" @click="remove(key)"/>
    </div>
    <div v-if="!readOnly" class="flex items-center gap-2">
      <TextInput v-model="newKey" :aria-label="t('editor.form.newKey')" :placeholder="t('editor.form.newKey')" @keydown.enter="add"/>
      <SecondaryButton compact @click="add">{{ t('editor.form.add') }}</SecondaryButton>
    </div>
  </fieldset>
</template>
