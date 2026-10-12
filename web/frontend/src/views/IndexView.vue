/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import ViewContent from '@/components/layout/ViewContent.vue'
import NeutralContainer from '@/components/container/NeutralContainer.vue'
import MutedText from '@/components/typography/MutedText.vue'

/**
 * The landing page. The editor itself is never reached from here: a server opens a session and
 * hands the player a link, so all this page does is say where that link comes from.
 */
const {t} = useI18n()

const COMMAND = '/bloodnight editor'
</script>

<template>
  <ViewContent :title="t('pages.index.title')">
    <NeutralContainer class="max-w-2xl space-y-3">
      <p class="text-ink">{{ t('pages.index.lead') }}</p>
      <i18n-t keypath="pages.index.howTo" tag="p" scope="global" class="text-ink">
        <template #command>
          <code class="rounded-node border border-line bg-raised px-1.5 py-0.5 text-sm text-accent-text">{{ COMMAND }}</code>
        </template>
      </i18n-t>
      <MutedText size="sm" tag="p">{{ t('pages.index.link') }}</MutedText>
    </NeutralContainer>
  </ViewContent>
</template>
