/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import ErrorButton from '@/components/button/ErrorButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import InfoBadge from '@/components/badge/InfoBadge.vue'
import Alert from '@/components/feedback/Alert.vue'
import Modal from '@/components/feedback/Modal.vue'
import MutedText from '@/components/typography/MutedText.vue'
import PageHeader from '@/components/typography/PageHeader.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import EffectBasics from '~/views/editorview/particles/EffectBasics.vue'
import EffectParts from '~/views/editorview/particles/EffectParts.vue'
import EffectPreview from '~/views/editorview/particles/EffectPreview.vue'
import EffectUses from '~/views/editorview/particles/EffectUses.vue'
import type {ParticleEffect} from '~/api/generated/payload'
import type {PatchOperation} from '~/util/patch/jsonPatch'
import {pointer} from '~/util/patch/jsonPointer'
import {section} from '~/util/session/sections'
import {removeEffect, usesOf, type EffectEntry} from '~/util/particles/particleEffects'

/**
 * One particle effect: its basics, a preview, its emitters and pieces, and where it is used. A built-in effect is shown
 * read only, with a button to copy it; deleting an effect also takes it off the mobs that wear it.
 */
const props = defineProps<{
  entry: EffectEntry
}>()

const emit = defineEmits<{
  deleted: []
  copy: [effect: ParticleEffect]
}>()

const session = useEditorSession()
const locks = useEditorLocks()
const changes = useEditorChanges()
const {t} = useI18n()
const confirming = ref(false)

const heldBy = computed(() => props.entry.builtIn ? null : locks.heldByOther(section.particleEffect(props.entry.id)))
const denied = computed(() => !props.entry.builtIn && !locks.mayChange(section.particleEffect(props.entry.id)))
const readOnly = computed(() => props.entry.builtIn || heldBy.value !== null || denied.value)
const uses = computed(() => usesOf(session.payload.value, props.entry.id))

function apply(operations: PatchOperation[]): void {
  if (!readOnly.value) void changes.apply(operations)
}

function save(change: Partial<ParticleEffect>): void {
  apply(Object.entries(change).map(([key, value]) => value === undefined
    ? {op: 'remove', path: pointer('particleEffects', props.entry.id, key)}
    : {op: 'add', path: pointer('particleEffects', props.entry.id, key), value}))
}

function deleteEffect(): void {
  const payload = session.payload.value
  confirming.value = false
  if (!payload) return
  void changes.apply(removeEffect(payload, props.entry.id)).then(applied => {
    if (applied) emit('deleted')
  })
}
</script>

<template>
  <main class="overflow-y-auto px-6 py-5">
    <div class="flex flex-wrap items-center gap-3">
      <PageHeader class="flex-1">{{ entry.effect.name || entry.id }}</PageHeader>
      <InfoBadge v-if="entry.builtIn">{{ t('editor.particles.builtIn') }}</InfoBadge>
      <InfoBadge class="font-mono">{{ entry.id }}</InfoBadge>
    </div>
    <Alert v-if="entry.builtIn" class="mt-3" variant="info">
      {{ t('editor.particles.builtInHint') }}
      <SecondaryButton class="ml-2" compact icon="copy" @click="emit('copy', entry.effect)">{{ t('editor.particles.copy') }}</SecondaryButton>
    </Alert>
    <Alert v-else-if="heldBy" class="mt-3" variant="held">{{ t('editor.particles.heldBy', {name: heldBy}) }}</Alert>
    <Alert v-else-if="denied" class="mt-3" variant="held">{{ t('editor.permission.readOnly') }}</Alert>
    <MutedText class="mt-3 max-w-2xl" size="sm" tag="p">{{ t('editor.particles.intro') }}</MutedText>
    <div class="flex max-w-4xl flex-col gap-5 pt-4">
      <EffectBasics :effect="entry.effect" :read-only="readOnly" @save="save"/>
      <EffectPreview :effect="entry.effect"/>
      <EffectParts :id="entry.id" :effect="entry.effect" :read-only="readOnly" @apply="apply"/>
      <EffectUses :uses="uses"/>
      <div v-if="!readOnly"><ErrorButton compact icon="trash" @click="confirming = true">{{ t('editor.particles.delete') }}</ErrorButton></div>
    </div>
    <Modal v-model="confirming" size="sm">
      <div class="space-y-4">
        <SubHeader>{{ t('editor.particles.confirm', {name: entry.effect.name || entry.id}) }}</SubHeader>
        <MutedText size="sm" tag="p">{{ uses.length > 0 ? t('editor.particles.confirmUsed', {count: uses.length}) : t('editor.sheet.confirmHint') }}</MutedText>
        <ButtonRow align="end" pair>
          <SecondaryButton data-cancel @click="confirming = false">{{ t('editor.sheet.cancel') }}</SecondaryButton>
          <PrimaryButton @click="deleteEffect">{{ t('editor.particles.delete') }}</PrimaryButton>
        </ButtonRow>
      </div>
    </Modal>
  </main>
</template>
