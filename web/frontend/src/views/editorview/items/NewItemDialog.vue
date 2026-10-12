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
import {idProblem} from '~/util/session/newEntries'
import {addItem, newItem} from '~/util/session/itemEntries'
import ItemPicker from '~/views/editorview/form/ItemPicker.vue'

/** Creates an item by id and material; name, lore and enchantments are set in its form afterwards. */
const open = defineModel<boolean>({required: true})

const props = defineProps<{
  taken: readonly string[]
}>()

const emit = defineEmits<{
  created: [id: string]
}>()

const session = useEditorSession()
const changes = useEditorChanges()
const {t} = useI18n()
const id = ref('')
const material = ref<string | undefined>(undefined)

const materialChoices = useItemAssets().choices(false)
const problem = computed(() => id.value === '' ? null : idProblem(id.value, props.taken))

async function create(): Promise<void> {
  const payload = session.payload.value
  if (!payload || id.value === '' || problem.value || !material.value) return
  if (await changes.apply(addItem(payload, id.value, newItem(material.value)))) {
    emit('created', id.value)
    open.value = false
    id.value = ''
    material.value = undefined
  }
}
</script>

<template>
  <Modal v-model="open" size="sm">
    <form class="space-y-4" @submit.prevent="create">
      <SubHeader>{{ t('editor.items.new') }}</SubHeader>
      <LabelledField v-slot="{id: fieldId}" :label="t('editor.create.id')">
        <TextInput :id="fieldId" v-model="id" class="font-mono"/>
        <FieldHint :class="problem ? 'text-problem' : ''">{{ t(problem ?? 'editor.create.idHint') }}</FieldHint>
      </LabelledField>
      <LabelledField :label="t('editor.items.material')">
        <ItemPicker v-model="material" :choices="materialChoices" :label="t('editor.items.material')"/>
      </LabelledField>
      <ButtonRow align="end" pair>
        <SecondaryButton data-cancel @click="open = false">{{ t('editor.sheet.cancel') }}</SecondaryButton>
        <PrimaryButton :disabled="id === '' || !material || problem !== null" type="submit">{{ t('editor.create.submit') }}</PrimaryButton>
      </ButtonRow>
    </form>
  </Modal>
</template>
