/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import SuggestInput from '@/components/input/text/SuggestInput.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'

/** An item's enchantments by key with their levels: rows changed, added from the server's enchantments, removed. */
const props = defineProps<{
  value: Readonly<Record<string, number>>
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: Record<string, number>]
}>()

const session = useEditorSession()
const assets = useItemAssets()
const {t} = useI18n()
const newKey = ref('')

const entries = computed(() => Object.entries(props.value).sort(([a], [b]) => a.localeCompare(b)))
const known = computed(() => session.payload.value?.choices?.enchantments ?? [])
const offered = computed(() => known.value
  .filter(key => props.value[key] === undefined)
  .map(key => ({value: key, label: assets.enchantment(key)}))
  .sort((a, b) => a.label.localeCompare(b.label)))

function level(key: string, value: number | undefined): void {
  if (value === undefined || value < 1 || value === props.value[key]) return
  emit('save', {...props.value, [key]: Math.round(value)})
}

function add(): void {
  const key = newKey.value.trim()
  if (key === '' || props.value[key] !== undefined) return
  emit('save', {...props.value, [key.includes(':') ? key : `minecraft:${key}`]: 1})
  newKey.value = ''
}

function remove(key: string): void {
  emit('save', Object.fromEntries(entries.value.filter(([candidate]) => candidate !== key)))
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ t('editor.items.enchantments') }}</FieldLabel></legend>
    <div v-for="[key, current] in entries" :key="key" class="flex items-center gap-3 border-b border-line-soft py-1">
      <span class="min-w-0 flex-1 truncate text-sm">
        {{ assets.enchantment(key) }} <span class="font-mono text-xs text-ink-faint">{{ key }}</span>
      </span>
      <NumberInput
          :aria-label="t('editor.items.level', {key})"
          :disabled="readOnly"
          :model-value="current"
          class="w-24"
          @update:model-value="value => level(key, value)"
      />
      <MutedIconButton v-if="!readOnly" :label="t('editor.form.removeKey', {key})" icon="xmark" @click="remove(key)"/>
    </div>
    <div v-if="!readOnly" class="flex items-center gap-2">
      <SuggestInput
          v-model="newKey"
          :aria-label="t('editor.items.newEnchantment')"
          :placeholder="t('editor.items.newEnchantment')"
          :suggestions="offered"
          @enter="add"
          @pick="add"
      />
      <SecondaryButton compact @click="add">{{ t('editor.form.add') }}</SecondaryButton>
    </div>
  </fieldset>
</template>
