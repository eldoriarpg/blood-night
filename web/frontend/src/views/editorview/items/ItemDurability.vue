/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import SectionHeader from '@/components/typography/SectionHeader.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import SliderInput from '@/components/input/range/SliderInput.vue'
import MutedText from '@/components/typography/MutedText.vue'
import type {EditorItem, MaterialInfo} from '~/api/generated/payload'

/**
 * How an item that wears out holds up: whether it is unbreakable, and how worn it already is, as a slider with the
 * uses that leaves. Shown only for items that wear out.
 */
const props = defineProps<{
  item: EditorItem
  info: MaterialInfo
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [key: keyof EditorItem, value: unknown]
}>()

const {t} = useI18n()
const wear = ref(0)

watch(() => props.item.wear, value => {
  wear.value = Math.round((value ?? 0) * 100)
}, {immediate: true})

const left = computed(() => Math.max(1, Math.round(props.info.durability * (1 - wear.value / 100))))

function saveWear(value: number): void {
  emit('save', 'wear', value <= 0 ? undefined : Math.min(100, value) / 100)
}
</script>

<template>
  <fieldset class="flex flex-col gap-3">
    <legend class="pb-1"><SectionHeader>{{ t('editor.items.durability.title') }}</SectionHeader></legend>
    <ToggleSetting :disabled="readOnly" :label="t('editor.items.durability.unbreakable')" :model-value="Boolean(item.unbreakable)" @update:model-value="value => emit('save', 'unbreakable', value)"/>
    <div class="flex flex-col gap-1">
      <FieldLabel>{{ t('editor.items.durability.wear') }}</FieldLabel>
      <SliderInput v-model="wear" :disabled="readOnly" :label="t('editor.items.durability.wear')" :max="100" :min="0" :step="1" :value-text="t('editor.items.durability.left', {left, max: info.durability})" @commit="saveWear"/>
      <MutedText size="sm" tag="p">{{ t('editor.items.durability.left', {left, max: info.durability}) }}</MutedText>
    </div>
  </fieldset>
</template>
