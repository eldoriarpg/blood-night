/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
/** One colour of the palette: a chip painted with it and the name it goes by. */
defineProps<{
  name: string
  colorClass: string
  round?: boolean
}>()
</script>

<template>
  <div class="flex items-center gap-2">
    <span :class="[colorClass, round ? 'h-3.5 w-3.5 rounded-full' : 'h-6 w-10 rounded-node border border-line']"/>
    <code class="text-xs text-ink-muted">{{ name }}</code>
  </div>
</template>
