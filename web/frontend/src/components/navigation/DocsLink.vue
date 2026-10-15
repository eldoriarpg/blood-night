/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import AppIcon from '@/components/display/AppIcon.vue'

/** A small link to a page of the documentation, opened beside the editor. */
defineProps<{
  href: string
}>()
</script>

<template>
  <a :href="href" class="inline-flex items-center gap-1.5 text-xs text-ink-faint hover:text-accent-text" rel="noopener" target="_blank">
    <AppIcon aria-hidden="true" icon="book-open"/>
    <slot/>
  </a>
</template>
