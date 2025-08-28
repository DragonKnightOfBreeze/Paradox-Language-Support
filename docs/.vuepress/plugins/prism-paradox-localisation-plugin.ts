// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import Prism from 'prismjs'
import { registerParadoxLocalisation } from '../highlighters/prism-paradox-localisation.js'
import { PluginObject } from "vuepress";

/**
 * uePress Prism plugin for Paradox Localisation.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerParadoxLocalisation } and call registerParadoxLocalisation(Prism)
 *
 * @see https://github.com/PrismJS/prism
 * @see https://prismjs.com
 * @see https://windea.icu/Paradox-Language-Support/ref-syntax.html#localisation
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
