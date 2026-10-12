/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref} from 'vue'
import SectionHeader from '@/components/typography/SectionHeader.vue'
import FormLabel from '@/components/input/FormLabel.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import BaseInput from '@/components/input/BaseInput.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import DecimalInput from '@/components/input/number/DecimalInput.vue'
import SelectInput from '@/components/input/select/SelectInput.vue'
import StyleToggles from '@/views/styleview/StyleToggles.vue'

/** The fields: text, search, numbers, a select and the borderless variant, each with its label. */
const text = ref('Blood moon')
const search = ref('')
const count = ref(3)
const chance = ref(0.25)
const kind = ref('event')
const inline = ref('Edited in place')
</script>

<template>
  <section class="space-y-4">
    <SectionHeader>Inputs</SectionHeader>
    <div class="grid gap-4 sm:grid-cols-2">
      <LabelledField v-slot="{id}" label="Text" help="A line under the field says what it is for.">
        <TextInput :id="id" v-model="text" placeholder="Name"/>
      </LabelledField>
      <LabelledField v-slot="{id}" label="Search">
        <SearchInput :id="id" v-model="search" placeholder="Search nodes"/>
      </LabelledField>
      <LabelledField v-slot="{id}" label="Whole number">
        <NumberInput :id="id" v-model="count"/>
      </LabelledField>
      <LabelledField v-slot="{id}" label="Decimal" hint>
        <DecimalInput :id="id" v-model="chance"/>
      </LabelledField>
      <LabelledField v-slot="{id}" label="Select">
        <SelectInput :id="id" v-model="kind" class="w-full">
          <option value="event">Event</option>
          <option value="action">Action</option>
          <option value="value">Value</option>
        </SelectInput>
      </LabelledField>
      <div>
        <FormLabel for="style-borderless">Borderless</FormLabel>
        <BaseInput id="style-borderless" v-model="inline" borderless/>
      </div>
    </div>
    <StyleToggles/>
  </section>
</template>
