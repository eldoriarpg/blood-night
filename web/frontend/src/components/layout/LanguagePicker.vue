/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import AppIcon from '@/components/display/AppIcon.vue'
import SelectInput from '@/components/input/select/SelectInput.vue'

/**
 * Picks the editor's language among all it has, each named in its own language, e.g. "Deutsch". The browser
 * remembers the choice.
 */
const {t, locale, locales, setLocale} = useI18n()

const choices = computed(() => locales.value.map(entry => {
  const code = typeof entry === 'string' ? entry : entry.code
  const name = new Intl.DisplayNames([code], {type: 'language'}).of(code) ?? code
  return {code, name: name.charAt(0).toLocaleUpperCase(code) + name.slice(1)}
}))

const picked = computed({
  get: () => locale.value,
  set: code => {
    if (typeof code === 'string') void setLocale(code as typeof locale.value)
  },
})
</script>

<template>
  <label class="flex items-center gap-1.5 text-ink-faint">
    <AppIcon aria-hidden="true" icon="language"/>
    <span class="sr-only">{{ t('footer.language') }}</span>
    <SelectInput v-model="picked" compact>
      <option v-for="choice in choices" :key="choice.code" :value="choice.code">{{ choice.name }}</option>
    </SelectInput>
  </label>
</template>
