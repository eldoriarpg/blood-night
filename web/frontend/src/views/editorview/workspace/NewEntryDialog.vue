/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import NewIdDialog from '~/views/editorview/form/NewIdDialog.vue'
import {EntityType, type EditorPayload} from '~/api/generated/payload'
import {addMacro, addMob, newMacro, newMob} from '~/util/session/newEntries'
import type {WorkspaceKind} from '~/views/editorview/workspace/workspaceEntries'

/** Creates a mob (id and the entity it takes over) or a macro (id and name). */
const open = defineModel<boolean>({required: true})

const props = defineProps<{
  kind: WorkspaceKind
  taken: readonly string[]
}>()

const emit = defineEmits<{
  created: [id: string]
}>()

const session = useEditorSession()
const {t} = useI18n()
const name = ref('')
const entityType = ref<string | undefined>('ZOMBIE')
const choices = useChoiceLabels()
const entityTypes = computed(() => {
  const mobs = session.payload.value?.choices?.mobTypes ?? []
  return mobs.length > 0 ? mobs : Object.values(EntityType)
})
const entityLabels = computed(() => choices.labels(entityTypes.value, {enumName: 'EntityType'}))

function operations(payload: EditorPayload, id: string) {
  return props.kind === 'mobs'
    ? addMob(payload, newMob(id, entityType.value as EntityType))
    : addMacro(payload, newMacro(id, name.value))
}

function created(id: string): void {
  name.value = ''
  emit('created', id)
}
</script>

<template>
  <NewIdDialog
      v-model="open"
      :operations="operations"
      :ready="kind !== 'mobs' || entityType !== undefined"
      :taken="taken"
      :title="t(`editor.create.title.${kind}`)"
      @created="created"
  >
    <LabelledField v-if="kind === 'mobs'" :label="t('editor.create.entityType')">
      <ChoicePicker v-model="entityType" :label="t('editor.create.entityType')" :labels="entityLabels" :options="entityTypes"/>
    </LabelledField>
    <LabelledField v-else v-slot="{id: fieldId}" :label="t('editor.create.name')">
      <TextInput :id="fieldId" v-model="name"/>
    </LabelledField>
  </NewIdDialog>
</template>
