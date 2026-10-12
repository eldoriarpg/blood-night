/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import BaseBadge from './BaseBadge.vue'

/** A badge for something to look at, such as a section another person holds. */
</script>

<template>
  <BaseBadge bg-class="border-held/50 bg-held-bg" text-class="text-held">
    <slot/>
  </BaseBadge>
</template>
