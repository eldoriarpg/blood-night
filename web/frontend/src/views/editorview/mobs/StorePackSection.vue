/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SectionLabel from '@/components/typography/SectionLabel.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import StoreOfferRow from '~/views/editorview/mobs/StoreOfferRow.vue'
import type {Offer, StorePack} from '~/api/generated/payload'
import {localized, offerState} from '~/util/session/storeOffers'
import {section} from '~/util/session/sections'

/**
 * One pack of the store: its name and description, a button installing every mob of it not installed yet, and its mobs.
 */
const props = defineProps<{
  pack: StorePack
  offers: readonly Offer[]
}>()

const emit = defineEmits<{
  install: [offers: readonly Offer[]]
  update: [offer: Offer]
}>()

const session = useEditorSession()
const locks = useEditorLocks()
const {t, locale} = useI18n()
const available = computed(() => {
  const payload = session.payload.value
  return payload ? props.offers.filter(offer => offerState(offer, payload) === 'available' && locks.mayChange(section.mob(offer.id))) : []
})
</script>

<template>
  <section class="flex flex-col gap-2">
    <div class="flex flex-wrap items-center justify-between gap-2">
      <div>
        <SectionLabel>{{ localized(pack.names, locale, pack.id) }}</SectionLabel>
        <MutedText size="sm" tag="p">{{ localized(pack.descriptions, locale, '') }}</MutedText>
      </div>
      <SecondaryButton v-if="available.length > 0" compact icon="download" @click="emit('install', available)">{{ t('editor.store.installPack', {count: available.length}) }}</SecondaryButton>
    </div>
    <StoreOfferRow v-for="offer in offers" :key="offer.id" :offer="offer" @install="emit('install', [offer])" @update="emit('update', offer)"/>
  </section>
</template>
