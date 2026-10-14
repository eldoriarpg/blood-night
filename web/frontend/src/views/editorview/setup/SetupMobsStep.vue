/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import CheckboxInput from '@/components/input/toggle/CheckboxInput.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import type {Offer, StorePack} from '~/api/generated/payload'
import {localized} from '~/util/session/storeOffers'

/** The store's packs with a box per mob; mobs whose id is already taken are left out. */
const picked = defineModel<string[]>({required: true})

const props = defineProps<{
  packs: readonly StorePack[]
  offers: readonly Offer[]
  taken: ReadonlySet<string>
}>()

const {locale} = useI18n()

function offersOf(pack: string): Offer[] {
  return props.offers.filter(offer => offer.pack === pack && !props.taken.has(offer.id))
}

function toggle(id: string, on: boolean): void {
  picked.value = on ? [...picked.value, id] : picked.value.filter(entry => entry !== id)
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <section v-for="pack in packs" :key="pack.id" class="flex flex-col gap-2">
      <SectionLabel>{{ localized(pack.names, locale, pack.id) }}</SectionLabel>
      <label v-for="offer in offersOf(pack.id)" :key="offer.id" class="flex items-start gap-2">
        <CheckboxInput :model-value="picked.includes(offer.id)" @update:model-value="toggle(offer.id, $event)"/>
        <span>
          <span class="font-medium">{{ localized(offer.names, locale, offer.id) }}</span>
          <MutedText size="sm" tag="p">{{ localized(offer.descriptions, locale, '') }}</MutedText>
        </span>
      </label>
    </section>
  </div>
</template>
