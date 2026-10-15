/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import Toggle from '@/components/input/toggle/ToggleSetting.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'

/**
 * Game rules a blood night sets: a row per rule by its name in the editor's language, with a switch or a number by
 * the rule's kind; a rule is added by picking it and removed with its button. A switch saves at once, a number once
 * it is left. Values are stored as text, the way the plugin reads them.
 */
const props = defineProps<{
  label: string
  value: Record<string, unknown> | undefined
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: Record<string, string>]
}>()

const {t} = useI18n()
const session = useEditorSession()
const assets = useItemAssets()
const rules = computed(() => session.payload.value?.choices?.gameRules ?? {})
const entries = computed(() => Object.entries(props.value ?? {}).map(([key, value]) => [key, String(value)] as const))
const available = computed(() => Object.keys(rules.value).filter(key => props.value?.[key] === undefined))

onMounted(() => {
  void assets.load()
})

function name(key: string): string {
  const translation = rules.value[key]?.translationKey
  const named = translation ? assets.entry('game_rule', `minecraft:${translation.replace(/^gamerule\./, '')}`) : undefined
  return named ?? key.replace(/^minecraft:/, '').replaceAll('_', ' ')
}

const labels = computed(() => Object.fromEntries(available.value.map(key => [key, name(key)])))
const numbers = ref<Record<string, number | undefined>>({})

watch(entries, value => {
  numbers.value = Object.fromEntries(value.map(([key, entry]) => [key, Number(entry)]))
}, {immediate: true})

function save(next: Record<string, string>): void {
  emit('save', next)
}

function set(key: string, value: string): void {
  save({...Object.fromEntries(entries.value), [key]: value})
}

function add(key: string | undefined): void {
  if (!key) return
  set(key, rules.value[key]?.type === 'boolean' ? 'true' : '0')
}

function remove(key: string): void {
  save(Object.fromEntries(entries.value.filter(([candidate]) => candidate !== key)))
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div v-for="[key, entry] in entries" :key="key" class="flex items-center gap-3">
      <div class="min-w-0 flex-1">
        <Toggle v-if="rules[key]?.type === 'boolean'" :disabled="readOnly" :label="name(key)" :model-value="entry === 'true'" @update:model-value="set(key, String($event))"/>
        <div v-else class="flex items-center gap-3">
          <span class="min-w-0 flex-1 text-sm">{{ name(key) }}</span>
          <div class="w-28 shrink-0">
            <NumberInput v-model="numbers[key]" :aria-label="name(key)" :disabled="readOnly" @focusout="set(key, String(numbers[key] ?? 0))"/>
          </div>
        </div>
      </div>
      <MutedIconButton v-if="!readOnly" :label="t('editor.form.removeKey', {key: name(key)})" hover="error" icon="xmark" @click="remove(key)"/>
    </div>
    <ChoicePicker v-if="!readOnly" :label="t('editor.gameRules.add')" :labels="labels" :model-value="undefined" :options="available" :placeholder="t('editor.gameRules.add')" @update:model-value="add"/>
  </fieldset>
</template>
