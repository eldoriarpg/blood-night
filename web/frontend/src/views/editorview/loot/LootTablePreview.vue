/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SectionHeader from '@/components/typography/SectionHeader.vue'
import LootItem from '~/views/editorview/loot/LootItem.vue'
import type {LootTable} from '~/api/generated/payload'
import type {ItemChoice} from '~/util/items/itemChoices'
import {entryShares, pick, rollAmount, usualAmount} from '~/util/loot/lootTables'

/** One pick of a table: the item and how many of it. */
interface Pick {
  item: string
  amount: number
}

/**
 * What one slot of a table drops: each entry's chance to be picked and its usual amount, and a button rolling one
 * pick the way the game does.
 */
const props = defineProps<{
  table: LootTable
  choices: readonly ItemChoice[]
}>()

const {t, locale} = useI18n()
const rolled = ref<Pick | null>(null)

const shares = computed(() => entryShares(props.table))

function rollOnce(): void {
  const picked = pick(props.table.entries.map(entry => ({value: entry, weight: entry.weight})), Math.random)
  rolled.value = picked ? {item: picked.item, amount: rollAmount(picked.amount, Math.random)} : null
}
</script>

<template>
  <section :aria-label="t('editor.loot.preview')" class="flex flex-col gap-2">
    <SectionHeader>{{ t('editor.loot.preview') }}</SectionHeader>
    <MutedText size="sm" tag="p">{{ t('editor.loot.previewHint') }}</MutedText>
    <table class="w-full text-sm">
      <thead class="text-left text-xs text-ink-muted">
        <tr><th class="py-1 font-normal">{{ t('editor.loot.item') }}</th><th class="font-normal">{{ t('editor.loot.shareColumn') }}</th><th class="font-normal">{{ t('editor.loot.usualAmount') }}</th></tr>
      </thead>
      <tbody>
        <tr v-for="(entry, index) in table.entries" :key="index" class="border-t border-line-soft">
          <td class="py-1"><LootItem :choices="choices" :item="entry.item"/></td>
          <td>{{ Math.round((shares[index] ?? 0) * 100) }} %</td>
          <td>{{ usualAmount(entry.amount).toLocaleString(locale) }}</td>
        </tr>
      </tbody>
    </table>
    <div class="flex flex-wrap items-center gap-3">
      <SecondaryButton compact icon="dice" @click="rollOnce">{{ t('editor.loot.roll') }}</SecondaryButton>
      <LootItem v-if="rolled" :amount="rolled.amount" :choices="choices" :item="rolled.item" aria-live="polite"/>
    </div>
  </section>
</template>
