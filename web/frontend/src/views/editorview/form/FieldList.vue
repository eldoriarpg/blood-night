/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SchemaForm from '~/views/editorview/form/SchemaForm.vue'
import type {Field} from '~/util/settings/schemaFields'
import {jsonClone} from '~/util/patch/jsonPatch'

/** A list of settings, e.g. ambience sounds: one block per entry, entries added and removed. */
const props = defineProps<{
  label: string
  item: Field
  value: readonly unknown[] | undefined
  path: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: unknown[]]
}>()

const {t} = useI18n()

function add(): void {
  emit('save', [...(props.value ?? []), jsonClone(props.item.defaultValue ?? {})])
}

function remove(index: number): void {
  emit('save', (props.value ?? []).filter((_, candidate) => candidate !== index))
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div v-for="(entry, index) in value ?? []" :key="index" class="flex items-start gap-2 rounded-panel border border-line-soft px-3">
      <div class="min-w-0 flex-1">
        <SchemaForm :fields="item.children" :path="`${path}/${index}`" :read-only="readOnly" :value="entry as Record<string, unknown>"/>
      </div>
      <MutedIconButton v-if="!readOnly" :label="t('editor.form.removeEntry', {number: index + 1})" class="mt-3" icon="xmark" @click="remove(index)"/>
    </div>
    <div v-if="!readOnly"><SecondaryButton compact @click="add">{{ t('editor.form.addEntry') }}</SecondaryButton></div>
  </fieldset>
</template>
