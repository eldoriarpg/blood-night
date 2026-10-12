/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import BaseBadge from './BaseBadge.vue'

/** A badge saying something worked or holds. */
</script>

<template>
  <BaseBadge bg-class="border-pin-number/50 bg-node-value/30" text-class="text-pin-number">
    <slot/>
  </BaseBadge>
</template>
