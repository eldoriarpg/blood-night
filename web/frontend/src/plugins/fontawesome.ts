/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {config, library} from '@fortawesome/fontawesome-svg-core'
import '@fortawesome/fontawesome-svg-core/styles.css'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {
    faCheck, faChevronDown, faCircleCheck, faCircleInfo, faCircleQuestion, faCopy, faDiagramProject, faEllipsisVertical, faExpand, faKeyboard, faPlay,
    faMagnifyingGlass, faMinus, faPen, faPlus, faSliders, faSpinner, faTrash, faTriangleExclamation, faXmark,
    faArrowLeft, faArrowRight, faArrowsRotate, faDownload, faStore, faSun, faMoon, faLanguage, faBookOpen,
} from '@fortawesome/free-solid-svg-icons'

/**
 * Registers FontAwesome icon by icon, which is what lets the bundler leave out what nobody uses.
 * An icon a file names has to be added here; the icon linter fails on any that is not.
 */
config.autoAddCss = false

library.add(
    faCheck, faChevronDown, faCircleCheck, faCircleInfo, faCircleQuestion, faCopy, faDiagramProject, faEllipsisVertical, faExpand, faKeyboard, faPlay,
    faMagnifyingGlass, faMinus, faPen, faPlus, faSliders, faSpinner, faTrash, faTriangleExclamation, faXmark,
    faArrowLeft, faArrowRight, faArrowsRotate, faDownload, faStore, faSun, faMoon, faLanguage, faBookOpen,
)

export default defineNuxtPlugin((nuxtApp) => {
    nuxtApp.vueApp.component('font-awesome-icon', FontAwesomeIcon)
})
