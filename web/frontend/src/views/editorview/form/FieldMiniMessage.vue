/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import MutedText from '@/components/typography/MutedText.vue'
import MiniMessageText from '@/components/display/MiniMessageText.vue'
import MiniMessageInput from '~/views/editorview/form/MiniMessageInput.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import {isTranslated, withFallbacks} from '~/util/minimessage/translations'

/**
 * A text with colours and styles in MiniMessage, e.g. `<red><bold><name>`: typed with tag completion and a quick
 * reference (see {@link MiniMessageInput}), with a preview under it of what players see, saved once it is left.
 * Placeholders the text may use are filled in with a sample. A text players see in their own language, e.g. a default
 * mob's name, is shown as its English name with a note, until it is replaced by one written here.
 */
const props = defineProps<{
  label: string
  value: string | undefined
  help?: string
  readOnly: boolean
  /** Placeholders and the sample shown for them, e.g. `{name: 'Blood Night'}`. */
  samples?: Readonly<Record<string, string>>
}>()

const emit = defineEmits<{
  save: [value: string]
}>()

const {t} = useI18n()
const draft = ref('')
const writing = ref(false)
const translated = computed(() => !writing.value && isTranslated(draft.value))

watch(() => props.value, value => {
  draft.value = value ?? ''
  writing.value = false
}, {immediate: true})

function writeOwn(): void {
  draft.value = withFallbacks(draft.value)
  writing.value = true
}

function save(): void {
  if (draft.value !== (props.value ?? '')) emit('save', draft.value)
}
</script>

<template>
  <LabelledField v-slot="{id}" :help="help" :label="label">
    <div v-if="translated" class="flex flex-wrap items-center gap-3 rounded-control bg-ground px-3 py-2">
      <MiniMessageText :samples="samples" :text="draft" base="var(--color-ink)"/>
      <MutedText class="flex-1" size="xs">{{ t('editor.miniMessage.translated') }}</MutedText>
      <SecondaryButton v-if="!readOnly" compact icon="pen" @click="writeOwn">{{ t('editor.miniMessage.writeOwn') }}</SecondaryButton>
    </div>
    <template v-else>
      <MiniMessageInput :id="id" v-model="draft" :disabled="readOnly" :samples="samples" @focusout="save"/>
      <div class="flex items-baseline gap-2 rounded-control bg-ground px-3 py-2">
        <MutedText size="xs">{{ t('editor.miniMessage.preview') }}</MutedText>
        <MiniMessageText :samples="samples" :text="draft"/>
      </div>
    </template>
  </LabelledField>
</template>
