/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import AppIcon from '@/components/display/AppIcon.vue'

/** Something is on its way, in the accent text colour. */
defineProps<{
  size?: 'sm' | 'md' | 'lg'
}>()
</script>

<template>
  <AppIcon
      :class="[size === 'sm' ? 'text-base' : size === 'lg' ? 'text-3xl' : 'text-xl', 'text-accent-text']"
      :icon="['fas', 'spinner']"
      spin
  />
</template>
