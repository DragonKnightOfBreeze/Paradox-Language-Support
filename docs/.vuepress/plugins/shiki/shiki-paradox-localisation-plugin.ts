// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiParadoxLocalisation from "../../highlighters/shiki/shiki-paradox-localisation.js";

/**
 * VuePress Shiki plugin for Paradox Localisation.
 *
 * Usage:
 * - Add to `plugins` in `docs/.vuepress/config.ts`:
 *   `plugins: [ shikiParadoxLocalisationPlugin() ]`
 * - Lazily register the custom language: `langs: [() => shikiParadoxLocalisation()]`.
 * - With `vuepress-theme-hope`, either merge into the theme's Shiki config or add an extra shikiPlugin instance.
 *
 *
 * References:
 * - https://github.com/shikijs/shiki
 * - https://shiki.style/guide/load-lang#custom-languages
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-localisation
 * - https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/docs/.vuepress/highlighters/shiki/shiki-paradox-localisation.ts
 *
 * @author windea
 */
export default function shikiParadoxLocalisationPlugin(): Plugin {
  return shikiPlugin({
    langs: [() => shikiParadoxLocalisation()]
  })
}
