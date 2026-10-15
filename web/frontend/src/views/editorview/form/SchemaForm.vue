/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SchemaField from '~/views/editorview/form/SchemaField.vue'
import FieldPause from '~/views/editorview/form/FieldPause.vue'
import type {Field} from '~/util/settings/schemaFields'
import {shownFields} from '~/util/settings/modifiedValues'
import {WORLD_KIND, worldShows} from '~/util/settings/worldKind'
import {fieldHelpKey} from '~/util/settings/fieldHelp'
import {groupKey} from '~/util/settings/specialFields'

/**
 * A form for an object of the payload, one field per property its schema names, without the settings that do not
 * matter right now, each with its help text where it has one, the group's own first (`settings.helpIn`). A shortest
 * and a longest pause (`minInterval`, `maxInterval`) are one range field. A MythicMobs type is only offered on a
 * server with MythicMobs mobs, or where one is set and can be cleared.
 */
const props = defineProps<{
  fields: readonly Field[]
  value: Record<string, unknown> | undefined
  path: string
  readOnly: boolean
}>()

const {t, te} = useI18n()
const session = useEditorSession()
const world = inject(WORLD_KIND, ref(undefined))
const mythicMobs = computed(() => (session.payload.value?.choices?.mythicTypes ?? []).length > 0)
const shown = computed(() => shownFields(props.fields, props.value)
  .filter(field => worldShows(field, props.path.split('/').at(-1), world.value))
  .filter(field => field.key !== 'mythicType' || mythicMobs.value || Boolean(props.value?.mythicType)))
const pause = computed(() => shown.value.some(field => field.key === 'minInterval') && shown.value.some(field => field.key === 'maxInterval'))

function number(key: string): number {
  const value = props.value?.[key] ?? props.fields.find(field => field.key === key)?.defaultValue
  return typeof value === 'number' ? value : 1
}

function help(field: Field): string | undefined {
  const inGroup = `settings.helpIn.${groupKey(props.path)}.${field.key}`
  if (te(inGroup)) return t(inGroup)
  const key = fieldHelpKey(field, props.fields, props.value)
  return key && te(key) ? t(key) : undefined
}
</script>

<template>
  <div class="flex flex-col divide-y divide-line-soft">
    <template v-for="field in shown" :key="field.key">
    <FieldPause
        v-if="pause && field.key === 'minInterval'"
        :label="t('editor.sounds.pause')"
        :longest="number('maxInterval')"
        :path="path"
        :read-only="readOnly"
        :shortest="number('minInterval')"
        class="py-3"
    />
    <SchemaField
        v-else-if="!(pause && field.key === 'maxInterval')"
        :field="field"
        :help="help(field)"
        :path="`${path}/${field.key}`"
        :read-only="readOnly"
        :value="value?.[field.key]"
    />
    </template>
  </div>
</template>
