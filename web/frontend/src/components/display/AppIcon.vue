/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'

/**
 * One FontAwesome icon, as every component taking an `icon` prop draws it.
 *
 * <p>The icon is named as a pair, `['fas', 'xmark']`, or by its bare name, which means the solid set.
 * Only icons registered in `plugins/fontawesome.ts` are drawn; the icon linter holds every pair a
 * file names to that list.
 */
const props = defineProps<{
  /** `['fas', 'xmark']`, or a bare name of the solid set. */
  icon: string | string[]
}>()

const pair = computed(() => (Array.isArray(props.icon) ? props.icon : ['fas', props.icon]))
</script>

<template>
  <font-awesome-icon :icon="pair"/>
</template>
