/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import Modal from '@/components/feedback/Modal.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import StorePackSection from '~/views/editorview/mobs/StorePackSection.vue'
import type {Offer} from '~/api/generated/payload'
import {installOffer, installOffers, updateOffer} from '~/util/session/storeOffers'

/**
 * The store: its packs with their mobs, to install one mob or a whole pack, and to update installed ones. Updates keep
 * the particle effects a mob wears unless the switch takes the store's as well.
 */
const open = defineModel<boolean>({required: true})

const emit = defineEmits<{
  installed: [id: string]
}>()

const session = useEditorSession()
const changes = useEditorChanges()
const {t} = useI18n()
const store = computed(() => session.payload.value?.store)
const particles = ref(false)

function offersOf(pack: string): Offer[] {
  return store.value?.offers.filter(offer => offer.pack === pack) ?? []
}

async function install(offers: readonly Offer[]): Promise<void> {
  const payload = session.payload.value
  if (!payload || offers.length === 0) return
  const operations = offers.length === 1 && offers[0] ? installOffer(offers[0], payload) : installOffers(offers, payload)
  if (await changes.apply(operations) && offers[0]) emit('installed', offers[0].id)
}

function update(offer: Offer): void {
  void changes.apply(updateOffer(offer, session.payload.value ?? undefined, particles.value))
}
</script>

<template>
  <Modal v-model="open" size="xl">
    <div class="flex flex-col gap-4">
      <SubHeader>{{ t('editor.store.title') }}</SubHeader>
      <ToggleSetting v-model="particles" :hint="t('editor.store.particlesHint')" :label="t('editor.store.particles')"/>
      <StorePackSection
          v-for="pack in store?.packs ?? []"
          :key="pack.id"
          :offers="offersOf(pack.id)"
          :pack="pack"
          :particles="particles"
          @install="install"
          @update="update"
      />
      <EmptyState v-if="(store?.packs.length ?? 0) === 0" :message="t('editor.store.empty')"/>
    </div>
  </Modal>
</template>
