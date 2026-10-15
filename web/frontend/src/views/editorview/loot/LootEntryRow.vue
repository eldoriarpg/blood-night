/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import ItemPicker from '~/views/editorview/form/ItemPicker.vue'
import LootAmountField from '~/views/editorview/loot/LootAmountField.vue'
import type {LootAmount, LootEntry} from '~/api/generated/payload'
import type {ItemChoice} from '~/util/items/itemChoices'
import {stackSize, withinStack} from '~/util/loot/lootTables'

/**
 * One entry of a loot table: its item, its weight with the chance that gives it to fill a slot, how many drop, and
 * whether it can drop more than once in one kill. Each change is saved as the whole entry.
 */
const props = defineProps<{
  entry: LootEntry
  index: number
  share: number
  choices: readonly ItemChoice[]
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [entry: LootEntry]
  remove: []
}>()

const {t} = useI18n()
const session = useEditorSession()
const size = computed(() => stackSize(props.entry.item, session.payload.value))
const weight = ref<number | undefined>(undefined)

watch(() => props.entry.weight, value => {
  weight.value = value
}, {immediate: true})

function save(change: Partial<LootEntry>): void {
  const next = {...props.entry, ...change}
  if (JSON.stringify(next) !== JSON.stringify(props.entry)) emit('save', next)
}

function saveItem(item: string | undefined): void {
  if (item) save({item, amount: withinStack(props.entry.amount, stackSize(item, session.payload.value))})
}

function saveWeight(): void {
  save({weight: Math.max(0, Math.round(weight.value ?? 0))})
}
</script>

<template>
  <li :aria-label="t('editor.loot.entry', {number: index + 1})" class="flex flex-col gap-3 rounded-panel border border-line-soft p-3">
    <div class="flex flex-wrap items-end gap-3">
      <LabelledField :label="t('editor.loot.item')" class="min-w-56 flex-1">
        <ItemPicker :choices="choices" :label="t('editor.loot.item')" :model-value="entry.item" :read-only="readOnly" @update:model-value="saveItem"/>
      </LabelledField>
      <LabelledField v-slot="{id}" :label="t('editor.loot.weight')" class="w-28">
        <NumberInput :id="id" v-model="weight" :disabled="readOnly" @focusout="saveWeight"/>
      </LabelledField>
      <span class="pb-2 text-sm text-ink-muted">{{ t('editor.loot.share', {percent: Math.round(share * 100)}) }}</span>
      <MutedIconButton v-if="!readOnly" :label="t('editor.loot.removeEntry')" class="mb-1 ml-auto" hover="error" icon="trash" @click="emit('remove')"/>
    </div>
    <LootAmountField :read-only="readOnly" :stack-size="size" :value="entry.amount" @save="(amount: LootAmount) => save({amount})"/>
    <ToggleSetting :disabled="readOnly" :hint="t('editor.loot.repeatableHint')" :label="t('editor.loot.repeatable')" :model-value="entry.repeatable" @update:model-value="repeatable => save({repeatable})"/>
  </li>
</template>
