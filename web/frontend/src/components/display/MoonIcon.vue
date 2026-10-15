/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {litPath, type MoonPhaseName} from '~/util/moon/moonPhases'

/** The moon in one of its phases: the dark disk with its lit part. Decorative; the phase is named beside it. */
const props = withDefaults(defineProps<{
  phase: MoonPhaseName
  size?: number
}>(), {size: 32})

const r = computed(() => props.size / 2)
</script>

<template>
  <svg :height="size" :viewBox="`0 0 ${size} ${size}`" :width="size" aria-hidden="true" class="shrink-0">
    <circle :cx="r" :cy="r" :r="r - 0.5" class="fill-[#1d1a2b] stroke-line"/>
    <path :d="litPath(phase, r)" class="fill-[#efe7c8]"/>
  </svg>
</template>
