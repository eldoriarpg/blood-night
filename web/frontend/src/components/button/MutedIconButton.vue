/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import IconButton from '@/components/button/IconButton.vue'

/**
 * An IconButton in the muted text colour that shifts to a hover accent, for low-emphasis row
 * actions.
 */
const props = defineProps<{
  icon: string | string[]
  label: string
  disabled?: boolean
  hover?: 'primary' | 'error' | 'text'
}>()

defineEmits<{
  click: [event: MouseEvent]
}>()

const HOVER_CLASSES = {
  primary: 'hover:text-accent-text',
  error: 'hover:text-problem',
  text: 'hover:text-ink',
} as const

const hoverClass = computed(() => HOVER_CLASSES[props.hover ?? 'primary'])
</script>

<template>
  <IconButton
      :icon="icon"
      :label="label"
      :disabled="disabled"
      :class="['text-ink-muted', hoverClass]"
      @click="$emit('click', $event)"
  />
</template>
