// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiCwt from "../../highlighters/shiki/shiki-cwt.js";

/**
 * VuePress Shiki plugin for CWT.
 *
 * Usage:
 * - 在 `docs/.vuepress/config.ts` 中引入并添加到 `plugins`：
 *   `plugins: [ shikiCwtPlugin() ]`
 * - 该插件按需注册自定义语言：`langs: [() => shikiCwt()]`。
 * - 与 `vuepress-theme-hope` 共存时，建议在主题的 Shiki 配置外部追加（或合并到统一的 shikiPlugin 配置）。
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
