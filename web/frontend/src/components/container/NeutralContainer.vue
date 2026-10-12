/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import BaseContainer from './BaseContainer.vue'

/** The plain panel on the surface colour. `clickable` gives it the hover of a panel that opens something. */
defineProps<{
  clickable?: boolean
}>()
</script>

<template>
  <BaseContainer
      :class="clickable ? 'cursor-pointer hover:border-ink-faint hover:bg-hover' : ''"
      class="border-line-soft bg-surface">
    <slot/>
  </BaseContainer>
</template>
