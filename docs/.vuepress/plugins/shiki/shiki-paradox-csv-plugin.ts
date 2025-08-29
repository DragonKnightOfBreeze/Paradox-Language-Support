// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import shikiParadoxCsv from "../../highlighters/shiki/shiki-paradox-csv.js";

/**
 * VuePress Shiki plugin for Paradox CSV.
 *
 * Not enabled by default. Import and add to `plugins` in `docs/.vuepress/config.ts` when needed.
 */
export default function shikiParadoxCsvPlugin(): Plugin {
  return shikiPlugin({
    langs: [() => shikiParadoxCsv()]
  })
}
