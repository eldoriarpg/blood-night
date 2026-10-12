/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {TooltipArrow, TooltipContent, TooltipPortal, TooltipProvider, TooltipRoot, TooltipTrigger} from 'reka-ui'
import AppIcon from '@/components/display/AppIcon.vue'

/**
 * A question mark in a node's header that shows what the node does while the pointer rests on it or it has focus.
 * Rendered at the body, so the canvas's zoom neither scales nor clips it.
 */
defineProps<{
  title: string
  description: string
  type: string
}>()

const {t} = useI18n()
</script>

<template>
  <TooltipProvider :delay-duration="150">
    <TooltipRoot>
      <TooltipTrigger
          :aria-label="t('editor.graph.help', {name: title})"
          class="nodrag flex size-4 shrink-0 items-center justify-center rounded-full text-[10px] text-white/75 hover:text-white focus-visible:text-white"
          data-testid="node-help"
      >
        <AppIcon icon="circle-question"/>
      </TooltipTrigger>
      <TooltipPortal>
        <TooltipContent
            :side-offset="8"
            class="z-[90] max-w-80 rounded-control border border-line bg-raised px-3 py-2 text-xs text-ink"
            data-testid="node-help-tooltip"
            side="top"
        >
          <span class="block font-semibold">{{ title }}</span>
          <span class="mt-1 block text-ink-muted">{{ description }}</span>
          <span class="mt-1 block font-mono text-ink-faint">{{ type }}</span>
          <TooltipArrow class="fill-raised"/>
        </TooltipContent>
      </TooltipPortal>
    </TooltipRoot>
  </TooltipProvider>
</template>
