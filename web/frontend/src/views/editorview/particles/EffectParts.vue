/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SectionHeader from '@/components/typography/SectionHeader.vue'
import EmitterRow from '~/views/editorview/particles/EmitterRow.vue'
import PieceRow from '~/views/editorview/particles/PieceRow.vue'
import type {Emitter, ParticleEffect, Piece} from '~/api/generated/payload'
import type {PatchOperation} from '~/util/patch/jsonPatch'
import {pointer} from '~/util/patch/jsonPointer'
import {LIMITS, newEmitter, newPiece} from '~/util/particles/particleEffects'

/**
 * An effect's emitters and display pieces: each in its own row, added, changed and removed one at a time.
 */
const props = defineProps<{
  id: string
  effect: ParticleEffect
  readOnly: boolean
}>()

const emit = defineEmits<{
  apply: [operations: PatchOperation[]]
}>()

const {t} = useI18n()
const emitters = computed(() => props.effect.emitters ?? [])
const pieces = computed(() => props.effect.pieces ?? [])

function path(list: 'emitters' | 'pieces', ...keys: (string | number)[]): string {
  return pointer('particleEffects', props.id, list, ...keys.map(String))
}

function add(list: 'emitters' | 'pieces', value: Emitter | Piece): void {
  const present = list === 'emitters' ? props.effect.emitters : props.effect.pieces
  const head: PatchOperation[] = present ? [] : [{op: 'add', path: path(list), value: []}]
  emit('apply', [...head, {op: 'add', path: path(list, '-'), value}])
}

function save(list: 'emitters' | 'pieces', index: number, value: Emitter | Piece): void {
  emit('apply', [{op: 'replace', path: path(list, index), value}])
}

function remove(list: 'emitters' | 'pieces', index: number): void {
  emit('apply', [{op: 'remove', path: path(list, index)}])
}
</script>

<template>
  <section :aria-label="t('editor.particles.emitters')" class="flex flex-col gap-3">
    <SectionHeader>{{ t('editor.particles.emitters') }}</SectionHeader>
    <MutedText v-if="emitters.length === 0" size="sm" tag="p">{{ t('editor.particles.noEmitters') }}</MutedText>
    <ol class="flex flex-col gap-3">
      <EmitterRow
          v-for="(emitter, index) in emitters"
          :key="index"
          :emitter="emitter"
          :index="index"
          :pieces="pieces.length"
          :read-only="readOnly"
          @remove="remove('emitters', index)"
          @save="value => save('emitters', index, value)"
      />
    </ol>
    <div v-if="!readOnly && emitters.length < LIMITS.emitters">
      <SecondaryButton compact icon="plus" @click="add('emitters', newEmitter())">{{ t('editor.particles.addEmitter') }}</SecondaryButton>
    </div>
  </section>
  <section :aria-label="t('editor.particles.pieces')" class="flex flex-col gap-3">
    <SectionHeader>{{ t('editor.particles.pieces') }}</SectionHeader>
    <MutedText size="sm" tag="p">{{ t('editor.particles.piecesIntro') }}</MutedText>
    <ol class="flex flex-col gap-3">
      <PieceRow
          v-for="(piece, index) in pieces"
          :key="index"
          :index="index"
          :piece="piece"
          :read-only="readOnly"
          @remove="remove('pieces', index)"
          @save="value => save('pieces', index, value)"
      />
    </ol>
    <div v-if="!readOnly && pieces.length < LIMITS.pieces">
      <SecondaryButton compact icon="plus" @click="add('pieces', newPiece())">{{ t('editor.particles.addPiece') }}</SecondaryButton>
    </div>
  </section>
</template>
