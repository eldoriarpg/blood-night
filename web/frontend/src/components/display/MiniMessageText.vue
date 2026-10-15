/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {preview} from '~/util/minimessage/preview'

/**
 * MiniMessage drawn as players see it, in Minecraft's font where it is available. Text without a colour of its own
 * takes the base colour, e.g. the purple Minecraft gives lore.
 */
const props = defineProps<{
  text: string
  /** Placeholders and the sample shown for them, e.g. `{name: 'Blood Night'}`. */
  samples?: Readonly<Record<string, string>>
  /** The colour of text that sets none, as CSS. */
  base?: string
}>()

const segments = computed(() => preview(props.text, props.samples))
</script>

<template>
  <span class="min-w-0 whitespace-pre-wrap font-[Minecraft,monospace]">
    <span
        v-for="(segment, index) in segments"
        :key="index"
        :style="{
          color: segment.color ?? base,
          fontWeight: segment.bold ? 'bold' : undefined,
          fontStyle: segment.italic ? 'italic' : undefined,
          textDecoration: [segment.underlined ? 'underline' : '', segment.strikethrough ? 'line-through' : ''].join(' ').trim() || undefined,
        }"
    >{{ segment.text }}</span>
  </span>
</template>
