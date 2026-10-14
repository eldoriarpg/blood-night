/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import DecimalInput from '@/components/input/number/DecimalInput.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import FieldList from '~/views/editorview/form/FieldList.vue'
import FieldMap from '~/views/editorview/form/FieldMap.vue'
import FieldChoices from '~/views/editorview/form/FieldChoices.vue'
import FieldTexts from '~/views/editorview/form/FieldTexts.vue'
import FieldJson from '~/views/editorview/form/FieldJson.vue'
import SchemaForm from '~/views/editorview/form/SchemaForm.vue'
import {fieldLabel} from '~/views/editorview/form/fieldLabel'
import {fieldOptions} from '~/util/settings/fieldOptions'
import type {Field} from '~/util/settings/schemaFields'
import {isItemField} from '~/util/session/itemEntries'
import ItemPicker from '~/views/editorview/form/ItemPicker.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'

/**
 * One field of a settings form, drawn by its kind; a change is saved as soon as it is made, or when a typed value
 * is left. Fields naming an item pick one
 * of the payload's items or the server's materials with icons and names. A MythicMobs type is picked from the
 * server's MythicMobs mobs when it has any. Choices show their names in the editor's
 * language and store their keys; long lists of choices are searched rather than scrolled.
 */
const props = defineProps<{
  field: Field
  value: unknown
  path: string
  readOnly: boolean
}>()

const changes = useEditorChanges()
const session = useEditorSession()
const {t, te} = useI18n()

const label = computed(() => fieldLabel(props.field.key, t, te))
const current = computed(() => props.value ?? props.field.defaultValue)
const draft = ref<string | number | undefined>(undefined)
const numberDraft = computed<number | undefined>({
  get: () => typeof draft.value === 'number' ? draft.value : undefined,
  set: value => {
    draft.value = value
  },
})
const textDraft = computed<string | undefined>({
  get: () => typeof draft.value === 'string' ? draft.value : undefined,
  set: value => {
    draft.value = value
  },
})
const listValue = computed(() => Array.isArray(current.value) ? current.value as unknown[] : undefined)
const textsValue = computed(() => Array.isArray(current.value) ? current.value as string[] : undefined)
const objectValue = computed(() => current.value !== null && typeof current.value === 'object' && !Array.isArray(current.value) ? current.value as Record<string, unknown> : undefined)
const itemField = computed(() => props.field.kind === 'text' && isItemField(props.field.key))
const itemChoices = useItemAssets().choices(true)
const choiceValue = computed(() => typeof current.value === 'string' ? current.value : null)
const choiceLabels = useChoiceLabels()
const options = computed(() => fieldOptions(props.field, session.payload.value?.choices?.mobTypes ?? [], current.value))
const optionLabels = computed(() => choiceLabels.labels(options.value, {enumName: props.field.enumName}))
const mythicTypes = computed(() => session.payload.value?.choices?.mythicTypes ?? [])
const mythicField = computed(() => props.field.key === 'mythicType' && mythicTypes.value.length > 0)
const mythicOptions = computed(() => choiceValue.value && !mythicTypes.value.includes(choiceValue.value)
  ? [choiceValue.value, ...mythicTypes.value]
  : mythicTypes.value)

watch(current, value => {
  draft.value = typeof value === 'number' || typeof value === 'string' ? value : undefined
}, {immediate: true})

function save(value: unknown): void {
  if (props.readOnly || JSON.stringify(value) === JSON.stringify(props.value)) return
  void changes.apply([{op: 'add', path: props.path, value}])
}
</script>

<template>
  <div class="py-3">
    <ToggleSetting v-if="field.kind === 'boolean'" :disabled="readOnly" :label="label" :model-value="Boolean(current)" @update:model-value="save"/>
    <LabelledField v-else-if="field.kind === 'integer'" v-slot="{id}" :label="label">
      <NumberInput :id="id" v-model="numberDraft" :disabled="readOnly" @focusout="save(draft)"/>
    </LabelledField>
    <LabelledField v-else-if="field.kind === 'number'" v-slot="{id}" :label="label">
      <DecimalInput :id="id" v-model="numberDraft" :disabled="readOnly" @focusout="save(draft)"/>
    </LabelledField>
    <LabelledField v-else-if="itemField" :label="label">
      <ItemPicker :choices="itemChoices" :label="label" :model-value="textDraft" :read-only="readOnly" clearable @update:model-value="save"/>
    </LabelledField>
    <LabelledField v-else-if="mythicField" :label="label">
      <ChoicePicker :label="label" :model-value="choiceValue || undefined" :options="mythicOptions" :read-only="readOnly" clearable @update:model-value="save($event ?? '')"/>
    </LabelledField>
    <LabelledField v-else-if="field.kind === 'text'" v-slot="{id}" :label="label">
      <TextInput :id="id" v-model="textDraft" :disabled="readOnly" @focusout="save(draft)"/>
    </LabelledField>
    <LabelledField v-else-if="field.kind === 'choice'" :label="label">
      <ChoicePicker :label="label" :labels="optionLabels" :model-value="choiceValue ?? undefined" :options="options" :read-only="readOnly" @update:model-value="save"/>
    </LabelledField>
    <FieldChoices v-else-if="field.kind === 'choices'" :label="label" :labels="optionLabels" :options="options" :read-only="readOnly" :value="textsValue" @save="save"/>
    <FieldTexts v-else-if="field.kind === 'texts'" :label="label" :read-only="readOnly" :value="textsValue" @save="save"/>
    <div v-else-if="field.kind === 'group'" class="flex flex-col gap-1">
      <SubHeader>{{ label }}</SubHeader>
      <SchemaForm :fields="field.children" :path="path" :read-only="readOnly" :value="objectValue ?? {}"/>
    </div>
    <FieldMap v-else-if="field.kind === 'map' && field.item" :item="field.item" :label="label" :path="path" :read-only="readOnly" :value="objectValue" @save="save"/>
    <FieldList v-else-if="field.kind === 'list' && field.item" :item="field.item" :label="label" :path="path" :read-only="readOnly" :value="listValue" @save="save"/>
    <FieldJson v-else :label="label" :read-only="readOnly" :value="current" @save="save"/>
  </div>
</template>
