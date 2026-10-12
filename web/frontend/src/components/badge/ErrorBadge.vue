/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import BaseBadge from './BaseBadge.vue'

/** A badge saying something is wrong. */
</script>

<template>
  <BaseBadge bg-class="border-problem/50 bg-problem-bg" text-class="text-problem">
    <slot/>
  </BaseBadge>
</template>
