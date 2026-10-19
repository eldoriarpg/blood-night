/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import {addItem, newItem} from '~/util/session/itemEntries'
import ItemPicker from '~/views/editorview/form/ItemPicker.vue'
import NewIdDialog from '~/views/editorview/form/NewIdDialog.vue'
import type {EditorPayload} from '~/api/generated/payload'

/** Creates an item by id and material; name, lore and enchantments are set in its form afterwards. */
const open = defineModel<boolean>({required: true})

defineProps<{
  taken: readonly string[]
}>()

const emit = defineEmits<{
  created: [id: string]
}>()

const {t} = useI18n()
const material = ref<string | undefined>(undefined)
const materialChoices = useItemAssets().choices(false)

function operations(payload: EditorPayload, id: string) {
  return addItem(payload, id, newItem(material.value ?? ''))
}

function created(id: string): void {
  material.value = undefined
  emit('created', id)
}
</script>

<template>
  <NewIdDialog v-model="open" :operations="operations" :ready="material !== undefined" :taken="taken" :title="t('editor.items.new')" @created="created">
    <LabelledField :label="t('editor.items.material')">
      <ItemPicker v-model="material" :choices="materialChoices" :label="t('editor.items.material')"/>
    </LabelledField>
  </NewIdDialog>
</template>
