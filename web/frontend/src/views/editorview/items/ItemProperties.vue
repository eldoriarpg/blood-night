/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import ItemLook from '~/views/editorview/items/ItemLook.vue'
import ItemDurability from '~/views/editorview/items/ItemDurability.vue'
import ItemAttributes from '~/views/editorview/items/ItemAttributes.vue'
import ItemTooltipParts from '~/views/editorview/items/ItemTooltipParts.vue'
import type {ItemSectionEmits, ItemSectionProps} from '~/util/items/itemSection'

/**
 * What an item is beyond its texts and enchantments: how it looks, how it wears where it wears out, the attributes it
 * gives and which tooltip parts it hides.
 */
defineProps<ItemSectionProps>()

const emit = defineEmits<ItemSectionEmits>()
</script>

<template>
  <div class="flex flex-col gap-4">
    <ItemLook :info="info" :item="item" :read-only="readOnly" @save="(key, value) => emit('save', key, value)"/>
    <ItemDurability v-if="info.durability > 0" :info="info" :item="item" :read-only="readOnly" @save="(key, value) => emit('save', key, value)"/>
    <ItemAttributes :info="info" :item="item" :read-only="readOnly" @save="(key, value) => emit('save', key, value)"/>
    <ItemTooltipParts :info="info" :item="item" :read-only="readOnly" @save="(key, value) => emit('save', key, value)"/>
  </div>
</template>
