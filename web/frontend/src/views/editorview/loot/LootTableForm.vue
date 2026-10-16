/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import ErrorButton from '@/components/button/ErrorButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import BareButton from '@/components/button/BareButton.vue'
import InfoBadge from '@/components/badge/InfoBadge.vue'
import Alert from '@/components/feedback/Alert.vue'
import Modal from '@/components/feedback/Modal.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import MutedText from '@/components/typography/MutedText.vue'
import PageHeader from '@/components/typography/PageHeader.vue'
import SectionHeader from '@/components/typography/SectionHeader.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import ItemPicker from '~/views/editorview/form/ItemPicker.vue'
import LootEntryRow from '~/views/editorview/loot/LootEntryRow.vue'
import LootTablePreview from '~/views/editorview/loot/LootTablePreview.vue'
import type {LootEntry} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import {section} from '~/util/session/sections'
import {mobLabel} from '~/util/mobs/mobName'
import {worldLabel} from '~/util/settings/worldNames'
import {entryShares, newEntry, removeTable, usesOf, type TableEntry, type TableUse} from '~/util/loot/lootTables'

/**
 * One loot table: its name, its entries, a preview of each entry's share and usual amount with a button rolling one
 * pick, and where the table is assigned, each a link there. Deleting a table in use also takes it out of those lists.
 */
const props = defineProps<{
  entry: TableEntry
}>()

const emit = defineEmits<{
  deleted: []
}>()

const session = useEditorSession()
const locks = useEditorLocks()
const changes = useEditorChanges()
const assets = useItemAssets()
const {openMob, openWorld, openServer} = useEditorNavigation()
const {t, locale} = useI18n()
const name = ref('')
const confirming = ref(false)

const choices = assets.choices(true)
const heldBy = computed(() => locks.heldByOther(section.lootTable(props.entry.id)))
const denied = computed(() => !locks.mayChange(section.lootTable(props.entry.id)))
const readOnly = computed(() => heldBy.value !== null || denied.value)
const shares = computed(() => entryShares(props.entry.table))
const uses = computed(() => usesOf(session.payload.value, props.entry.id))

onMounted(() => {
  void assets.load()
})

watch(() => props.entry.table.name, value => {
  name.value = value
}, {immediate: true})

function path(...keys: (string | number)[]): string {
  return pointer('lootTables', props.entry.id, ...keys.map(String))
}

function saveName(): void {
  const trimmed = name.value.trim()
  if (readOnly.value || trimmed === '' || trimmed === props.entry.table.name) return
  void changes.apply([{op: 'replace', path: path('name'), value: trimmed}])
}

function add(item: string | undefined): void {
  if (!item || readOnly.value) return
  void changes.apply([{op: 'add', path: path('entries', '-'), value: newEntry(item)}])
}

function save(index: number, entry: LootEntry): void {
  if (!readOnly.value) void changes.apply([{op: 'replace', path: path('entries', index), value: entry}])
}

function remove(index: number): void {
  if (!readOnly.value) void changes.apply([{op: 'remove', path: path('entries', index)}])
}

function useLabel(use: TableUse): string {
  const where = use.kind === 'mob'
    ? t('editor.loot.usedBy.mob', {name: mobName(use.id)})
    : use.kind === 'world'
      ? t('editor.loot.usedBy.world', {name: worldName(use.id)})
      : t('editor.loot.usedBy.server')
  return use.ordinary ? t('editor.loot.usedBy.ordinary', {where}) : where
}

function worldName(uid: string): string {
  const world = session.payload.value?.settings?.worlds?.[uid]
  return world ? worldLabel(world) : uid
}

function mobName(id: string): string {
  return mobLabel(session.payload.value?.mobs?.[id]?.attributes?.name, locale.value) || id
}

function open(use: TableUse): void {
  if (use.kind === 'mob') openMob(use.id)
  else if (use.kind === 'world') openWorld(use.id, 'loot')
  else openServer('loot')
}

