/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import SectionHeader from '@/components/typography/SectionHeader.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import ErrorButton from '@/components/button/ErrorButton.vue'
import IconButton from '@/components/button/IconButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import ActionsMenu from '@/components/button/ActionsMenu.vue'
import DropdownMenuItem from '@/components/button/DropdownMenuItem.vue'

/** The button family: the colour variants, compact and disabled forms, icon buttons and the row menu. */
</script>

<template>
  <section class="space-y-4">
    <SectionHeader>Buttons</SectionHeader>
    <ButtonRow>
      <PrimaryButton :icon="['fas', 'plus']">Primary</PrimaryButton>
      <SecondaryButton>Secondary</SecondaryButton>
      <ErrorButton :icon="['fas', 'trash']">Error</ErrorButton>
    </ButtonRow>
    <ButtonRow>
      <PrimaryButton compact>Compact</PrimaryButton>
      <SecondaryButton compact>Compact</SecondaryButton>
      <ErrorButton compact>Compact</ErrorButton>
    </ButtonRow>
    <ButtonRow>
      <PrimaryButton disabled>Disabled</PrimaryButton>
      <SecondaryButton disabled>Disabled</SecondaryButton>
      <ErrorButton disabled>Disabled</ErrorButton>
    </ButtonRow>
    <ButtonRow pair>
      <SecondaryButton data-cancel>Cancel</SecondaryButton>
      <PrimaryButton>Save</PrimaryButton>
    </ButtonRow>
  </section>

  <section class="space-y-4">
    <SectionHeader>Icon buttons and menus</SectionHeader>
    <div class="flex flex-wrap items-center gap-2">
      <IconButton :icon="['fas', 'pen']" label="Edit"/>
      <IconButton :icon="['fas', 'pen']" label="Edit" disabled/>
      <MutedIconButton :icon="['fas', 'pen']" label="Edit" hover="primary"/>
      <MutedIconButton :icon="['fas', 'xmark']" label="Remove" hover="error"/>
      <MutedIconButton :icon="['fas', 'check']" label="Confirm" hover="text"/>
      <ActionsMenu label="Node actions">
        <DropdownMenuItem :icon="['fas', 'pen']">Rename</DropdownMenuItem>
        <DropdownMenuItem :icon="['fas', 'plus']" disabled>Duplicate</DropdownMenuItem>
        <DropdownMenuItem :icon="['fas', 'trash']" destructive>Delete</DropdownMenuItem>
      </ActionsMenu>
    </div>
  </section>
</template>
