/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {TooltipArrow, TooltipContent, TooltipPortal, TooltipProvider, TooltipRoot, TooltipTrigger} from 'reka-ui'
import type {PortView} from '~/util/graph/graphView'

/**
 * Shows a pin's name, type and documentation when the pointer rests on its row. Rendered at the body, so the
 * canvas's zoom neither scales nor clips it; a pin without documentation still shows its type.
 */
defineProps<{
  port: PortView
  side: 'left' | 'right'
}>()
</script>

<template>
  <TooltipProvider :delay-duration="350" :skip-delay-duration="150">
    <TooltipRoot>
      <TooltipTrigger as-child>
        <slot/>
      </TooltipTrigger>
      <TooltipPortal>
        <TooltipContent
            :side="side"
            :side-offset="10"
            class="z-[90] max-w-72 rounded-control border border-line bg-raised px-3 py-2 text-xs text-ink"
            data-testid="port-tooltip"
        >
          <span class="flex items-baseline gap-2">
            <span class="font-semibold">{{ port.name }}</span>
            <span v-if="port.typeName" class="font-mono text-ink-faint">{{ port.typeName }}</span>
          </span>
          <span v-if="port.description" class="mt-1 block text-ink-muted">{{ port.description }}</span>
          <TooltipArrow class="fill-raised"/>
        </TooltipContent>
      </TooltipPortal>
    </TooltipRoot>
  </TooltipProvider>
</template>
