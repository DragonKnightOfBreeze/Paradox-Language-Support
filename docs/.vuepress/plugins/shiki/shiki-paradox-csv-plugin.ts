// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiParadoxCsv from "../../highlighters/shiki/shiki-paradox-csv.js";

/**
 * VuePress Shiki plugin for Paradox CSV.
 *
 * Usage:
 * - 在 `docs/.vuepress/config.ts` 中引入并添加到 `plugins`：
 *   `plugins: [ shikiParadoxCsvPlugin() ]`
 * - 该插件按需注册自定义语言：`langs: [() => shikiParadoxCsv()]`。
 * - 与 `vuepress-theme-hope` 共存时，建议在主题的 Shiki 配置外部追加（或合并到统一的 shikiPlugin 配置）。
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
