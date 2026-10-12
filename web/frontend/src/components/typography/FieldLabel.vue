/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
/**
 * The label of a field. `hint` makes it the small muted label of an optional field, `inline` lays a
 * control out beside its text.
 */
defineProps<{
  hint?: boolean
  inline?: boolean
}>()
</script>

<template>
  <label :class="[
    hint ? 'text-xs text-ink-muted' : 'text-sm text-ink',
    inline ? 'flex items-center gap-2' : 'block font-medium',
  ]">
    <slot/>
  </label>
</template>
