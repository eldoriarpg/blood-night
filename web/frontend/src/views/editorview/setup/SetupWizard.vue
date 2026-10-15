/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {worldLabel} from '~/util/settings/worldNames'
import ButtonRow from '@/components/button/ButtonRow.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import MutedText from '@/components/typography/MutedText.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import SetupAtmosphereStep from '~/views/editorview/setup/SetupAtmosphereStep.vue'
import SetupLegacyStep from '~/views/editorview/setup/SetupLegacyStep.vue'
import SetupMobsStep from '~/views/editorview/setup/SetupMobsStep.vue'
import SetupPresetStep from '~/views/editorview/setup/SetupPresetStep.vue'
import SetupSummaryStep from '~/views/editorview/setup/SetupSummaryStep.vue'
import SetupWorldsStep from '~/views/editorview/setup/SetupWorldsStep.vue'
import type {EditorPayload} from '~/api/generated/payload'
import {chosenOffers, initialChoices, setupOperations, type Difficulty, type Frequency} from '~/util/session/setupWizard'
import {showToast} from '~/util/toast'

/**
 * The first-run setup: the old installation when there is one, worlds, how often, how hard, atmosphere, mobs from the
 * store, and a summary. Finishing writes everything as one change and marks the session as done; it is applied on the
 * server like any session. Leaving shows the full editor.
 */
const props = defineProps<{
  payload: EditorPayload
}>()

const emit = defineEmits<{
  leave: []
}>()

const session = useEditorSession()
const changes = useEditorChanges()
const {t} = useI18n()
const FREQUENCIES: readonly Frequency[] = ['keep', 'rarely', 'sometimes', 'often', 'moon']
const DIFFICULTIES: readonly Difficulty[] = ['keep', 'gentle', 'normal', 'brutal']

const choices = ref(initialChoices(props.payload))
const index = ref(0)
const finishing = ref(false)
const finished = ref(false)
const steps = computed(() => [...(props.payload.legacy ? ['legacy'] : []), 'worlds', 'frequency', 'difficulty', 'atmosphere', 'mobs', 'summary'])
const step = computed(() => steps.value[index.value] ?? 'summary')
const last = computed(() => index.value === steps.value.length - 1)
const names = computed(() => Object.fromEntries(Object.entries(props.payload.settings.worlds).map(([uid, world]) => [uid, worldLabel(world)])))
const offers = computed(() => chosenOffers(props.payload, choices.value))
const taken = computed(() => new Set(Object.keys((choices.value.takeOver ? props.payload.legacy?.mobs : props.payload.mobs) ?? {})))

function takeOver(value: boolean): void {
  choices.value = initialChoices(props.payload, value)
}

async function finish(): Promise<void> {
  finishing.value = true
  try {
    if (!await changes.apply(setupOperations(props.payload, choices.value))) return
    await session.api().submit()
    finished.value = true
  } catch (error) {
    showToast((error as Error).message, 'error')
  } finally {
    finishing.value = false
  }
}
</script>

<template>
  <div class="mx-auto flex w-full max-w-2xl flex-1 flex-col gap-4 overflow-y-auto px-4 py-6">
    <template v-if="finished">
      <SubHeader>{{ t('editor.setup.done.title') }}</SubHeader>
      <MutedText tag="p">{{ t('editor.setup.done.hint') }}</MutedText>
      <div>
        <SecondaryButton icon="pen" @click="emit('leave')">{{ t('editor.setup.leave') }}</SecondaryButton>
      </div>
    </template>
    <template v-else>
      <MutedText size="sm" tag="p">{{ t('editor.setup.progress', {step: index + 1, steps: steps.length}) }}</MutedText>
      <SubHeader>{{ t(`editor.setup.${step}.title`) }}</SubHeader>
      <SetupLegacyStep v-if="step === 'legacy'" :model-value="choices.takeOver" :notes="payload.legacy?.notes ?? []" @update:model-value="takeOver"/>
      <SetupWorldsStep v-else-if="step === 'worlds'" v-model="choices" :names="names"/>
      <SetupPresetStep v-else-if="step === 'frequency'" v-model="choices.frequency" :presets="FREQUENCIES" step="frequency"/>
      <SetupPresetStep v-else-if="step === 'difficulty'" v-model="choices.difficulty" :presets="DIFFICULTIES" step="difficulty"/>
      <SetupAtmosphereStep v-else-if="step === 'atmosphere'" v-model="choices"/>
      <SetupMobsStep v-else-if="step === 'mobs'" v-model="choices.mobs" :offers="payload.store?.offers ?? []" :packs="payload.store?.packs ?? []" :taken="taken"/>
      <SetupSummaryStep v-else :choices="choices" :mobs="offers.length" :names="names"/>
      <ButtonRow align="between">
        <SecondaryButton icon="pen" @click="emit('leave')">{{ t('editor.setup.leave') }}</SecondaryButton>
        <SecondaryButton v-if="index > 0" icon="arrow-left" @click="index--">{{ t('editor.setup.back') }}</SecondaryButton>
        <PrimaryButton v-if="last" :disabled="finishing" icon="check" @click="finish">{{ t('editor.setup.finish') }}</PrimaryButton>
        <PrimaryButton v-else icon="arrow-right" @click="index++">{{ t('editor.setup.next') }}</PrimaryButton>
      </ButtonRow>
    </template>
  </div>
</template>
