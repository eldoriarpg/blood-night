/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import FieldPercent from '~/views/editorview/form/FieldPercent.vue'
import FieldDayTime from '~/views/editorview/form/FieldDayTime.vue'
import FieldUnchanged from '~/views/editorview/form/FieldUnchanged.vue'
import FieldMap from '~/views/editorview/form/FieldMap.vue'
import FieldDropWeights from '~/views/editorview/form/FieldDropWeights.vue'
import FieldLootTables from '~/views/editorview/form/FieldLootTables.vue'
import FieldExperience from '~/views/editorview/form/FieldExperience.vue'
import FieldMoney from '~/views/editorview/form/FieldMoney.vue'
import type {Experience, Money} from '~/api/generated/payload'
import FieldGameRules from '~/views/editorview/form/FieldGameRules.vue'
import FieldMiniMessage from '~/views/editorview/form/FieldMiniMessage.vue'
import FieldMoonPhases from '~/views/editorview/form/FieldMoonPhases.vue'
import FieldPhaseCycle from '~/views/editorview/form/FieldPhaseCycle.vue'
import FieldCurve from '~/views/editorview/form/FieldCurve.vue'
import FieldWorldMobs from '~/views/editorview/form/FieldWorldMobs.vue'
import FieldMobName from '~/views/editorview/form/FieldMobName.vue'
import type {Field} from '~/util/settings/schemaFields'
import type {SpecialField} from '~/util/settings/specialFields'
import {mobLabel} from '~/util/mobs/mobName'

/**
 * A settings field drawn its own way (see {@link SpecialField}): chances as sliders, times of day as a clock, effects,
 * mobs, game rules and loot tables picked by name, and MiniMessage with a preview. The mob a dragon fight's dragon
 * becomes is picked from the mobs based on the Ender Dragon only. A mob's or a world's loot settings may be left to the
 * next broader level, which the field names; the money of ordinary enemies is not passed down, so it never is.
 */
const props = defineProps<{
  special: SpecialField
  field: Field
  label: string
  help?: string
  value: unknown
  path: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: unknown]
}>()

const {t, locale} = useI18n()
const session = useEditorSession()
const assets = useItemAssets()

onMounted(() => {
  void assets.load()
})

const number = computed(() => typeof props.value === 'number' ? props.value : undefined)
const numbers = computed(() => Array.isArray(props.value) ? props.value.filter((entry): entry is number => typeof entry === 'number') : undefined)
const text = computed(() => typeof props.value === 'string' ? props.value : undefined)
const object = computed(() => props.value !== null && typeof props.value === 'object' && !Array.isArray(props.value) ? props.value as Record<string, unknown> : undefined)

const effects = computed(() => session.payload.value?.choices?.effects ?? [])
const effectLabels = computed(() => Object.fromEntries(effects.value.map(key => [key, assets.entry('effect', key) ?? key])))
const mobs = computed(() => Object.keys(session.payload.value?.mobs ?? {}).sort())
const dragons = computed(() => mobs.value.filter(id => session.payload.value?.mobs?.[id]?.attributes?.entityType === 'ENDER_DRAGON'))
const mobLabels = computed(() => Object.fromEntries(mobs.value.map(id => [id, mobLabel(session.payload.value?.mobs?.[id]?.attributes?.name, locale.value) || id])))
const texts = computed(() => Array.isArray(props.value) ? props.value.filter((entry): entry is string => typeof entry === 'string') : undefined)
const experience = computed(() => object.value as Experience | undefined)
const money = computed(() => object.value as Money | undefined)
const economy = computed(() => session.payload.value?.choices?.economy === true)
const inherited = computed(() => {
  if (props.path.startsWith('/mobs/')) return t('editor.loot.asBroader.mob')
  if (props.path.startsWith('/settings/worlds/')) return t('editor.loot.asBroader.world')
  return undefined
})
const moneyInherited = computed(() => props.path.includes('/ordinary/') ? undefined : inherited.value)
const samples = computed(() => ({name: t('editor.miniMessage.nightName'), player: 'Steve', world: 'world'}))
</script>

<template>
  <FieldPercent v-if="special === 'percent'" :help="help" :label="label" :read-only="readOnly" :value="number" @save="emit('save', $event)"/>
  <FieldDayTime v-else-if="special === 'dayTime'" :help="help" :label="label" :read-only="readOnly" :value="number" @save="emit('save', $event)"/>
  <FieldUnchanged
      v-else-if="special === 'unchanged' || special === 'unchangedSeconds'"
      :help="help"
      :label="label"
      :read-only="readOnly"
      :ticks="special === 'unchangedSeconds'"
      :value="number"
      @save="emit('save', $event)"
  />
  <LabelledField v-else-if="special === 'effect'" :help="help" :label="label">
    <ChoicePicker :label="label" :labels="effectLabels" :model-value="text" :options="effects" :read-only="readOnly" @update:model-value="emit('save', $event)"/>
  </LabelledField>
  <LabelledField v-else-if="special === 'mob'" :help="help" :label="label">
    <ChoicePicker :label="label" :labels="mobLabels" :model-value="text" :options="dragons" :read-only="readOnly" @update:model-value="emit('save', $event)"/>
  </LabelledField>
  <FieldMap
      v-else-if="special === 'mobMap' && field.item"
      :item="field.item"
      :add-label="t('editor.form.addMob')"
      :key-labels="mobLabels"
      :keys="mobs"
      :label="label"
      :path="path"
      :read-only="readOnly"
      :value="object"
      @save="emit('save', $event)"
  />
  <FieldDropWeights v-else-if="special === 'dropWeights'" :help="help" :inherited="inherited" :label="label" :read-only="readOnly" :value="object" @save="emit('save', $event)"/>
  <FieldLootTables v-else-if="special === 'lootTables'" :help="help" :label="label" :read-only="readOnly" :value="texts" @save="emit('save', $event)"/>
  <FieldExperience v-else-if="special === 'experience'" :help="help" :inherited="inherited" :label="label" :read-only="readOnly" :value="experience" @save="emit('save', $event)"/>
  <FieldMoney v-else-if="special === 'money'" :economy="economy" :help="help" :inherited="moneyInherited" :label="label" :read-only="readOnly" :value="money" @save="emit('save', $event)"/>
  <FieldMobName v-else-if="special === 'mobName'" :help="help" :label="label" :read-only="readOnly" :value="object" @save="emit('save', $event)"/>
  <FieldWorldMobs v-else-if="special === 'worldMobs'" :help="help" :label="label" :read-only="readOnly" :value="object" @save="emit('save', $event)"/>
  <FieldCurve v-else-if="special === 'curve'" :help="help" :label="label" :read-only="readOnly" :value="object" @save="emit('save', $event)"/>
  <FieldPhaseCycle v-else-if="special === 'phaseCycle'" :help="help" :label="label" :read-only="readOnly" :value="numbers" @save="emit('save', $event)"/>
  <FieldMoonPhases v-else-if="special === 'moonPhases'" :help="help" :label="label" :read-only="readOnly" :value="object" @save="emit('save', $event)"/>
  <FieldGameRules v-else-if="special === 'gameRules'" :label="label" :read-only="readOnly" :value="object" @save="emit('save', $event)"/>
  <FieldMiniMessage v-else :help="help" :label="label" :read-only="readOnly" :samples="samples" :value="text" @save="emit('save', $event)"/>
</template>
