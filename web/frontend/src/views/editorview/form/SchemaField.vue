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
import FieldSounds from '~/views/editorview/form/FieldSounds.vue'
import FieldSpecial from '~/views/editorview/form/FieldSpecial.vue'
import {groupKey, parentKey, specialField} from '~/util/settings/specialFields'
import type {SoundEntry} from '~/api/generated/payload'
import FieldMap from '~/views/editorview/form/FieldMap.vue'
import FieldChoices from '~/views/editorview/form/FieldChoices.vue'
import FieldTexts from '~/views/editorview/form/FieldTexts.vue'
import FieldSuggestedTexts from '~/views/editorview/form/FieldSuggestedTexts.vue'
import FieldJson from '~/views/editorview/form/FieldJson.vue'
import SchemaForm from '~/views/editorview/form/SchemaForm.vue'
import MutedText from '@/components/typography/MutedText.vue'
import {fieldLabel} from '~/views/editorview/form/fieldLabel'
import {fieldOptions, spawnReasonOptions} from '~/util/settings/fieldOptions'
import type {Field} from '~/util/settings/schemaFields'
import {isItemField} from '~/util/session/itemEntries'
import ItemPicker from '~/views/editorview/form/ItemPicker.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import {attributeLabels, suggestionsFor} from '~/util/settings/serverSuggestions'
import {biomeTagName, biomeWords} from '~/util/settings/biomeNames'

/**
 * One field of a settings form, drawn by its kind; a change is saved as soon as it is made, or when a typed value
 * is left. Fields naming an item pick one
 * of the payload's items or the server's materials with icons and names. A MythicMobs type is picked from the
 * server's MythicMobs mobs when it has any, an attribute from the server's attributes. Lists of worlds and biomes
 * suggest the server's while typing. Choices show their names in the editor's
 * language and store their keys; long lists of choices are searched rather than scrolled.
 */
const props = defineProps<{
  field: Field
  value: unknown
  path: string
  /** A line under the field saying what it does, where that depends on other settings or is not obvious. */
  help?: string
  readOnly: boolean
}>()

const changes = useEditorChanges()
const session = useEditorSession()
const {t, te} = useI18n()

const soundList = computed(() => props.field.kind === 'list' && props.field.item?.definition === 'SoundEntry')
const parent = computed(() => parentKey(props.path))
const group = computed(() => groupKey(props.path.split('/').slice(0, -1).join('/')))
const MOB_SPAWN_REASONS = /^\/mobs\/([^/]+)\/spawn\/reasons$/
const NIGHT_COMMANDS: ReadonlySet<string> = new Set(['beginCommands', 'endCommands'])
const COMMAND_LISTS: ReadonlySet<string> = new Set([...NIGHT_COMMANDS, 'deathCommands', 'respawnCommands'])
const commandSuggestions = computed(() => props.field.key.endsWith('Commands') ? session.payload.value?.choices?.commands ?? [] : undefined)
const special = computed(() => specialField(props.field, parent.value))
const label = computed(() => {
  if (soundList.value && te(`settings.soundLists.${props.field.key}`)) return t(`settings.soundLists.${props.field.key}`)
  const inGroup = `settings.fieldsIn.${group.value}.${props.field.key}`
  return te(inGroup) ? t(inGroup) : fieldLabel(props.field.key, t, te)
})
const example = computed(() => te(`settings.examples.${props.field.key}`) ? t(`settings.examples.${props.field.key}`, {player: '{player}', world: '{world}'}) : undefined)
const placeholders = computed(() => {
  const each = NIGHT_COMMANDS.has(props.field.key)
  return COMMAND_LISTS.has(props.field.key)
    ? [{token: '{player}', meaning: t(each ? 'editor.placeholders.playerEach' : 'editor.placeholders.player')}, {token: '{world}', meaning: t('editor.placeholders.world')}]
    : undefined
})
const inlineHelp = computed(() => ['boolean', 'integer', 'number', 'text', 'choice'].includes(props.field.kind) || special.value === 'dropWeights' || special.value === 'lootTables' || special.value === 'experience' || special.value === 'money' || special.value === 'moonPhases' || special.value === 'phaseCycle' || special.value === 'curve' || special.value === 'worldMobs' || special.value === 'mobName')
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
const soundsValue = computed(() => listValue.value as SoundEntry[] | undefined)
const textsValue = computed(() => Array.isArray(current.value) ? current.value as string[] : undefined)
const objectValue = computed(() => current.value !== null && typeof current.value === 'object' && !Array.isArray(current.value) ? current.value as Record<string, unknown> : undefined)
const itemField = computed(() => props.field.kind === 'text' && isItemField(props.field.key))
const assets = useItemAssets()
const itemChoices = assets.choices(true)
const choiceValue = computed(() => typeof current.value === 'string' ? current.value : null)
const choiceLabels = useChoiceLabels()
const options = computed(() => {
  const own = fieldOptions(props.field, session.payload.value?.choices?.mobTypes ?? [], current.value)
  const mob = MOB_SPAWN_REASONS.exec(props.path)?.[1]
  if (props.field.enumName !== 'SpawnReason' || mob === undefined) return own
  const type = session.payload.value?.mobs?.[mob]?.attributes?.entityType
  return spawnReasonOptions(own, type ? session.payload.value?.choices?.spawnReasons?.[type] : undefined, current.value)
})
const optionLabels = computed(() => choiceLabels.labels(options.value, {enumName: props.field.enumName}))
const mythicTypes = computed(() => session.payload.value?.choices?.mythicTypes ?? [])
const mythicField = computed(() => props.field.key === 'mythicType' && mythicTypes.value.length > 0)
const mythicOptions = computed(() => choiceValue.value && !mythicTypes.value.includes(choiceValue.value)
  ? [choiceValue.value, ...mythicTypes.value]
  : mythicTypes.value)

