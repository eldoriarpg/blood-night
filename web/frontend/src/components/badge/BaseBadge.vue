/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
/**
 * The pill every badge is built from. The colour variants name their background and text classes;
 * on the fixed dark palette both are known up front, so nothing has to be measured.
 */
withDefaults(defineProps<{
  bgClass: string
  textClass?: string
}>(), {
  textClass: 'text-ink',
})
</script>

<template>
  <span
      class="inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-xs font-semibold"
      :class="[bgClass, textClass]"
  >
    <slot/>
  </span>
</template>
