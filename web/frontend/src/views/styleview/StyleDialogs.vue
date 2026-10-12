/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref} from 'vue'
import SectionHeader from '@/components/typography/SectionHeader.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import Modal from '@/components/feedback/Modal.vue'
import SidePanel from '@/components/feedback/SidePanel.vue'
import Popover from '@/components/feedback/Popover.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import LabelledField from '@/components/input/LabelledField.vue'

/** What opens on top of a page: the dialog, the side panel and a popover with a field in it. */
const modalOpen = ref(false)
const panelOpen = ref(false)
const name = ref('')
</script>

<template>
  <section class="space-y-4">
    <SectionHeader>Dialogs and panels</SectionHeader>
    <ButtonRow>
      <SecondaryButton @click="modalOpen = true">Open dialog</SecondaryButton>
      <SecondaryButton @click="panelOpen = true">Open side panel</SecondaryButton>
      <Popover label="Rename" role="dialog" align="start" panel-class="w-64 p-3">
        <template #trigger="{toggle, triggerAttrs}">
          <SecondaryButton v-bind="triggerAttrs" @click="toggle">Open popover</SecondaryButton>
        </template>
        <LabelledField v-slot="{id}" label="Name">
          <TextInput :id="id" v-model="name" placeholder="Macro name"/>
        </LabelledField>
      </Popover>
    </ButtonRow>

    <Modal v-model="modalOpen" size="sm">
      <div class="space-y-4">
        <SubHeader>Delete the macro?</SubHeader>
        <MutedText size="sm" tag="p">Every node using it loses its contents.</MutedText>
        <ButtonRow pair align="end">
          <SecondaryButton data-cancel @click="modalOpen = false">Cancel</SecondaryButton>
          <PrimaryButton @click="modalOpen = false">Delete</PrimaryButton>
        </ButtonRow>
      </div>
    </Modal>

    <SidePanel v-model="panelOpen">
      <div class="space-y-3 p-6">
        <SubHeader>Side panel</SubHeader>
        <MutedText size="sm" tag="p">For a record with more to it than a dialog holds.</MutedText>
        <SecondaryButton @click="panelOpen = false">Close</SecondaryButton>
      </div>
    </SidePanel>
  </section>
</template>
