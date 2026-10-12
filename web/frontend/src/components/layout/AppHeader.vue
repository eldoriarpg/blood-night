/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'

/** The bar across the top of every page: the name of the application, linking home. */
const {t} = useI18n()
</script>

<template>
  <header class="border-b border-line-soft bg-surface">
    <div class="mx-auto flex h-14 w-full max-w-5xl items-center px-4 sm:px-6">
      <NuxtLink to="/" class="row-link font-heading text-lg font-bold text-ink hover:text-accent-text">
        {{ t('app.name') }}
      </NuxtLink>
    </div>
  </header>
</template>
