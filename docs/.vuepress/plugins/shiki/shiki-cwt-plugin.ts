// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiCwt from "../../highlighters/shiki/shiki-cwt.js";

/**
 * VuePress Shiki plugin for CWT.
 *
 * Usage:
 * - Add to `plugins` in `docs/.vuepress/config.ts`:
 *   `plugins: [ shikiCwtPlugin() ]`
 * - Lazily register the custom language: `langs: [() => shikiCwt()]`.
 * - With `vuepress-theme-hope`, either merge into the theme's Shiki config or add an extra shikiPlugin instance.
 *
 * References:
 * - https://github.com/shikijs/shiki
 * - https://shiki.style/guide/load-lang#custom-languages
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#cwt
 * - https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/docs/.vuepress/highlighters/shiki/shiki-cwt.ts
 *
 * @author windea
 */
export default function shikiCwtPlugin(): Plugin {
  return shikiPlugin({
    langs: [() => shikiCwt()]
  })
}
