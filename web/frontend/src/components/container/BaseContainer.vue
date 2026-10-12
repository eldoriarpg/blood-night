/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
/** The panel every container is built from: the panel radius, a border, and padding unless told otherwise. */
withDefaults(defineProps<{
  padded?: boolean
}>(), {
  padded: true,
})
</script>

<template>
  <div
      :class="padded ? 'p-4' : ''"
      class="rounded-panel border transition-colors duration-150"
  >
    <slot/>
  </div>
</template>
