/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import SuggestInput from '@/components/input/text/SuggestInput.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import BareButton from '@/components/button/BareButton.vue'
import MiniMessageInput from '~/views/editorview/form/MiniMessageInput.vue'

/**
 * A list of texts, e.g. commands: every entry a row of its own, changed in place and saved when it is left, and
 * removed with its button. The field below adds its text with the button or Enter, suggesting while typing where
 * there are suggestions, e.g. the server's commands. An entry left empty is dropped, and texts are saved without the
 * spaces around them. Texts in MiniMessage, e.g. lore, are typed with tag completion and a quick reference, and what
 * is typed is reported with `edit` for a live preview. Placeholders the texts may hold are listed under the field; picking one adds it to the new text.
 */
const props = defineProps<{
  label: string
  value: readonly string[] | undefined
  readOnly: boolean
  /** What a new entry could look like, shown in the empty field. */
  example?: string
  /** Texts offered while typing, e.g. the server's commands. */
  suggestions?: readonly string[]
  /** Placeholders the texts may hold, e.g. `{player}`, with what each stands for. */
  placeholders?: readonly {token: string, meaning: string}[]
  /** Whether the texts are MiniMessage. */
  miniMessage?: boolean
}>()

const emit = defineEmits<{
  save: [value: string[]]
  /** The texts as they are being typed, the new one last. */
  edit: [value: string[]]
}>()

const {t} = useI18n()
const offered = computed(() => (props.suggestions ?? []).map(value => ({value})))
const drafts = ref<string[]>([])
const draft = ref('')

watch(() => props.value, value => {
  drafts.value = [...value ?? []]
}, {immediate: true})

watch([drafts, draft], () => emit('edit', [...drafts.value, draft.value].filter(entry => entry.trim() !== '')), {deep: true})

function save(entries: readonly string[]): void {
  const cleaned = entries.map(entry => entry.trim()).filter(entry => entry !== '')
  const saved = props.value ?? []
  if (cleaned.length === saved.length && cleaned.every((entry, index) => entry === saved[index])) {
    drafts.value = [...saved]
    return
  }
  emit('save', cleaned)
}

function add(): void {
  if (draft.value.trim() === '') return
  save([...drafts.value, draft.value])
  draft.value = ''
}

function insert(token: string): void {
  draft.value = draft.value === '' || draft.value.endsWith(' ') ? `${draft.value}${token}` : `${draft.value} ${token}`
}

function remove(index: number): void {
  save(drafts.value.filter((_, at) => at !== index))
}
</script>

<template>
  <LabelledField v-slot="{id}" :label="label">
    <ul v-if="drafts.length > 0" class="flex flex-col gap-1">
      <li v-for="(_, index) in drafts" :key="index" class="flex items-center gap-1">
        <component
            :is="miniMessage ? MiniMessageInput : TextInput"
            v-model="drafts[index]"
            :aria-label="t('editor.form.entry', {label, number: index + 1})"
            :disabled="readOnly"
            class="font-mono"
            @focusout="save(drafts)"
            @keydown.enter.prevent="save(drafts)"
        />
        <MutedIconButton v-if="!readOnly" :label="t('editor.form.removeEntry', {number: index + 1})" hover="error" icon="xmark" @click="remove(index)"/>
      </li>
    </ul>
    <div v-if="!readOnly" class="flex items-center gap-1">
      <MiniMessageInput
          v-if="miniMessage"
          :id="id"
          v-model="draft"
          :placeholder="example ? t('editor.form.addExample', {example}) : t('editor.form.addText')"
          @keydown.enter.prevent="add"
      />
      <SuggestInput
          v-else
          :id="id"
          v-model="draft"
          :placeholder="example ? t('editor.form.addExample', {example}) : t('editor.form.addText')"
          :suggestions="offered"
          class="font-mono"
          @enter="add"
      />
      <SecondaryButton :disabled="draft.trim() === ''" compact icon="plus" @click="add">{{ t('editor.form.add') }}</SecondaryButton>
    </div>
    <div v-if="!readOnly && placeholders?.length" class="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-ink-muted">
      <span>{{ t('editor.form.placeholders') }}</span>
      <span v-for="entry in placeholders" :key="entry.token" class="flex items-center gap-1">
        <BareButton
            :aria-label="t('editor.form.insertPlaceholder', {token: entry.token})"
            class="rounded-control border border-line px-1.5 py-0.5 font-mono text-ink hover:bg-raised"
            @click="insert(entry.token)"
        >{{ entry.token }}</BareButton>
        {{ entry.meaning }}
      </span>
    </div>
  </LabelledField>
</template>
