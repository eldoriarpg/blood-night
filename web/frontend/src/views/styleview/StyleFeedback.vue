/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import SectionHeader from '@/components/typography/SectionHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'
import Alert from '@/components/feedback/Alert.vue'
import Spinner from '@/components/feedback/Spinner.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import {showToast} from '@/util/toast'

/** The banners, the spinner, the empty state and the toasts, which a button here raises. */
</script>

<template>
  <section class="space-y-4">
    <SectionHeader>Alerts</SectionHeader>
    <div class="space-y-3">
      <Alert variant="info">The session was opened by Steve.</Alert>
      <Alert variant="success">Saved as revision 12.</Alert>
      <Alert variant="held">Alex is editing the mob rules right now.</Alert>
      <Alert variant="error">The macro refers to a node that no longer exists.</Alert>
    </div>
  </section>

  <section class="space-y-4">
    <SectionHeader>Spinner and empty state</SectionHeader>
    <div class="flex flex-wrap items-center gap-6">
      <Spinner size="sm"/>
      <Spinner size="md"/>
      <Spinner size="lg"/>
      <MutedText size="sm">Small, medium, large</MutedText>
    </div>
    <EmptyState message="No macros yet."/>
    <EmptyState compact>Nothing selected.</EmptyState>
  </section>

  <section class="space-y-4">
    <SectionHeader>Toasts</SectionHeader>
    <ButtonRow>
      <SecondaryButton @click="showToast('Settings loaded.', 'info')">Info toast</SecondaryButton>
      <SecondaryButton @click="showToast('Saved.', 'success')">Success toast</SecondaryButton>
      <SecondaryButton @click="showToast('The server refused the change.', 'error')">Error toast</SecondaryButton>
      <SecondaryButton @click="showToast('Node deleted.', 'info', 8000, {label: 'Undo', run: () => showToast('Restored.', 'success')})">
        Toast with action
      </SecondaryButton>
    </ButtonRow>
  </section>
</template>
