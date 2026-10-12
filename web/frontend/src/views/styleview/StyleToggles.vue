/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref} from 'vue'
import FieldLabel from '@/components/typography/FieldLabel.vue'
import CheckboxInput from '@/components/input/toggle/CheckboxInput.vue'
import ToggleSwitch from '@/components/input/toggle/ToggleSwitch.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'

/** The on and off controls: a checkbox, the switch between two options, and a setting with its switch. */
const checked = ref(true)
const mixed = ref(false)
const side = ref('graph')
const enabled = ref(true)
const locked = ref(false)
</script>

<template>
  <div class="grid gap-4 sm:grid-cols-2">
    <div class="space-y-2">
      <FieldLabel inline><CheckboxInput v-model="checked"/> Checked</FieldLabel>
      <FieldLabel inline><CheckboxInput v-model="mixed" indeterminate/> Partly selected</FieldLabel>
      <ToggleSwitch v-model="side" option-a="graph" option-b="list" label-a="Graph" label-b="List"/>
    </div>
    <div class="space-y-3">
      <ToggleSetting v-model="enabled" label="Announce blood nights" hint="Tells every player when one begins."/>
      <ToggleSetting v-model="locked" label="Disabled setting" disabled/>
    </div>
  </div>
</template>
