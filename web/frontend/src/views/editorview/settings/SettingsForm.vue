/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SchemaForm from '~/views/editorview/form/SchemaForm.vue'
import schema from '~/api/generated/payload.schema.json'
import {fieldsOf, type SchemaDocument} from '~/util/settings/schemaFields'

/**
 * Some properties of a settings object as a form. A single property holding a group shows the group's fields
 * directly, so a tab such as Night reads as the night's settings.
 */
const props = defineProps<{
  definition: string
  properties: readonly string[] | undefined
  value: Record<string, unknown> | undefined
  path: string
  readOnly: boolean
}>()

const document = schema as unknown as SchemaDocument

const form = computed(() => {
  const fields = fieldsOf(document, props.definition).filter(field => !props.properties || props.properties.includes(field.key))
  const only = fields.length === 1 ? fields[0] : undefined
  if (only?.kind === 'group') {
    return {fields: only.children, value: props.value?.[only.key] as Record<string, unknown> | undefined, path: `${props.path}/${only.key}`}
  }
  return {fields, value: props.value, path: props.path}
})
</script>

<template>
  <div class="max-w-3xl">
    <SchemaForm :fields="form.fields" :path="form.path" :read-only="readOnly" :value="form.value"/>
  </div>
</template>
