/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import Modal from '@/components/feedback/Modal.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'

/**
 * Asks how to add a mob: a new one from scratch, or one from the store.
 */
const open = defineModel<boolean>({required: true})

const emit = defineEmits<{
  choose: [way: 'new' | 'store']
}>()

const {t} = useI18n()

function choose(way: 'new' | 'store'): void {
  open.value = false
  emit('choose', way)
}
</script>

<template>
  <Modal v-model="open" size="sm">
    <div class="flex flex-col gap-3">
      <SubHeader>{{ t('editor.mobs.addTitle') }}</SubHeader>
      <SecondaryButton icon="plus" @click="choose('new')">{{ t('editor.mobs.addNew') }}</SecondaryButton>
      <MutedText size="sm" tag="p">{{ t('editor.mobs.addNewHint') }}</MutedText>
      <SecondaryButton icon="store" @click="choose('store')">{{ t('editor.mobs.addStore') }}</SecondaryButton>
      <MutedText size="sm" tag="p">{{ t('editor.mobs.addStoreHint') }}</MutedText>
    </div>
  </Modal>
</template>
