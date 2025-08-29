// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiParadoxScript from "../../highlighters/shiki/shiki-paradox-script.js";

/**
 * VuePress Shiki plugin for Paradox Script.
 *
 * Usage:
 * - Add to `plugins` in `docs/.vuepress/config.ts`:
 *   `plugins: [ shikiParadoxScriptPlugin() ]`
 * - Lazily register the custom language: `langs: [() => shikiParadoxScript()]`.
 * - With `vuepress-theme-hope`, either merge into the theme's Shiki config or add an extra shikiPlugin instance.
 *
 * References:
 * - https://github.com/shikijs/shiki
 * - https://shiki.style/guide/load-lang#custom-languages
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-script
 * - https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/docs/.vuepress/highlighters/shiki/shiki-paradox-script.ts
 */
export default function shikiParadoxScriptPlugin(): Plugin {
  return shikiPlugin({
    langs: [() => shikiParadoxScript()]
  })
}
