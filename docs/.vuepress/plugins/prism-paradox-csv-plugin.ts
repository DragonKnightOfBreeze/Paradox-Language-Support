// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import Prism from 'prismjs'
import { registerParadoxCsv } from '../highlighters/prism-paradox-csv.js'
import { PluginObject } from "vuepress";

/**
 * VuePress Prism plugin for CWT.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerCwt } and call registerCwt(Prism)
 *
 * @see https://github.com/PrismJS/prism
 * @see https://prismjs.com
 * @see https://prismjs.com/extending#language-definitions
 * @see https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-csv
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