const attributeOptions = computed(() => {
  const attributes = props.field.key === 'attribute' ? session.payload.value?.choices?.attributes ?? [] : []
  return choiceValue.value && attributes.length > 0 && !attributes.includes(choiceValue.value) ? [choiceValue.value, ...attributes] : attributes
})
const suggestions = computed(() => props.field.kind === 'texts' ? suggestionsFor(props.field.key, session.payload.value) : null)
const suggestionLabels = computed(() => props.field.key === 'biomes' ? Object.fromEntries((suggestions.value ?? []).map(key => [key, biomeName(key)])) : undefined)

function biomeName(key: string): string {
  const tag = biomeTagName(key)
  if (tag) return t(`editor.biomes.${tag.kind}`, {name: tag.name})
  return assets.entry('biome', key) ?? biomeWords(key)
}

watch(current, value => {
  draft.value = typeof value === 'number' || typeof value === 'string' ? value : undefined
}, {immediate: true})

function save(value: unknown): void {
  if (props.readOnly) return
  if (value === undefined) {
    if (props.value !== undefined && props.value !== null) void changes.apply([{op: 'remove', path: props.path}])
    return
  }
  if (JSON.stringify(value) === JSON.stringify(props.value)) return
  void changes.apply([{op: 'add', path: props.path, value}])
}
</script>

<template>
  <div class="py-3">
    <FieldSpecial v-if="special" :field="field" :help="help" :label="label" :path="path" :read-only="readOnly" :special="special" :value="current" @save="save"/>
    <ToggleSetting v-else-if="field.kind === 'boolean'" :disabled="readOnly" :hint="help" :label="label" :model-value="Boolean(current)" @update:model-value="save"/>
    <LabelledField v-else-if="field.kind === 'integer'" v-slot="{id}" :help="help" :label="label">
      <NumberInput :id="id" v-model="numberDraft" :disabled="readOnly" @focusout="save(draft)"/>
    </LabelledField>
    <LabelledField v-else-if="field.kind === 'number'" v-slot="{id}" :help="help" :label="label">
      <DecimalInput :id="id" v-model="numberDraft" :disabled="readOnly" @focusout="save(draft)"/>
    </LabelledField>
    <LabelledField v-else-if="itemField" :help="help" :label="label">
      <ItemPicker :choices="itemChoices" :label="label" :model-value="textDraft" :read-only="readOnly" clearable @update:model-value="save"/>
    </LabelledField>
    <LabelledField v-else-if="mythicField" :help="help" :label="label">
      <ChoicePicker :label="label" :model-value="choiceValue || undefined" :options="mythicOptions" :read-only="readOnly" clearable @update:model-value="save($event ?? '')"/>
    </LabelledField>
    <LabelledField v-else-if="attributeOptions.length > 0" :help="help" :label="label">
      <ChoicePicker :label="label" :labels="attributeLabels(attributeOptions)" :model-value="choiceValue ?? undefined" :options="attributeOptions" :read-only="readOnly" @update:model-value="save"/>
    </LabelledField>
    <LabelledField v-else-if="field.kind === 'text'" v-slot="{id}" :help="help" :label="label">
      <TextInput :id="id" v-model="textDraft" :disabled="readOnly" @focusout="save(draft)"/>
    </LabelledField>
    <LabelledField v-else-if="field.kind === 'choice'" :help="help" :label="label">
      <ChoicePicker :label="label" :labels="optionLabels" :model-value="choiceValue ?? undefined" :options="options" :read-only="readOnly" @update:model-value="save"/>
    </LabelledField>
    <FieldChoices v-else-if="field.kind === 'choices'" :label="label" :labels="optionLabels" :options="options" :read-only="readOnly" :value="textsValue" @save="save"/>
    <FieldSuggestedTexts v-else-if="suggestions" :label="label" :labels="suggestionLabels" :read-only="readOnly" :suggestions="suggestions" :value="textsValue" @save="save"/>
    <FieldTexts v-else-if="field.kind === 'texts'" :example="example" :label="label" :placeholders="placeholders" :suggestions="commandSuggestions" :read-only="readOnly" :value="textsValue" @save="save"/>
    <div v-else-if="field.kind === 'group'" class="flex flex-col gap-1">
      <SubHeader>{{ label }}</SubHeader>
      <SchemaForm :fields="field.children" :path="path" :read-only="readOnly" :value="objectValue ?? {}"/>
    </div>
    <FieldMap v-else-if="field.kind === 'map' && field.item" :item="field.item" :label="label" :path="path" :read-only="readOnly" :value="objectValue" @save="save"/>
    <FieldSounds v-else-if="soundList" :label="label" :read-only="readOnly" :value="soundsValue" @save="save"/>
    <FieldList v-else-if="field.kind === 'list' && field.item" :item="field.item" :label="label" :path="path" :read-only="readOnly" :value="listValue" @save="save"/>
    <FieldJson v-else :label="label" :read-only="readOnly" :value="current" @save="save"/>
    <MutedText v-if="help && !inlineHelp" size="sm" tag="p">{{ help }}</MutedText>
  </div>
</template>
