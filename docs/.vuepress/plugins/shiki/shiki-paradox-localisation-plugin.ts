// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiParadoxLocalisation from "../../highlighters/shiki/shiki-paradox-localisation.js";

/**
 * VuePress Shiki plugin for Paradox Localisation.
 *
 * Not enabled by default. Import and add to `plugins` in `docs/.vuepress/config.ts` when needed.
 */
export default function shikiParadoxLocalisationPlugin(): Plugin {
  return shikiPlugin({
    langs: [() => shikiParadoxLocalisation()]
  })
}
