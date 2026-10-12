/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import PageHeader from '@/components/typography/PageHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'

/**
 * The body of a page under the default layout: its heading, an optional line under it, and the
 * content. The title also names the browser tab, so a page is titled in one place.
 */
const props = defineProps<{
  title: string
  subtitle?: string
}>()

useHead(() => ({title: props.title}))
</script>

<template>
  <div class="mx-auto w-full max-w-5xl px-4 py-6 sm:px-6 sm:py-8">
    <header class="mb-6 space-y-1 sm:mb-8">
      <PageHeader>{{ title }}</PageHeader>
      <MutedText v-if="subtitle" size="sm" tag="p">{{ subtitle }}</MutedText>
    </header>
    <slot/>
  </div>
</template>
