// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiCwt from "../../highlighters/shiki/shiki-cwt.js";

/**
 * VuePress Shiki plugin for CWT.
 *
 * Not enabled by default. Import and add to `plugins` in `docs/.vuepress/config.ts` when needed.
 */
export default function shikiCwtPlugin(): Plugin {
  return shikiPlugin({
    langs: [() => shikiCwt()]
  })
}
