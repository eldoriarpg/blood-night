/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import BareButton from '@/components/button/BareButton.vue'
import AppIcon from '@/components/display/AppIcon.vue'

/** Switches the editor between its dark and its light colours, showing the one it switches to. */
const {t} = useI18n()
const {theme, toggle} = useTheme()
</script>

<template>
  <BareButton
      :aria-label="theme === 'light' ? t('footer.dark') : t('footer.light')"
      :title="theme === 'light' ? t('footer.dark') : t('footer.light')"
      class="flex size-7 items-center justify-center rounded-control text-ink-faint hover:bg-hover hover:text-ink"
      @click="toggle"
  >
    <AppIcon :icon="theme === 'light' ? 'moon' : 'sun'"/>
  </BareButton>
</template>
