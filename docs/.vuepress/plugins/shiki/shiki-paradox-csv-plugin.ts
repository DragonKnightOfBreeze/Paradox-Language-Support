// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiParadoxCsv from "../../highlighters/shiki/shiki-paradox-csv.js";

/**
 * VuePress Shiki plugin for Paradox CSV.
 *
 * Usage:
 * - Add to `plugins` in `docs/.vuepress/config.ts`:
 *   `plugins: [ shikiParadoxCsvPlugin() ]`
 * - Lazily register the custom language: `langs: [() => shikiParadoxCsv()]`.
 * - With `vuepress-theme-hope`, either merge into the theme's Shiki config or add an extra shikiPlugin instance.
 *
 * References:
 * - https://github.com/shikijs/shiki
 * - https://shiki.style/guide/load-lang#custom-languages
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-csv
 * - https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/docs/.vuepress/highlighters/shiki/shiki-paradox-csv.ts
 *
 * @author windea
 */
export default function shikiParadoxCsvPlugin(): Plugin {
  return shikiPlugin({
    langs: [() => shikiParadoxCsv()]
  })
}
