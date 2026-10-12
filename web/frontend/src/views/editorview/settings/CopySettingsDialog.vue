/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import InfoBadge from '@/components/badge/InfoBadge.vue'
import Modal from '@/components/feedback/Modal.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import MutedText from '@/components/typography/MutedText.vue'
import BareButton from '@/components/button/BareButton.vue'
import CheckboxInput from '@/components/input/toggle/CheckboxInput.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import {copyOperations} from '~/util/settings/copySettings'
import {section} from '~/util/session/sections'
import {WORLD_SECTIONS} from '~/util/settings/settingsSections'

/**
 * Copies sections of one world's settings to other worlds, in full or in part. Worlds someone else holds cannot be
 * picked as targets; the copy is one change, so it lands as one revision that can be restored.
 */
const open = defineModel<boolean>({required: true})

const props = defineProps<{
  from: string
  sections: readonly string[]
}>()

const session = useEditorSession()
const changes = useEditorChanges()
const locks = useEditorLocks()
const {t} = useI18n()
const source = ref(props.from)
const targets = ref<string[]>([])
const chosen = ref<string[]>([])

watch(open, now => {
  if (!now) return
  source.value = props.from
  targets.value = []
  chosen.value = [...props.sections]
})

const worlds = computed(() => Object.entries(session.payload.value?.settings?.worlds ?? {})
  .map(([uid, world]) => ({uid, name: world.name, heldBy: locks.heldByOther(section.world(uid))}))
  .sort((a, b) => a.name.localeCompare(b.name)))

function toggle(list: string[], value: string, on: boolean): string[] {
  return on ? [...new Set([...list, value])] : list.filter(entry => entry !== value)
}

async function copy(): Promise<void> {
  const settings = session.payload.value?.settings
  if (!settings) return
  if (await changes.apply(copyOperations(settings, source.value, targets.value, chosen.value))) open.value = false
}
</script>

<template>
  <Modal v-model="open" size="lg">
    <div class="space-y-5">
      <SubHeader>{{ t('editor.copy.title') }}</SubHeader>
      <div class="grid gap-5 sm:grid-cols-2">
        <div class="flex flex-col gap-1">
          <SectionLabel>{{ t('editor.copy.from') }}</SectionLabel>
          <BareButton
              v-for="world in worlds"
              :key="world.uid"
              :class="world.uid === source ? 'bg-selected' : 'hover:bg-raised'"
              class="min-h-10 rounded-control px-2 text-left font-mono text-sm"
              @click="source = world.uid; targets = targets.filter(target => target !== world.uid)"
          >{{ world.name }}</BareButton>
        </div>
        <fieldset class="flex flex-col gap-1">
          <legend><SectionLabel>{{ t('editor.copy.to') }}</SectionLabel></legend>
          <label v-for="world in worlds.filter(candidate => candidate.uid !== source)" :key="world.uid" class="flex min-h-10 items-center gap-2 px-2 text-sm">
            <CheckboxInput :disabled="world.heldBy !== null" :model-value="targets.includes(world.uid)" @update:model-value="on => targets = toggle(targets, world.uid, on)"/>
            <span class="flex-1 font-mono">{{ world.name }}</span>
            <InfoBadge v-if="world.heldBy">{{ t('editor.copy.editing', {name: world.heldBy}) }}</InfoBadge>
          </label>
        </fieldset>
      </div>
      <fieldset class="flex flex-col gap-1">
        <div class="flex items-center justify-between">
          <legend><SectionLabel>{{ t('editor.copy.sections') }}</SectionLabel></legend>
          <SecondaryButton compact @click="chosen = chosen.length === WORLD_SECTIONS.length ? [] : WORLD_SECTIONS.map(entry => entry.key)">
            {{ chosen.length === WORLD_SECTIONS.length ? t('editor.copy.clear') : t('editor.copy.all') }}
          </SecondaryButton>
        </div>
        <div class="grid grid-cols-2 gap-1 sm:grid-cols-4">
          <label v-for="entry in WORLD_SECTIONS" :key="entry.key" class="flex min-h-10 items-center gap-2 px-2 text-sm">
            <CheckboxInput :model-value="chosen.includes(entry.key)" @update:model-value="on => chosen = toggle(chosen, entry.key, on)"/>
            {{ t(`editor.settings.sections.${entry.key}`) }}
          </label>
        </div>
      </fieldset>
      <MutedText size="sm" tag="p">{{ t('editor.copy.summary', {sections: chosen.length, worlds: targets.length}) }}</MutedText>
      <ButtonRow align="end" pair>
        <SecondaryButton data-cancel @click="open = false">{{ t('editor.sheet.cancel') }}</SecondaryButton>
        <PrimaryButton :disabled="targets.length === 0 || chosen.length === 0" @click="copy">{{ t('editor.copy.submit') }}</PrimaryButton>
      </ButtonRow>
    </div>
  </Modal>
</template>
