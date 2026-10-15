/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import MutedText from '@/components/typography/MutedText.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import SuggestInput from '@/components/input/text/SuggestInput.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MiniMessageText from '@/components/display/MiniMessageText.vue'
import MiniMessageInput from '~/views/editorview/form/MiniMessageInput.vue'
import type {MobName} from '~/api/generated/payload'
import {NAME_LANGUAGES, mobLabel} from '~/util/mobs/mobName'

/**
 * A mob's name: a text per language, each saved once it is left and removed with its button, a language added from
 * the ones BloodNight ships or by its code, e.g. {@code pt_br}, and one style for all of them with a preview of the
 * name in the editor's language. Players see the text of their own language, else the server's fallback language's.
 */
const props = defineProps<{
  label: string
  value: Readonly<Record<string, unknown>> | undefined
  help?: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: MobName]
}>()

const {t, locale} = useI18n()
const style = ref('')
const texts = ref<Record<string, string>>({})
const adding = ref('')

watch(() => props.value, value => {
  style.value = typeof value?.style === 'string' ? value.style : ''
  const given = value?.texts
  texts.value = given && typeof given === 'object' ? Object.fromEntries(Object.entries(given).filter(([, text]) => typeof text === 'string')) as Record<string, string> : {}
}, {immediate: true})

const languages = computed(() => Object.keys(texts.value).sort((a, b) => order(a) - order(b) || a.localeCompare(b)))
const offered = computed(() => NAME_LANGUAGES
  .filter(code => !(code in texts.value))
  .map(code => ({value: code, label: languageName(code)})))
const preview = computed(() => style.value + mobLabel({style: style.value, texts: texts.value}, locale.value))

function order(code: string): number {
  const index = NAME_LANGUAGES.indexOf(code)
  return index === -1 ? NAME_LANGUAGES.length : index
}

function languageName(code: string): string {
  try {
    return new Intl.DisplayNames([locale.value], {type: 'language'}).of(code.replace('_', '-')) ?? code
  } catch {
    return code
  }
}

function save(): void {
  const kept = Object.fromEntries(Object.entries(texts.value).map(([code, text]) => [code, text.trim()]).filter(([, text]) => text !== ''))
  emit('save', {style: style.value.trim(), texts: kept})
}

function add(): void {
  const code = adding.value.trim().toLowerCase().replace('-', '_')
  adding.value = ''
  if (!/^[a-z]{2,3}(_[a-z0-9]{2,4})?$/.test(code) || code in texts.value) return
  texts.value = {...texts.value, [code]: ''}
}

function remove(code: string): void {
  texts.value = Object.fromEntries(Object.entries(texts.value).filter(([language]) => language !== code))
  save()
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div v-for="code in languages" :key="code" class="flex items-center gap-2">
      <span class="w-32 shrink-0 truncate text-sm">
        {{ languageName(code) }} <span class="font-mono text-xs text-ink-faint">{{ code }}</span>
      </span>
      <TextInput
          v-model="texts[code]"
          :aria-label="t('editor.mobName.text', {language: languageName(code)})"
          :disabled="readOnly"
          @focusout="save"
          @keydown.enter.prevent="save"
      />
      <MutedIconButton v-if="!readOnly" :label="t('editor.mobName.remove', {language: languageName(code)})" hover="error" icon="xmark" @click="remove(code)"/>
    </div>
    <MutedText v-if="languages.length === 0" size="sm" tag="p">{{ t('editor.mobName.none') }}</MutedText>
    <div v-if="!readOnly" class="flex items-center gap-1">
      <SuggestInput
          v-model="adding"
          :aria-label="t('editor.mobName.add')"
          :placeholder="t('editor.mobName.addPlaceholder')"
          :suggestions="offered"
          @enter="add"
          @pick="add"
      />
      <SecondaryButton :disabled="adding.trim() === ''" compact icon="plus" @click="add">{{ t('editor.mobName.addButton') }}</SecondaryButton>
    </div>
    <div class="flex items-center gap-2">
      <span class="w-32 shrink-0 text-sm">{{ t('editor.mobName.style') }}</span>
      <MiniMessageInput v-model="style" :aria-label="t('editor.mobName.style')" :disabled="readOnly" placeholder="<dark_red>" @focusout="save"/>
    </div>
    <div v-if="languages.length > 0" class="flex items-baseline gap-2 rounded-control bg-ground px-3 py-2">
      <MutedText size="xs">{{ t('editor.miniMessage.preview') }}</MutedText>
      <MiniMessageText :text="preview" base="var(--color-ink)"/>
    </div>
    <MutedText v-if="help" size="sm" tag="p">{{ help }}</MutedText>
  </fieldset>
</template>
