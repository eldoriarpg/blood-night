/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import type {RouteLocationRaw} from 'vue-router'
import AppIcon from '@/components/display/AppIcon.vue'

/**
 * One entry of a dropdown.
 *
 * <p>`destructive` is the entry that deletes something. It is coloured, and by convention it is the
 * last one in the menu.
 *
 * <p>`to` is the entry that opens a page rather than doing something, and such an entry is drawn as
 * the link it is, so it can be middle-clicked, copied and announced. An entry the reader may not
 * take stays a disabled button, because a link cannot be switched off.
 */
const props = defineProps<{
  icon?: string | string[]
  iconClass?: string
  disabled?: boolean
  destructive?: boolean
  to?: RouteLocationRaw | null
}>()

defineEmits<{
  click: []
}>()

const opensPage = computed(() => props.to != null && !props.disabled)

const entryClass = computed(() => [
  'w-full text-left px-3 py-2 text-sm flex items-center gap-2 transition-colors cursor-pointer hover:bg-hover disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-transparent',
  props.destructive ? 'text-problem' : 'text-ink',
])

const glyphClass = computed(() => [
  'w-4',
  props.iconClass ?? (props.destructive ? 'text-problem' : 'text-ink-muted'),
])
</script>

<template>
  <NuxtLink
      v-if="opensPage"
      :to="props.to!"
      :class="[entryClass, 'row-link no-underline hover:no-underline']"
  >
    <AppIcon v-if="icon" :icon="icon" :class="glyphClass"/>
    <slot/>
  </NuxtLink>
  <button
      v-else
      type="button"
      :disabled="disabled"
      :class="entryClass"
      @click="$emit('click')"
  >
    <AppIcon v-if="icon" :icon="icon" :class="glyphClass"/>
    <slot/>
  </button>
</template>
