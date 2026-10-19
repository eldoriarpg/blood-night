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
import FieldHint from '@/components/typography/FieldHint.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import type {EditorPayload} from '~/api/generated/payload'
import type {PatchOperation} from '~/util/patch/jsonPatch'
import {idProblem} from '~/util/session/newEntries'

/**
 * A dialog that creates something by a new id: the id field with what is wrong with it, the fields the slot adds, and
 * a button that sends the change and closes the dialog. The id is cleared after a creation; the parent clears its own
 * fields on `created`.
 */
const open = defineModel<boolean>({required: true})

const props = defineProps<{
  title: string
  /** Ids already in use. */
  taken: readonly string[]
  /** Whether the slot's fields are filled in. */
  ready: boolean
  /** The change that creates it with the id. */
  operations: (payload: EditorPayload, id: string) => PatchOperation[]
}>()

const emit = defineEmits<{
  created: [id: string]
}>()

const session = useEditorSession()
const changes = useEditorChanges()
const {t} = useI18n()
const id = ref('')
const problem = computed(() => id.value === '' ? null : idProblem(id.value, props.taken))

async function create(): Promise<void> {
  const payload = session.payload.value
  if (!payload || id.value === '' || problem.value || !props.ready) return
  const created = id.value
  if (await changes.apply(props.operations(payload, created))) {
    open.value = false
    id.value = ''
    emit('created', created)
  }
}
</script>

<template>
  <Modal v-model="open" size="sm">
    <form class="space-y-4" @submit.prevent="create">
      <SubHeader>{{ title }}</SubHeader>
      <LabelledField v-slot="{id: fieldId}" :label="t('editor.create.id')">
        <TextInput :id="fieldId" v-model="id" class="font-mono"/>
        <FieldHint :class="problem ? 'text-problem' : ''">{{ t(problem ?? 'editor.create.idHint') }}</FieldHint>
      </LabelledField>
      <slot/>
      <ButtonRow align="end" pair>
        <SecondaryButton data-cancel @click="open = false">{{ t('editor.sheet.cancel') }}</SecondaryButton>
        <PrimaryButton :disabled="id === '' || !ready || problem !== null" type="submit">{{ t('editor.create.submit') }}</PrimaryButton>
      </ButtonRow>
    </form>
  </Modal>
</template>
