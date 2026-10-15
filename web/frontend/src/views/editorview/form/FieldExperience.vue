/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import DecimalInput from '@/components/input/number/DecimalInput.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import type {Experience, ValueModifier} from '~/api/generated/payload'

/**
 * How the experience of a kill changes: unchanged, multiplied, or a fixed amount, with the number for the last two.
 * Where a broader level can decide instead, a mob's or a world's, "as the broader level" leaves it unset.
 */
const props = defineProps<{
  label: string
  value: Experience | null | undefined
  help?: string
  readOnly: boolean
  /** Whose setting applies while this one is unset, e.g. "As the world or the server"; none where it is always set. */
  inherited?: string
}>()

const emit = defineEmits<{
  save: [value: Experience | undefined]
}>()

const {t} = useI18n()
const INHERIT = 'INHERIT'
const MODIFIERS: readonly ValueModifier[] = ['DEFAULT', 'MULTIPLY', 'VALUE']
const draft = ref<number | undefined>(undefined)

const modifier = computed(() => props.value?.modifier ?? (props.inherited === undefined ? 'DEFAULT' : INHERIT))
const options = computed(() => props.inherited === undefined ? [...MODIFIERS] : [INHERIT, ...MODIFIERS])
const labels = computed(() => ({
  [INHERIT]: props.inherited ?? '',
  DEFAULT: t('editor.experience.unchanged'),
  MULTIPLY: t('editor.experience.multiply'),
  VALUE: t('editor.experience.value'),
}))

watch(() => props.value?.value, value => {
  draft.value = value
}, {immediate: true})

function pick(choice: string | undefined): void {
  if (!choice || choice === modifier.value) return
  if (choice === INHERIT) emit('save', undefined)
  else emit('save', {modifier: choice as ValueModifier, value: choice === 'VALUE' ? Math.round(draft.value ?? 10) : (draft.value ?? 2)})
}

function saveValue(): void {
  if (props.value && draft.value !== undefined && draft.value !== props.value.value) emit('save', {...props.value, value: Math.max(0, draft.value)})
}
</script>

<template>
  <LabelledField :help="help" :label="label">
    <div class="flex flex-wrap items-center gap-3">
      <ChoicePicker :label="label" :labels="labels" :model-value="modifier" :options="options" :read-only="readOnly" class="min-w-48 flex-1" @update:model-value="pick"/>
      <div v-if="value && value.modifier !== 'DEFAULT'" class="w-28">
        <DecimalInput v-model="draft" :aria-label="t(value.modifier === 'VALUE' ? 'editor.experience.amount' : 'editor.experience.factor')" :disabled="readOnly" @focusout="saveValue"/>
      </div>
    </div>
  </LabelledField>
</template>
