/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import ViewContent from '@/components/layout/ViewContent.vue'
import StylePalette from '@/views/styleview/StylePalette.vue'
import StyleTypography from '@/views/styleview/StyleTypography.vue'
import StyleButtons from '@/views/styleview/StyleButtons.vue'
import StyleInputs from '@/views/styleview/StyleInputs.vue'
import StyleBadges from '@/views/styleview/StyleBadges.vue'
import StyleContainers from '@/views/styleview/StyleContainers.vue'
import StyleFeedback from '@/views/styleview/StyleFeedback.vue'
import StyleDialogs from '@/views/styleview/StyleDialogs.vue'
import StyleNavigation from '@/views/styleview/StyleNavigation.vue'

/**
 * Every base component on one page, for looking at them side by side. A component added to
 * `src/components` is added here as well.
 */
const {t} = useI18n()
</script>

<template>
  <ViewContent :title="t('pages.style.title')" :subtitle="t('pages.style.subtitle')">
    <div class="space-y-10 sm:space-y-12">
      <StylePalette/>
      <StyleTypography/>
      <StyleButtons/>
      <StyleInputs/>
      <StyleBadges/>
      <StyleContainers/>
      <StyleFeedback/>
      <StyleDialogs/>
      <StyleNavigation/>
    </div>
  </ViewContent>
</template>
