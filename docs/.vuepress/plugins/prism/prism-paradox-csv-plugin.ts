// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import Prism from 'prismjs'
import { registerParadoxCsv } from '../../highlighters/prism/prism-paradox-csv.js'
import { PluginObject } from "vuepress";

/**
 * VuePress Prism plugin for CWT.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerCwt } and call registerCwt(Prism)
 *
 * References:
 * - https://github.com/PrismJS/prism
 * - https://prismjs.com
 * - https://prismjs.com/extending#language-definitions
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-csv
 * - https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/docs/.vuepress/highlighters/prism/prism-paradox-csv.js
 *
 * @author windea
 */
export default function prismParadoxCsvPlugin(): PluginObject {
  return {
    name: 'prism-paradox-csv',
    extendsMarkdown(/* md */) {
      // Register Paradox CSV language for SSR highlighting
      registerParadoxCsv(Prism)
    },
  }
}
