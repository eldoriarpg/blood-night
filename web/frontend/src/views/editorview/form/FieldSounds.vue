/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import FieldLabel from '@/components/typography/FieldLabel.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import RangeSlider from '@/components/input/range/RangeSlider.vue'
import SoundPicker from '~/views/editorview/form/SoundPicker.vue'
import type {SoundEntry} from '~/api/generated/payload'
import {randomIn, rangeOf} from '~/util/sounds/soundRanges'

/**
 * A list of sounds, each picked by ear with its pitch and volume as ranges: every time the server plays it, it picks
 * a pitch and a volume from them. The play button of an entry does the same, so what it plays is what players may
 * hear. A change is saved when a sound is picked or a slider let go.
 */
const props = defineProps<{
  label: string
  value: readonly SoundEntry[] | undefined
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: SoundEntry[]]
}>()

const {t} = useI18n()
const sounds = useSoundAssets()
const NEW_SOUND = 'minecraft:entity.lightning_bolt.thunder'

const drafts = ref<{sound: string, pitch: [number, number], volume: [number, number]}[]>([])

watch(() => props.value, value => {
  drafts.value = (value ?? []).map(entry => ({
    sound: entry.sound ?? NEW_SOUND,
    pitch: rangeOf(entry.pitch),
    volume: rangeOf(entry.volume),
  }))
}, {immediate: true, deep: true})

const pitchMarks = computed(() => [
  {value: 0.5, label: t('editor.sounds.deeper')},
  {value: 1, label: t('editor.sounds.normal')},
  {value: 2, label: t('editor.sounds.higher')},
])
const volumeMarks = computed(() => [
  {value: 0, label: t('editor.sounds.quiet')},
  {value: 1, label: t('editor.sounds.loud')},
])

function save(): void {
  emit('save', drafts.value.map(entry => ({
    sound: entry.sound,
    pitch: {min: entry.pitch[0], max: entry.pitch[1]},
    volume: {min: entry.volume[0], max: entry.volume[1]},
  })))
}

function pick(index: number, sound: string): void {
  const entry = drafts.value[index]
  if (!entry) return
  entry.sound = sound
  save()
}

function add(): void {
  drafts.value.push({sound: NEW_SOUND, pitch: [1, 1], volume: [1, 1]})
  save()
}

function remove(index: number): void {
  drafts.value.splice(index, 1)
  save()
}

function play(index: number): void {
  const entry = drafts.value[index]
  if (entry) sounds.play(entry.sound, randomIn(entry.pitch), randomIn(entry.volume))
}

const percent = (value: number) => `${Math.round(value * 100)}%`
const times = (value: number) => `${value.toFixed(2)}x`
</script>

<template>
  <fieldset class="flex flex-col gap-2">
    <legend><FieldLabel>{{ label }}</FieldLabel></legend>
    <div v-for="(entry, index) in drafts" :key="index" class="flex flex-col gap-3 rounded-panel border border-line-soft p-3">
      <div class="flex items-center gap-1">
        <SoundPicker
            :label="t('editor.sounds.sound', {number: index + 1})"
            :model-value="entry.sound"
            :read-only="readOnly"
            class="min-w-0 flex-1"
            @update:model-value="pick(index, $event)"
        />
        <MutedIconButton v-if="!readOnly" :label="t('editor.form.removeEntry', {number: index + 1})" hover="error" icon="xmark" @click="remove(index)"/>
      </div>
      <div class="grid gap-4 sm:grid-cols-2">
        <div class="flex flex-col gap-1">
          <span class="text-sm">{{ t('editor.sounds.pitch') }}</span>
          <RangeSlider
              v-model="entry.pitch"
              :disabled="readOnly"
              :format="times"
              :from-label="t('editor.sounds.pitchFrom')"
              :marks="pitchMarks"
              :max="2"
              :min="0.5"
              :step="0.05"
              :to-label="t('editor.sounds.pitchTo')"
              @commit="save"
          />
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-sm">{{ t('editor.sounds.volume') }}</span>
          <RangeSlider
              v-model="entry.volume"
              :disabled="readOnly"
              :format="percent"
              :from-label="t('editor.sounds.volumeFrom')"
              :marks="volumeMarks"
              :max="1"
              :min="0"
              :step="0.05"
              :to-label="t('editor.sounds.volumeTo')"
              @commit="save"
          />
        </div>
      </div>
      <div>
        <SecondaryButton :disabled="!sounds.playable(entry.sound)" compact icon="play" @click="play(index)">{{ t('editor.sounds.try') }}</SecondaryButton>
      </div>
    </div>
    <div v-if="!readOnly"><SecondaryButton compact icon="plus" @click="add">{{ t('editor.sounds.add') }}</SecondaryButton></div>
  </fieldset>
</template>
