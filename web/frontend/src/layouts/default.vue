/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
/** The frame of every page that is not the editor: the header bar above the page and the footer below it. */
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <AppHeader/>
    <main class="flex-1">
      <slot/>
    </main>
    <AppFooter/>
  </div>
</template>
