/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import type {SetupChoices} from '~/util/session/setupWizard'

/** The look and sound of blood nights, and whether players are offered the resource pack. */
const choices = defineModel<SetupChoices>({required: true})

const {t} = useI18n()
</script>

<template>
  <div class="flex flex-col gap-3">
    <ToggleSetting v-model="choices.effects" :hint="t('editor.setup.atmosphere.effectsHint')" :label="t('editor.setup.atmosphere.effects')"/>
    <ToggleSetting v-model="choices.heartbeat" :hint="t('editor.setup.atmosphere.heartbeatHint')" :label="t('editor.setup.atmosphere.heartbeat')"/>
    <ToggleSetting v-model="choices.resourcePack" :hint="t('editor.setup.atmosphere.packHint')" :label="t('editor.setup.atmosphere.pack')"/>
  </div>
</template>
