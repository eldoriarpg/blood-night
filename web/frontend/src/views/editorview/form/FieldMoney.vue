/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import Alert from '@/components/feedback/Alert.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import DecimalInput from '@/components/input/number/DecimalInput.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import type {Money} from '~/api/generated/payload'

/**
 * The money a kill pays through Vault: nothing, or an amount rolled between a least and a most. Where a broader level
 * can decide instead, a mob's or a world's, "as the broader level" leaves it unset and "nothing" stores a zero amount;
 * elsewhere unset pays nothing. Warns when the server has no economy to pay through.
 */
const props = defineProps<{
  label: string
  value: Money | null | undefined
  help?: string
  readOnly: boolean
  /** Whose setting applies while this one is unset, e.g. "As the world or the server"; none where unset pays nothing. */
  inherited?: string
  /** Whether the server has an economy that takes payments. */
  economy: boolean
}>()

const emit = defineEmits<{
  save: [value: Money | undefined]
}>()

const {t} = useI18n()
const INHERIT = 'INHERIT'
const NOTHING = 'NOTHING'
const PAY = 'PAY'
const least = ref<number | undefined>(undefined)
const most = ref<number | undefined>(undefined)

const mode = computed(() => {
  if (!props.value) return props.inherited === undefined ? NOTHING : INHERIT
  return props.value.max > 0 ? PAY : NOTHING
})
const options = computed(() => props.inherited === undefined ? [NOTHING, PAY] : [INHERIT, NOTHING, PAY])
const labels = computed(() => ({
  [INHERIT]: props.inherited ?? '',
  [NOTHING]: t('editor.money.nothing'),
  [PAY]: t('editor.money.pay'),
}))

watch(() => props.value, value => {
  least.value = value?.min
  most.value = value?.max
}, {immediate: true})

function pick(choice: string | undefined): void {
  if (!choice || choice === mode.value) return
  if (choice === INHERIT) emit('save', undefined)
  else if (choice === NOTHING) emit('save', props.inherited === undefined ? undefined : {min: 0, max: 0})
  else emit('save', {min: 1, max: 5})
}

function saveAmounts(): void {
  if (!props.value || least.value === undefined || most.value === undefined) return
  const min = Math.max(0, least.value)
  const max = Math.max(min, most.value)
  if (min !== props.value.min || max !== props.value.max) emit('save', {min, max})
}
</script>

<template>
  <LabelledField :help="help" :label="label">
    <div class="flex flex-col gap-2">
      <div class="flex flex-wrap items-center gap-3">
        <ChoicePicker :label="label" :labels="labels" :model-value="mode" :options="options" :read-only="readOnly" class="min-w-48 flex-1" @update:model-value="pick"/>
        <template v-if="mode === PAY">
          <div class="w-28">
            <DecimalInput v-model="least" :aria-label="t('editor.money.min')" :disabled="readOnly" @focusout="saveAmounts"/>
          </div>
          <span class="text-ink-muted">{{ t('editor.money.to') }}</span>
          <div class="w-28">
            <DecimalInput v-model="most" :aria-label="t('editor.money.max')" :disabled="readOnly" @focusout="saveAmounts"/>
          </div>
        </template>
      </div>
      <Alert v-if="mode === PAY && !economy" data-testid="money-no-economy" variant="held">{{ t('editor.money.noEconomy') }}</Alert>
    </div>
  </LabelledField>
</template>
