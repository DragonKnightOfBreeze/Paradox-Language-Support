// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import Prism from 'prismjs'
import { registerParadoxLocalisation } from '../../highlighters/prism/prism-paradox-localisation.js'
import { PluginObject } from "vuepress";

/**
 * VuePress Prism plugin for Paradox Localisation.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerParadoxLocalisation } and call registerParadoxLocalisation(Prism)
 *
 * References:
 * - https://github.com/PrismJS/prism
 * - https://prismjs.com
 * - https://prismjs.com/extending#language-definitions
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#localisation
 * - https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/docs/.vuepress/highlighters/prism/prism-paradox-localisation.js
 *
 * @author windea
 */
export default function prismParadoxLocalisationPlugin(): PluginObject {
  return {
    name: 'prism-paradox-localisation',
    extendsMarkdown() {
      // Register Paradox Localisation language for SSR highlighting
      registerParadoxLocalisation(Prism)
    },
  }
}
