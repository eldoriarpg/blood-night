/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import BaseButton from './BaseButton.vue'

/** The button a screen is answered with: the accent fill. */
</script>

<template>
  <BaseButton class="border-accent bg-accent text-accent-contrast enabled:hover:border-accent-hover enabled:hover:bg-accent-hover">
    <slot/>
  </BaseButton>
</template>
