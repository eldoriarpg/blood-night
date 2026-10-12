/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SchemaField from '~/views/editorview/form/SchemaField.vue'
import type {Field} from '~/util/settings/schemaFields'

/** A form for an object of the payload, one field per property its schema names. */
defineProps<{
  fields: readonly Field[]
  value: Record<string, unknown> | undefined
  path: string
  readOnly: boolean
}>()
</script>

<template>
  <div class="flex flex-col divide-y divide-line-soft">
    <SchemaField
        v-for="field in fields"
        :key="field.key"
        :field="field"
        :path="`${path}/${field.key}`"
        :read-only="readOnly"
        :value="value?.[field.key]"
    />
  </div>
</template>
