/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SchemaForm from '~/views/editorview/form/SchemaForm.vue'
import schema from '~/api/generated/payload.schema.json'
import {fieldsOf, type Field, type SchemaDocument} from '~/util/settings/schemaFields'

/**
 * Some properties of a settings object as a form, each a name or a path into its groups such as `deathActions/mob`.
 * A single property holding a group shows the group's fields directly, so a tab such as Night reads as the night's
 * settings.
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
  const all = fieldsOf(document, props.definition)
  const only = props.properties?.length === 1 ? props.properties[0]?.split('/') : undefined
  if (only) {
    const found = descend(all, props.value, props.path, only)
    if (found?.field.kind === 'group') return {fields: found.field.children, value: found.value, path: found.path}
  }
  const fields = all.filter(field => !props.properties || props.properties.includes(field.key))
  return {fields, value: props.value, path: props.path}
})

function descend(fields: readonly Field[], value: Record<string, unknown> | undefined, path: string, segments: readonly string[]): {field: Field, value: Record<string, unknown> | undefined, path: string} | undefined {
  const [segment, ...rest] = segments
  const field = fields.find(candidate => candidate.key === segment)
  if (!field || segment === undefined) return undefined
  const inner = value?.[segment] as Record<string, unknown> | undefined
  const here = `${path}/${segment}`
  return rest.length === 0 ? {field, value: inner, path: here} : descend(field.children, inner, here, rest)
}
</script>

<template>
  <div class="max-w-3xl">
    <SchemaForm :fields="form.fields" :path="form.path" :read-only="readOnly" :value="form.value"/>
  </div>
</template>
