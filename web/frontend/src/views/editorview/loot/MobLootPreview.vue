/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import LootItem from '~/views/editorview/loot/LootItem.vue'
import {mobLevels, pool, pooledKey, roll, simulate, slotChances, slotsOf, usualAmount, type Rolled} from '~/util/loot/lootTables'

/**
 * What a mob drops from loot tables when a player kills it in a world: every entry that can drop after the mob's items
 * replaced the same ones further up, the table and level it comes from, its chance per kill (from 10 000 simulated
 * kills) and usual amount, how many entries drop and whose count that is, and a button rolling one kill.
 */
const props = defineProps<{
  mobId: string
}>()

const session = useEditorSession()
const assets = useItemAssets()
const {t, locale} = useI18n()
const KILLS = 10_000
const world = ref<string | undefined>(undefined)
const kill = ref<Rolled[] | null>(null)

const choices = assets.choices(true)
const worlds = computed(() => Object.entries(session.payload.value?.settings?.worlds ?? {})
  .map(([uid, entry]) => ({uid, name: entry.name}))
  .sort((a, b) => a.name.localeCompare(b.name)))
const worldNames = computed(() => Object.fromEntries(worlds.value.map(entry => [entry.uid, entry.name])))
const chosenWorld = computed(() => world.value ?? worlds.value[0]?.uid)
const tables = computed(() => session.payload.value?.lootTables ?? {})
const levels = computed(() => mobLevels(session.payload.value, props.mobId, chosenWorld.value))
const pooled = computed(() => pool(levels.value, tables.value))
const slots = computed(() => slotsOf(levels.value))
const counts = computed(() => slotChances(slots.value.slots))
const chances = computed(() => simulate(levels.value, tables.value, KILLS, Math.random))
const sourceOf = computed(() => new Map(levels.value.flatMap(level => level.tables.map(id => [id, level.source] as const)).reverse()))

watch(() => [props.mobId, chosenWorld.value], () => {
  kill.value = null
})

function tableName(id: string): string {
  return tables.value[id]?.name || id
}

function countLabel(count: number): string {
  if (count === 0) return t('editor.dropWeights.nothing')
  return count === 1 ? t('editor.dropWeights.oneDrop') : t('editor.dropWeights.drops', {count})
}
</script>

<template>
  <section :aria-label="t('editor.mobLoot.title')" class="flex flex-col gap-2 py-3">
    <SectionLabel>{{ t('editor.mobLoot.title') }}</SectionLabel>
    <LabelledField v-if="worlds.length > 0" :label="t('editor.mobLoot.world')">
      <ChoicePicker :label="t('editor.mobLoot.world')" :labels="worldNames" :model-value="chosenWorld" :options="worlds.map(entry => entry.uid)" @update:model-value="world = $event"/>
    </LabelledField>
    <MutedText v-if="pooled.length === 0" size="sm" tag="p">{{ t('editor.mobLoot.empty') }}</MutedText>
    <template v-else>
      <MutedText size="sm" tag="p">
        {{ slots.source ? t(`editor.mobLoot.countFrom.${slots.source}`) : t('editor.mobLoot.noCount') }}
        <template v-for="(entry, index) in counts" :key="entry.count">{{ index > 0 ? ', ' : ' ' }}{{ countLabel(entry.count) }} {{ Math.round(entry.chance * 100) }} %</template>
      </MutedText>
      <ul class="flex flex-col gap-1 text-sm">
        <li v-for="entry in pooled" :key="pooledKey(entry)" class="flex items-center gap-2 border-t border-line-soft pt-1">
          <span class="flex min-w-0 flex-1 flex-col">
            <LootItem :choices="choices" :item="entry.entry.item"/>
            <span class="truncate text-xs text-ink-faint">{{ tableName(entry.table) }} · {{ t(`editor.mobLoot.level.${sourceOf.get(entry.table) ?? 'mob'}`) }}</span>
          </span>
          <span :title="t('editor.mobLoot.chanceHint')" class="shrink-0 tabular-nums">{{ Math.round((chances.get(pooledKey(entry)) ?? 0) * 100) }} %</span>
          <span :title="t('editor.loot.usualAmount')" class="w-14 shrink-0 text-right text-ink-muted">× {{ usualAmount(entry.entry.amount).toLocaleString(locale) }}</span>
        </li>
      </ul>
      <div class="flex flex-wrap items-center gap-2">
        <SecondaryButton compact icon="dice" @click="kill = roll(levels, tables, Math.random)">{{ t('editor.mobLoot.roll') }}</SecondaryButton>
        <span v-if="kill && kill.length === 0" class="text-sm text-ink-muted">{{ t('editor.dropWeights.nothing') }}</span>
        <LootItem v-for="(entry, index) in kill ?? []" :key="index" :amount="entry.amount" :choices="choices" :item="entry.entry.item" class="text-sm"/>
      </div>
    </template>
  </section>
</template>
