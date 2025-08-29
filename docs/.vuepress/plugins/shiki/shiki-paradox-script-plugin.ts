// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiParadoxScript from "../../highlighters/shiki/shiki-paradox-script.js";

/**
 * VuePress Shiki plugin for Paradox Script.
 *
 * Not enabled by default. Import and add to `plugins` in `docs/.vuepress/config.ts` when needed.
 */
export default function shikiParadoxScriptPlugin(): Plugin {
  return shikiPlugin({
    langs: [() => shikiParadoxScript()]
  })
}
