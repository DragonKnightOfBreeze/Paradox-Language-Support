// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import Prism from 'prismjs'
import { registerParadoxScript } from '../../highlighters/prism/prism-paradox-script.js'
import { PluginObject } from "vuepress";

/**
 * VuePress Prism plugin for Paradox Script.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerParadoxScript } and call registerParadoxScript(Prism)
 *
 * References:
 * - https://github.com/PrismJS/prism
 * - https://prismjs.com
 * - https://prismjs.com/extending#language-definitions
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-script
 * - https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/docs/.vuepress/highlighters/prism/prism-paradox-script.js
 *
 * @author windea
 */
export default function prismParadoxScriptPlugin(): PluginObject {
  return {
    name: 'prism-paradox-script',
    extendsMarkdown() {
      // Register Paradox Script language for SSR highlighting
      registerParadoxScript(Prism)
    },
  }
}
