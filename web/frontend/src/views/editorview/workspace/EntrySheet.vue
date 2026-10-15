/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import BareButton from '@/components/button/BareButton.vue'
import AppIcon from '@/components/display/AppIcon.vue'
import ErrorButton from '@/components/button/ErrorButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import Modal from '@/components/feedback/Modal.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SchemaForm from '~/views/editorview/form/SchemaForm.vue'
import schema from '~/api/generated/payload.schema.json'
import {fieldsOf, type SchemaDocument} from '~/util/settings/schemaFields'
import type {WorkspaceEntry, WorkspaceKind} from '~/views/editorview/workspace/workspaceEntries'

/**
 * The settings of the open mob (attributes, movement, spawn rules, equipment, extension, drops) or macro (name, description,
 * category), and deleting it. Each part of a mob folds under its header and starts folded, so the sheet opens short;
 * which parts the reader opened is remembered by the browser, for every mob alike. A mob that is a MythicMobs mob
 * instead has no equipment or extension, since the plugin gives it neither. Inline, it fills a column of its own and
 * has nothing to close.
 */
const props = defineProps<{
  kind: WorkspaceKind
  entry: WorkspaceEntry
  readOnly: boolean
  inline?: boolean
}>()

const emit = defineEmits<{
  close: []
  deleted: []
}>()

const session = useEditorSession()
const changes = useEditorChanges()
const {t} = useI18n()
const confirming = ref(false)
const document = schema as unknown as SchemaDocument
const OPENED_STORAGE_KEY = 'bloodnight.mobSheet.opened'
const opened = ref<ReadonlySet<string>>(new Set())

onMounted(() => {
  opened.value = storedOpened()
})

const value = computed(() => {
  const payload = session.payload.value
  if (props.kind === 'mobs') return payload?.mobs?.[props.entry.id] as Record<string, unknown> | undefined
  return payload?.macros?.find(macro => macro.id === props.entry.id) as Record<string, unknown> | undefined
})

const mythic = computed(() => {
  const type = (value.value?.attributes as {mythicType?: unknown} | undefined)?.mythicType
  return typeof type === 'string' && type.trim() !== ''
})
const parts = computed(() => props.kind === 'mobs'
  ? ['attributes', 'movement', 'spawn', 'equipment', 'extension', 'mobDrops']
      .filter(key => !mythic.value || (key !== 'equipment' && key !== 'extension'))
      .map(key => ({key, fields: fieldsOf(document, definitionOf(key))}))
  : [{key: 'macro', fields: fieldsOf(document, 'Macro').filter(field => field.key !== 'id' && field.key !== 'body')}])

function definitionOf(key: string): string {
  return {attributes: 'Attributes', spawn: 'SpawnRules', equipment: 'Equipment', extension: 'Extension', mobDrops: 'MobDrops', movement: 'Movement'}[key] ?? key
}

function partValue(key: string): Record<string, unknown> | undefined {
  return key === 'macro' ? value.value : value.value?.[key] as Record<string, unknown> | undefined
}

function partPath(key: string): string {
  return key === 'macro' ? props.entry.path : `${props.entry.path}/${key}`
}

function storedOpened(): Set<string> {
  try {
    const stored: unknown = JSON.parse(localStorage.getItem(OPENED_STORAGE_KEY) ?? '[]')
    return new Set(Array.isArray(stored) ? stored.filter((part): part is string => typeof part === 'string') : [])
  } catch {
    return new Set()
  }
}

function storeOpened(parts: ReadonlySet<string>): boolean {
  try {
    localStorage.setItem(OPENED_STORAGE_KEY, JSON.stringify([...parts]))
    return true
  } catch {
    return false
  }
}

function toggleFold(key: string): void {
  const next = new Set(opened.value)
  if (!next.delete(key)) next.add(key)
  opened.value = next
  storeOpened(next)
}

function create(key: string): void {
  void changes.apply([{op: 'add', path: partPath(key), value: {}}])
}

function remove(): void {
  confirming.value = false
  void changes.apply([{op: 'remove', path: props.entry.path}]).then(applied => {
    if (applied) emit('deleted')
  })
}
</script>

<template>
  <aside :aria-label="t(`editor.sheet.title.${kind}`)" :class="inline ? 'h-full w-full' : 'max-h-full w-[26rem] rounded-panel border border-line'" class="flex flex-col gap-3 overflow-y-auto bg-surface p-4">
    <div class="flex items-center justify-between">
      <SectionLabel>{{ t(`editor.sheet.title.${kind}`) }}</SectionLabel>
      <MutedIconButton v-if="!inline" :label="t('editor.sheet.close')" icon="xmark" @click="emit('close')"/>
    </div>
    <SubHeader class="font-mono">{{ entry.id }}</SubHeader>
    <section v-for="part in parts" :key="part.key" class="flex flex-col">
      <BareButton
          v-if="part.key !== 'macro'"
          :aria-expanded="opened.has(part.key)"
          class="flex items-center gap-2 rounded-control py-1 text-left hover:bg-raised"
          @click="toggleFold(part.key)"
      >
        <AppIcon :class="opened.has(part.key) ? '' : '-rotate-90'" class="text-xs text-ink-faint transition-transform" icon="chevron-down"/>
        <SectionLabel>{{ t(`editor.sheet.parts.${part.key}`) }}</SectionLabel>
      </BareButton>
      <template v-if="part.key === 'macro' || opened.has(part.key)">
        <SchemaForm v-if="part.key === 'macro' || partValue(part.key)" :fields="part.fields" :path="partPath(part.key)" :read-only="readOnly" :value="partValue(part.key)"/>
        <div v-else-if="!readOnly" class="py-2"><SecondaryButton compact @click="create(part.key)">{{ t('editor.sheet.set') }}</SecondaryButton></div>
      </template>
    </section>
    <div v-if="!readOnly" class="pt-2"><ErrorButton compact icon="trash" @click="confirming = true">{{ t(`editor.sheet.delete.${kind}`) }}</ErrorButton></div>
    <Modal v-model="confirming" size="sm">
      <div class="space-y-4">
        <SubHeader>{{ t(`editor.sheet.confirm.${kind}`, {id: entry.id}) }}</SubHeader>
        <MutedText size="sm" tag="p">{{ t('editor.sheet.confirmHint') }}</MutedText>
        <ButtonRow align="end" pair>
          <SecondaryButton data-cancel @click="confirming = false">{{ t('editor.sheet.cancel') }}</SecondaryButton>
          <PrimaryButton @click="remove">{{ t(`editor.sheet.delete.${kind}`) }}</PrimaryButton>
        </ButtonRow>
      </div>
    </Modal>
  </aside>
</template>
