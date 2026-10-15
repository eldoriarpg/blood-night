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
import {addTable, newTable, tableId} from '~/util/loot/lootTables'

/** Creates a loot table by name; its id is made from the name, its entries are added in its form afterwards. */
const open = defineModel<boolean>({required: true})

const emit = defineEmits<{
  created: [id: string]
}>()

const session = useEditorSession()
const changes = useEditorChanges()
const {t} = useI18n()
const name = ref('')

const id = computed(() => tableId(name.value, Object.keys(session.payload.value?.lootTables ?? {})))

async function create(): Promise<void> {
  const payload = session.payload.value
  if (!payload || name.value.trim() === '') return
  const created = id.value
  if (await changes.apply(addTable(payload, created, newTable(name.value)))) {
    emit('created', created)
    open.value = false
    name.value = ''
  }
}
</script>

<template>
  <Modal v-model="open" size="sm">
    <form class="space-y-4" @submit.prevent="create">
      <SubHeader>{{ t('editor.loot.new') }}</SubHeader>
      <LabelledField v-slot="{id: fieldId}" :label="t('editor.loot.name')">
        <TextInput :id="fieldId" v-model="name" :placeholder="t('editor.loot.namePlaceholder')"/>
        <FieldHint>{{ t('editor.loot.idHint', {id: name.trim() === '' ? '…' : id}) }}</FieldHint>
      </LabelledField>
      <ButtonRow align="end" pair>
        <SecondaryButton data-cancel @click="open = false">{{ t('editor.sheet.cancel') }}</SecondaryButton>
        <PrimaryButton :disabled="name.trim() === ''" type="submit">{{ t('editor.create.submit') }}</PrimaryButton>
      </ButtonRow>
    </form>
  </Modal>
</template>
