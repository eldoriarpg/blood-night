/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import SectionHeader from '@/components/typography/SectionHeader.vue'
import PrimaryBadge from '@/components/badge/PrimaryBadge.vue'
import SecondaryBadge from '@/components/badge/SecondaryBadge.vue'
import SuccessBadge from '@/components/badge/SuccessBadge.vue'
import ErrorBadge from '@/components/badge/ErrorBadge.vue'
import InfoBadge from '@/components/badge/InfoBadge.vue'
import CountBadge from '@/components/badge/CountBadge.vue'
import PillBadge from '@/components/badge/PillBadge.vue'

/** The badges: the colour variants, the count dot and the bare pill a caller colours itself. */
</script>

<template>
  <section class="space-y-4">
    <SectionHeader>Badges</SectionHeader>
    <div class="flex flex-wrap items-center gap-2">
      <PrimaryBadge>Primary</PrimaryBadge>
      <SecondaryBadge>Secondary</SecondaryBadge>
      <SuccessBadge>Valid</SuccessBadge>
      <ErrorBadge>2 problems</ErrorBadge>
      <InfoBadge>Held by Steve</InfoBadge>
      <CountBadge :count="3"/>
      <CountBadge :count="12" small/>
      <PillBadge class="bg-node-event px-2 py-0.5 text-xs font-semibold text-ink">Event</PillBadge>
      <PillBadge class="bg-node-macro px-2 py-0.5 text-xs font-semibold text-ink">Macro</PillBadge>
    </div>
  </section>
</template>
