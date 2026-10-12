/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref} from 'vue'
import SectionHeader from '@/components/typography/SectionHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'
import TabBar from '@/components/navigation/TabBar.vue'

/** The tab bar, with the tab it has selected. */
const TABS = [
  {key: 'mobs', label: 'Mobs'},
  {key: 'macros', label: 'Macros'},
  {key: 'settings', label: 'Settings'},
]

const tab = ref('mobs')
</script>

<template>
  <section class="space-y-4">
    <SectionHeader>Navigation</SectionHeader>
    <TabBar v-model="tab" :tabs="TABS"/>
    <MutedText size="sm" tag="p">Selected: {{ tab }}</MutedText>
  </section>
</template>
