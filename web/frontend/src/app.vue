/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {useI18n} from 'vue-i18n'

/**
 * The root of the application: the language of the page, the title every tab carries, and the
 * toasts, which only the browser shows.
 */
const {t, locale} = useI18n()

useHead(() => ({
  titleTemplate: (title?: string) => (title ? `${title} - ${t('app.name')}` : t('app.name')),
  htmlAttrs: {lang: locale.value},
}))
</script>

<template>
  <NuxtLayout>
    <NuxtPage/>
  </NuxtLayout>
  <ClientOnly>
    <ToastContainer/>
  </ClientOnly>
</template>
