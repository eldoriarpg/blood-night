/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import PageHeader from '@/components/typography/PageHeader.vue'
import SectionHeader from '@/components/typography/SectionHeader.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import FieldLabel from '@/components/typography/FieldLabel.vue'
import FieldHint from '@/components/typography/FieldHint.vue'
import MutedText from '@/components/typography/MutedText.vue'

/** The headings and text styles, from the page heading down to muted copy and code. */
</script>

<template>
  <section class="space-y-3">
    <SectionHeader>Typography</SectionHeader>
    <PageHeader>Page header</PageHeader>
    <SectionHeader>Section header</SectionHeader>
    <SubHeader>Sub header</SubHeader>
    <SectionLabel>Section label</SectionLabel>
    <FieldLabel>Field label</FieldLabel>
    <FieldHint>Field hint</FieldHint>
    <MutedText tag="p" size="sm">Muted text, for what is said in passing.</MutedText>
    <p class="text-sm text-ink">Body text in the system face, with <code class="text-accent-text">ids_and_code</code> in JetBrains Mono.</p>
  </section>
</template>
