/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import NewLootTableDialog from '~/views/editorview/loot/NewLootTableDialog.vue'
import {tableEntries} from '~/util/loot/lootTables'

/**
 * The loot tables a mob, a world or the server assigns: one chip per table with its name, which opens the table, a
 * picker adding another table by name, and a shortcut making a new table and assigning it at once. A table id the
 * session does not know is shown as it is, so it can be taken out.
 */
const props = defineProps<{
  label: string
  value: readonly string[] | undefined
  help?: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: string[]]
}>()

const session = useEditorSession()
const {openLootTable} = useEditorNavigation()
const {t} = useI18n()
const creating = ref(false)

const tables = computed(() => tableEntries(session.payload.value))
const names = computed(() => Object.fromEntries(tables.value.map(entry => [entry.id, entry.table.name || entry.id])))
const assigned = computed(() => props.value ?? [])
const offered = computed(() => tables.value.map(entry => entry.id).filter(id => !assigned.value.includes(id)))

function add(id: string | undefined): void {
  if (id && !assigned.value.includes(id)) emit('save', [...assigned.value, id])
}

function remove(id: string): void {
  emit('save', assigned.value.filter(candidate => candidate !== id))
}
</script>

<template>
  <LabelledField :help="help" :label="label">
    <ul v-if="assigned.length > 0" class="flex flex-wrap gap-1">
      <li v-for="id in assigned" :key="id" class="flex items-center gap-1 rounded-control border border-line-soft pl-2 text-sm">
        <BareButton v-if="names[id]" :title="t('editor.lootPicker.open', {name: names[id]})" class="hover:underline" @click="openLootTable(id)">{{ names[id] }}</BareButton>
        <span v-else :title="t('editor.lootPicker.unknown')" class="font-mono text-problem">{{ id }}</span>
        <MutedIconButton v-if="!readOnly" :label="t('editor.form.remove', {entry: names[id] ?? id})" hover="error" icon="xmark" @click="remove(id)"/>
      </li>
    </ul>
    <div v-if="!readOnly" class="flex flex-wrap items-center gap-2">
      <ChoicePicker
          :label="t('editor.lootPicker.add')"
          :labels="names"
          :model-value="undefined"
          :options="offered"
          :placeholder="t('editor.lootPicker.add')"
          class="min-w-48 flex-1"
          @update:model-value="add"
      />
      <SecondaryButton compact icon="plus" @click="creating = true">{{ t('editor.loot.new') }}</SecondaryButton>
    </div>
    <NewLootTableDialog v-model="creating" @created="add"/>
  </LabelledField>
</template>
