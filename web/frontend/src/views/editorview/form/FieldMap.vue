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
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import {pointer} from '~/util/patch/jsonPointer'
import type {Field} from '~/util/settings/schemaFields'

/**
 * Values by key, e.g. a world's mob weights: one row per key, rows added by key and removed. With `keys`, a key is
 * picked from them by its name rather than typed, and rows show the names.
 */
const props = defineProps<{
  label: string
  item: Field
  value: Record<string, unknown> | undefined
  path: string
  readOnly: boolean
  /** The keys that can be added, e.g. the session's mobs. */
  keys?: readonly string[]
  /** Names of the keys. */
  keyLabels?: Readonly<Record<string, string>>
  /** What the picker for a new key says, e.g. "Add a mob". */
  addLabel?: string
}>()

const emit = defineEmits<{
  save: [value: Record<string, unknown>]
}>()

const {t} = useI18n()
const newKey = ref('')

const entries = computed(() => Object.entries(props.value ?? {}))

const addable = computed(() => (props.keys ?? []).filter(key => props.value?.[key] === undefined))

function pick(key: string | undefined): void {
  if (!key) return
  newKey.value = key
  add()
}

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
      <span :class="keyLabels?.[key] ? '' : 'font-mono'" class="w-48 shrink-0 py-5 text-sm">{{ keyLabels?.[key] ?? key }}</span>
      <div class="min-w-0 flex-1">
        <SchemaField :field="{...item, key: 'value'}" :path="`${path}${pointer(key)}`" :read-only="readOnly" :value="entry"/>
      </div>
      <MutedIconButton v-if="!readOnly" :label="t('editor.form.removeKey', {key})" class="mt-4" icon="xmark" @click="remove(key)"/>
    </div>
    <ChoicePicker
        v-if="!readOnly && keys"
        :label="addLabel ?? t('editor.form.addKey')"
        :labels="keyLabels"
        :model-value="undefined"
        :options="addable"
        :placeholder="addLabel"
        @update:model-value="pick"
    />
    <div v-else-if="!readOnly" class="flex items-center gap-2">
      <TextInput v-model="newKey" :aria-label="t('editor.form.newKey')" :placeholder="t('editor.form.newKey')" @keydown.enter="add"/>
      <SecondaryButton compact @click="add">{{ t('editor.form.add') }}</SecondaryButton>
    </div>
  </fieldset>
</template>
