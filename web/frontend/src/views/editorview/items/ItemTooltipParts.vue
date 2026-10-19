/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SectionHeader from '@/components/typography/SectionHeader.vue'
import CheckboxInput from '@/components/input/toggle/CheckboxInput.vue'
import type {TooltipPart} from '~/api/generated/payload'
import type {ItemSectionEmits, ItemSectionProps} from '~/util/items/itemSection'
import {hideableParts} from '~/util/items/itemLook'

/**
 * Which parts of an item's tooltip are left out: one checkbox per part the item shows. The item keeps what a hidden
 * part stands for.
 */
const props = defineProps<ItemSectionProps>()

const emit = defineEmits<ItemSectionEmits>()

const {t} = useI18n()
const parts = computed(() => hideableParts(props.item, props.info))
const hidden = computed(() => new Set(props.item.hidden ?? []))

function flip(part: TooltipPart, on: boolean): void {
  const next = new Set(hidden.value)
  if (on) next.add(part)
  else next.delete(part)
  emit('save', 'hidden', parts.value.filter(candidate => next.has(candidate)))
}
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend class="pb-1"><SectionHeader>{{ t('editor.items.hidden.title') }}</SectionHeader></legend>
    <div class="flex flex-wrap gap-x-5 gap-y-1">
      <label v-for="part in parts" :key="part" class="flex min-h-9 items-center gap-2 text-sm">
        <CheckboxInput :disabled="readOnly" :model-value="hidden.has(part)" @update:model-value="on => flip(part, on)"/>
        {{ t(`editor.items.hidden.${part}`) }}
      </label>
    </div>
  </fieldset>
</template>
