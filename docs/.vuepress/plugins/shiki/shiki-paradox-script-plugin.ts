// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiParadoxScript from "../../highlighters/shiki/shiki-paradox-script.js";

/**
 * VuePress Shiki plugin for Paradox Script.
 *
 * Usage:
 * - 在 `docs/.vuepress/config.ts` 中引入并添加到 `plugins`：
 *   `plugins: [ shikiParadoxScriptPlugin() ]`
 * - 该插件按需注册自定义语言：`langs: [() => shikiParadoxScript()]`。
 * - 与 `vuepress-theme-hope` 共存时，建议在主题的 Shiki 配置外部追加（或合并到统一的 shikiPlugin 配置）。
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
