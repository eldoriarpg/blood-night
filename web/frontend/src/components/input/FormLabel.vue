/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
/** A label above a field, pointing at it by id. */
defineProps<{ for?: string }>()
</script>

<template>
  <label :for="$props.for" class="text-sm text-ink-muted mb-1 block">
    <slot />
  </label>
</template>
