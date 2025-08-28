// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import { fileURLToPath } from 'url'
import { dirname, resolve } from 'path'

/**
 * VuePress Shiki plugin for Paradox Script.
 *
 * Not enabled by default. Import and add to `plugins` in `docs/.vuepress/config.ts` when needed.
 */
export default function shikiParadoxScriptPlugin(): Plugin {
  const __filename = fileURLToPath(import.meta.url)
  const __dirname = dirname(__filename)
  const grammarPath = resolve(__dirname, '../highlighters/shiki/paradox-script.tmLanguage.json')
  return shikiPlugin({
    langs: [
      {
        id: 'paradox_script',
        scopeName: 'source.paradoxscript',
        path: grammarPath,
        aliases: ['paradox-script']
      },
    ],
  })
}
