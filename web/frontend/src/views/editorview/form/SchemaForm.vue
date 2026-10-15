/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SchemaField from '~/views/editorview/form/SchemaField.vue'
import type {Field} from '~/util/settings/schemaFields'
import {shownFields} from '~/util/settings/modifiedValues'
import {fieldHelpKey} from '~/util/settings/fieldHelp'

/**
 * A form for an object of the payload, one field per property its schema names, without the settings that do not
 * matter right now, each with its help text where it has one.
 */
const props = defineProps<{
  fields: readonly Field[]
  value: Record<string, unknown> | undefined
  path: string
  readOnly: boolean
}>()

const {t, te} = useI18n()
const shown = computed(() => shownFields(props.fields, props.value))

function help(field: Field): string | undefined {
  const key = fieldHelpKey(field, props.fields, props.value)
  return key && te(key) ? t(key) : undefined
}
</script>

<template>
  <div class="flex flex-col divide-y divide-line-soft">
    <SchemaField
        v-for="field in shown"
        :key="field.key"
        :field="field"
        :help="help(field)"
        :path="`${path}/${field.key}`"
        :read-only="readOnly"
        :value="value?.[field.key]"
    />
  </div>
</template>
