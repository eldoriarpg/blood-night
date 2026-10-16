/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import SuggestInput from '@/components/input/text/SuggestInput.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'

/**
 * An item's enchantments by key with their levels: rows changed, added from the server's enchantments, removed. A level
 * is kept from 1 to the highest the enchantment reaches in survival, unless the switch allows higher ones; it starts on
 * for an item that already has a level above that. A level is saved once its field is left.
 */
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
const drafts = ref<Record<string, number | undefined>>({})

const entries = computed(() => Object.entries(props.value).sort(([a], [b]) => a.localeCompare(b)))
const maxLevels = computed(() => session.payload.value?.choices?.enchantments ?? {})
const known = computed(() => Object.keys(maxLevels.value))
const overMax = (key: string, level: number) => maxLevels.value[key] !== undefined && level > maxLevels.value[key]!
const beyond = ref(Object.entries(props.value).some(([key, level]) => overMax(key, level)))

watch(() => props.value, value => {
  drafts.value = {...value}
}, {immediate: true, deep: true})
const offered = computed(() => known.value
  .filter(key => props.value[key] === undefined)
  .map(key => ({value: key, label: assets.enchantment(key)}))
  .sort((a, b) => a.label.localeCompare(b.label)))

function allowed(key: string, value: number | undefined): number {
  const level = Math.max(1, Math.round(value ?? 1))
  const max = maxLevels.value[key]
  return !beyond.value && max !== undefined ? Math.min(level, max) : level
}

function level(key: string): void {
  const value = allowed(key, drafts.value[key])
  drafts.value[key] = value
  if (value !== props.value[key]) emit('save', {...props.value, [key]: value})
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
      <span v-if="maxLevels[key] !== undefined" :class="overMax(key, current) ? 'text-problem' : 'text-ink-faint'" class="shrink-0 text-xs">
        {{ t('editor.items.maxLevel', {max: maxLevels[key]}) }}
      </span>
      <div class="w-24 shrink-0">
        <NumberInput
            v-model="drafts[key]"
            :aria-label="t('editor.items.level', {key})"
            :disabled="readOnly"
            @focusout="level(key)"
        />
      </div>
      <MutedIconButton v-if="!readOnly" :label="t('editor.form.removeKey', {key})" icon="xmark" @click="remove(key)"/>
    </div>
    <ToggleSetting v-if="!readOnly" v-model="beyond" :hint="t('editor.items.beyondHint')" :label="t('editor.items.beyond')"/>
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
