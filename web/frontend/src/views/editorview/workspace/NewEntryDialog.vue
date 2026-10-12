/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import Modal from '@/components/feedback/Modal.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import FieldHint from '@/components/typography/FieldHint.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import {EntityType} from '~/api/generated/payload'
import {addMacro, addMob, idProblem, newMacro, newMob} from '~/util/session/newEntries'
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
const changes = useEditorChanges()
const {t} = useI18n()
const id = ref('')
const name = ref('')
const entityType = ref<string | undefined>('ZOMBIE')
const choices = useChoiceLabels()
const entityTypes = computed(() => {
  const mobs = session.payload.value?.choices?.mobTypes ?? []
  return mobs.length > 0 ? mobs : Object.values(EntityType)
})
const entityLabels = computed(() => choices.labels(entityTypes.value, {enumName: 'EntityType'}))

const problem = computed(() => id.value === '' ? null : idProblem(id.value, props.taken))

async function create(): Promise<void> {
  const payload = session.payload.value
  if (!payload || id.value === '' || problem.value || (props.kind === 'mobs' && !entityType.value)) return
  const operations = props.kind === 'mobs'
    ? addMob(payload, newMob(id.value, entityType.value as EntityType))
    : addMacro(payload, newMacro(id.value, name.value))
  if (await changes.apply(operations)) {
    emit('created', id.value)
    open.value = false
    id.value = ''
    name.value = ''
  }
}
</script>

<template>
  <Modal v-model="open" size="sm">
    <form class="space-y-4" @submit.prevent="create">
      <SubHeader>{{ t(`editor.create.title.${kind}`) }}</SubHeader>
      <LabelledField v-slot="{id: fieldId}" :label="t('editor.create.id')">
        <TextInput :id="fieldId" v-model="id" class="font-mono"/>
        <FieldHint :class="problem ? 'text-problem' : ''">{{ t(problem ?? 'editor.create.idHint') }}</FieldHint>
      </LabelledField>
      <LabelledField v-if="kind === 'mobs'" :label="t('editor.create.entityType')">
        <ChoicePicker v-model="entityType" :label="t('editor.create.entityType')" :labels="entityLabels" :options="entityTypes"/>
      </LabelledField>
      <LabelledField v-else v-slot="{id: fieldId}" :label="t('editor.create.name')">
        <TextInput :id="fieldId" v-model="name"/>
      </LabelledField>
      <ButtonRow align="end" pair>
        <SecondaryButton data-cancel @click="open = false">{{ t('editor.sheet.cancel') }}</SecondaryButton>
        <PrimaryButton :disabled="id === '' || problem !== null" type="submit">{{ t('editor.create.submit') }}</PrimaryButton>
      </ButtonRow>
    </form>
  </Modal>
</template>
