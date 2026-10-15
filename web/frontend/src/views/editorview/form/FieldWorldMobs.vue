/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import MutedText from '@/components/typography/MutedText.vue'
import AppIcon from '@/components/display/AppIcon.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import ToggleInput from '@/components/input/toggle/ToggleInput.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import {plainText} from '~/util/items/itemChoices'
import {spawnsNaturally} from '~/util/mobs/naturalSpawns'
import {WORLD_KIND} from '~/util/settings/worldKind'

/**
 * Which mobs spawn in a world: every mob, grouped by the entity type it is based on, with a switch and its weight here, its own weight until one is set for the
 * world. A mob switched off is stored with weight 0, one at its own weight not at all. A mob whose type never spawns
 * by itself in this kind of world is marked, since it then only takes the place of mobs brought in some other way,
 * and one button switches all of those off.
 */
const props = defineProps<{
  label: string
  value: Readonly<Record<string, unknown>> | undefined
  help?: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: Record<string, number>]
}>()

const {t} = useI18n()
const session = useEditorSession()
const assets = useItemAssets()
const world = inject(WORLD_KIND, ref(undefined))

interface Row {
  id: string
  name: string
  type: string
  own: number
  natural: boolean
}

const rows = computed<Row[]>(() => Object.entries(session.payload.value?.mobs ?? {})
  .map(([id, mob]) => ({
    id,
    name: plainText(mob.attributes.displayName) || id,
    type: assets.entry('entity_type', `minecraft:${mob.attributes.entityType.toLowerCase()}`) ?? mob.attributes.entityType,
    own: mob.spawn.weight,
    natural: spawnsNaturally(mob.attributes.entityType, mob.attributes.exactType, world.value?.environment ?? 'CUSTOM'),
  }))
  .sort((a, b) => a.name.localeCompare(b.name)))
const groups = computed(() => {
  const byType = new Map<string, Row[]>()
  for (const row of rows.value) byType.set(row.type, [...byType.get(row.type) ?? [], row])
  return [...byType.entries()].sort(([a], [b]) => a.localeCompare(b)).map(([type, members]) => ({type, rows: members, natural: members[0]?.natural ?? true}))
})

const weights = ref<Record<string, number | undefined>>({})

watch([() => props.value, rows], () => {
  weights.value = Object.fromEntries(rows.value.map(row => [row.id, stored(row.id) ?? row.own]))
}, {immediate: true})

function stored(id: string): number | undefined {
  const weight = props.value?.[id]
  return typeof weight === 'number' ? weight : undefined
}

function save(): void {
  const known = new Set(rows.value.map(row => row.id))
  const kept = Object.entries(props.value ?? {}).filter(([id, weight]) => !known.has(id) && typeof weight === 'number') as [string, number][]
  const set = rows.value.flatMap(row => {
    const weight = Math.max(0, Math.round(weights.value[row.id] ?? row.own))
    return weight === row.own ? [] : [[row.id, weight] as [string, number]]
  })
  emit('save', Object.fromEntries([...kept, ...set]))
}

const strangers = computed(() => rows.value.filter(row => !row.natural && (weights.value[row.id] ?? 0) > 0))

function switchOffStrangers(): void {
  for (const row of strangers.value) weights.value[row.id] = 0
  save()
}

function toggle(row: Row, on: boolean): void {
  weights.value[row.id] = on ? (row.own > 0 ? row.own : 1) : 0
  save()
}
</script>

<template>
  <fieldset class="flex flex-col gap-1">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <MutedText v-if="help" size="sm" tag="p">{{ help }}</MutedText>
    <div v-if="strangers.length && !readOnly" class="flex flex-wrap items-center gap-3 rounded-control border border-held/50 bg-held-bg px-3 py-2 text-sm text-held">
      <AppIcon icon="triangle-exclamation"/>
      <span class="min-w-0 flex-1">{{ t('editor.worldMobs.strangers', {count: strangers.length}) }}</span>
      <SecondaryButton compact @click="switchOffStrangers">{{ t('editor.worldMobs.switchOff') }}</SecondaryButton>
    </div>
    <section v-for="group in groups" :key="group.type" :aria-label="group.type" class="flex flex-col pt-2">
      <h4 class="flex items-center gap-2 border-b border-line py-1 text-xs font-semibold uppercase tracking-wide text-ink-muted">
        {{ group.type }}
        <span class="font-normal normal-case tracking-normal text-ink-faint">{{ group.rows.length === 1 ? t('editor.worldMobs.oneMob') : t('editor.worldMobs.mobs', {count: group.rows.length}) }}</span>
      </h4>
      <div v-for="row in group.rows" :key="row.id" class="flex flex-col gap-1 border-b border-line-soft py-2">
        <div class="flex items-center gap-3">
          <ToggleInput
              :aria-label="t('editor.worldMobs.spawns', {name: row.name})"
              :disabled="readOnly"
              :model-value="(weights[row.id] ?? 0) > 0"
              @update:model-value="toggle(row, $event)"
          />
          <span class="min-w-0 flex-1 truncate text-sm">{{ row.name }}</span>
          <template v-if="(weights[row.id] ?? 0) > 0">
            <span class="text-xs text-ink-muted">{{ t('editor.worldMobs.weight') }}</span>
            <div class="w-24">
              <NumberInput v-model="weights[row.id]" :aria-label="t('editor.worldMobs.weightOf', {name: row.name})" :disabled="readOnly" @focusout="save"/>
            </div>
          </template>
        </div>
        <p v-if="!row.natural && (weights[row.id] ?? 0) > 0" class="flex items-center gap-2 pl-14 text-xs text-held">
          <AppIcon icon="triangle-exclamation"/>
          {{ t('editor.worldMobs.notNatural', {type: row.type, world: t(`editor.worldMobs.environment.${world?.environment ?? 'CUSTOM'}`)}) }}
        </p>
      </div>
    </section>
  </fieldset>
</template>
