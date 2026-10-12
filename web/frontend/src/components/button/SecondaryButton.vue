/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import BaseButton from './BaseButton.vue'

/** Every other action: the raised surface with a line around it. */
</script>

<template>
  <BaseButton class="border-line bg-raised text-ink enabled:hover:bg-hover">
    <slot/>
  </BaseButton>
</template>