function deleteTable(): void {
  const payload = session.payload.value
  confirming.value = false
  if (!payload) return
  void changes.apply(removeTable(payload, props.entry.id)).then(applied => {
    if (applied) emit('deleted')
  })
}
</script>

<template>
  <main class="overflow-y-auto px-6 py-5">
    <div class="flex flex-wrap items-center gap-3">
      <PageHeader class="flex-1">{{ entry.table.name || entry.id }}</PageHeader>
      <InfoBadge class="font-mono">{{ entry.id }}</InfoBadge>
    </div>
    <Alert v-if="heldBy" class="mt-3" variant="held">{{ t('editor.loot.heldBy', {name: heldBy}) }}</Alert>
    <Alert v-else-if="denied" class="mt-3" variant="held">{{ t('editor.permission.readOnly') }}</Alert>
    <MutedText class="mt-3 max-w-2xl" size="sm" tag="p">{{ t('editor.loot.intro') }}</MutedText>
    <div class="flex max-w-3xl flex-col gap-5 pt-4">
      <LabelledField v-slot="{id}" :label="t('editor.loot.name')">
        <TextInput :id="id" v-model="name" :disabled="readOnly" @focusout="saveName"/>
      </LabelledField>

      <section :aria-label="t('editor.loot.entries')" class="flex flex-col gap-3">
        <SectionHeader>{{ t('editor.loot.entries') }}</SectionHeader>
        <MutedText v-if="entry.table.entries.length === 0" size="sm" tag="p">{{ t('editor.loot.noEntries') }}</MutedText>
        <ol class="flex flex-col gap-3">
          <LootEntryRow
              v-for="(candidate, index) in entry.table.entries"
              :key="index"
              :choices="choices"
              :entry="candidate"
              :index="index"
              :read-only="readOnly"
              :share="shares[index] ?? 0"
              @remove="remove(index)"
              @save="value => save(index, value)"
          />
        </ol>
        <LabelledField v-if="!readOnly" :label="t('editor.loot.addEntry')">
          <ItemPicker :choices="choices" :label="t('editor.loot.addEntry')" :model-value="undefined" @update:model-value="add"/>
        </LabelledField>
      </section>

      <LootTablePreview v-if="entry.table.entries.length > 0" :choices="choices" :table="entry.table"/>

      <section :aria-label="t('editor.loot.usedBy.title')" class="flex flex-col gap-2">
        <SectionHeader>{{ t('editor.loot.usedBy.title') }}</SectionHeader>
        <MutedText v-if="uses.length === 0" size="sm" tag="p">{{ t('editor.loot.usedBy.none') }}</MutedText>
        <ul class="flex flex-col gap-1">
          <li v-for="use in uses" :key="use.path">
            <BareButton class="text-sm text-accent hover:underline" @click="open(use)">{{ useLabel(use) }}</BareButton>
          </li>
        </ul>
      </section>

      <div v-if="!readOnly"><ErrorButton compact icon="trash" @click="confirming = true">{{ t('editor.loot.delete') }}</ErrorButton></div>
    </div>
    <Modal v-model="confirming" size="sm">
      <div class="space-y-4">
        <SubHeader>{{ t('editor.loot.confirm', {name: entry.table.name || entry.id}) }}</SubHeader>
        <MutedText size="sm" tag="p">{{ uses.length > 0 ? t('editor.loot.confirmUsed', {count: uses.length}) : t('editor.sheet.confirmHint') }}</MutedText>
        <ButtonRow align="end" pair>
          <SecondaryButton data-cancel @click="confirming = false">{{ t('editor.sheet.cancel') }}</SecondaryButton>
          <PrimaryButton @click="deleteTable">{{ t('editor.loot.delete') }}</PrimaryButton>
        </ButtonRow>
      </div>
    </Modal>
  </main>
</template>
