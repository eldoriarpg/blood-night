/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import RangeSlider from '@/components/input/range/RangeSlider.vue'
import type {LootWear} from '~/api/generated/payload'

/**
 * How worn an entry's item drops: off drops it as worn as the item is; on rolls a share of its durability for every
 * drop, from one percentage to another, picked on a range slider.
 */
const props = defineProps<{
  value: LootWear | null | undefined
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: LootWear | undefined]
}>()

const {t} = useI18n()
const range = ref<[number, number]>([40, 90])

watch(() => props.value, value => {
  if (value) range.value = [Math.round(value.min * 100), Math.round(value.max * 100)]
}, {immediate: true})

function toggle(on: boolean): void {
  emit('save', on ? {min: range.value[0] / 100, max: range.value[1] / 100} : undefined)
}

function commit(value: [number, number]): void {
  emit('save', {min: value[0] / 100, max: value[1] / 100})
}
</script>

<template>
  <div class="flex flex-col gap-2">
    <ToggleSetting :disabled="readOnly" :hint="t('editor.loot.wear.hint')" :label="t('editor.loot.wear.label')" :model-value="Boolean(value)" @update:model-value="toggle"/>
    <RangeSlider
        v-if="value"
        v-model="range"
        :disabled="readOnly"
        :format="percent => `${percent} %`"
        :from-label="t('editor.loot.wear.from')"
        :max="100"
        :min="0"
        :step="5"
        :to-label="t('editor.loot.wear.to')"
        @commit="commit"
    />
  </div>
</template>
