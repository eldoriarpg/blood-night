/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import type {ItemChoice} from '~/util/items/itemChoices'

/** An item of a loot table as players would recognise it: its icon and its name, and how many when there is a count. */
const props = defineProps<{
  item: string
  choices: readonly ItemChoice[]
  amount?: number
}>()

const assets = useItemAssets()
const choice = computed(() => props.choices.find(candidate => candidate.key === props.item))
</script>

<template>
  <span class="inline-flex min-w-0 items-center gap-2">
    <img v-if="assets.icon(choice?.texture)" :src="assets.icon(choice?.texture)" alt="" class="bn-icon size-6 shrink-0"/>
    <span class="truncate">{{ amount !== undefined ? `${amount} × ` : '' }}{{ choice?.name ?? item }}</span>
  </span>
</template>
