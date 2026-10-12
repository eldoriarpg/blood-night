/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import SectionHeader from '@/components/typography/SectionHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'
import BaseContainer from '@/components/container/BaseContainer.vue'
import NeutralContainer from '@/components/container/NeutralContainer.vue'
import PrimaryContainer from '@/components/container/PrimaryContainer.vue'
import SecondaryContainer from '@/components/container/SecondaryContainer.vue'
import SuccessContainer from '@/components/container/SuccessContainer.vue'
import ErrorContainer from '@/components/container/ErrorContainer.vue'
import InfoContainer from '@/components/container/InfoContainer.vue'

/** The panels, one of each variant. */
</script>

<template>
  <section class="space-y-4">
    <SectionHeader>Containers</SectionHeader>
    <div class="grid gap-3 sm:grid-cols-2">
      <BaseContainer class="border-line-soft"><MutedText size="sm">Base</MutedText></BaseContainer>
      <NeutralContainer clickable><MutedText size="sm">Neutral, clickable</MutedText></NeutralContainer>
      <PrimaryContainer><MutedText size="sm">Primary</MutedText></PrimaryContainer>
      <SecondaryContainer><MutedText size="sm">Secondary</MutedText></SecondaryContainer>
    </div>
    <div class="grid gap-3 sm:grid-cols-2">
      <SuccessContainer><MutedText size="sm">Success</MutedText></SuccessContainer>
      <ErrorContainer><MutedText size="sm">Error</MutedText></ErrorContainer>
      <InfoContainer><MutedText size="sm">Info</MutedText></InfoContainer>
    </div>
  </section>
</template>
