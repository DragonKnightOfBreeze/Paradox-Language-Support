// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import Prism from 'prismjs'
import { registerCwt } from '../highlighters/prism-cwt.js'

/**
 * VuePress Prism plugin for CWT.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerCwt } and call registerCwt(Prism)
 *
 * @see https://github.com/PrismJS/prism
 * @see https://prismjs.com
 * @see https://windea.icu/Paradox-Language-Support/ref-syntax.html#cwt
 *
 * @author windea
 */
export default function prismCwtPlugin() {
  return {
    name: 'prism-cwt',
    extendsMarkdown() {
      // Register CWT language for SSR highlighting
      registerCwt(Prism)
    },
  }
}
