// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import type { Plugin } from 'vuepress'
import { shikiPlugin } from '@vuepress/plugin-shiki'
import { fileURLToPath } from 'url'
import { dirname, resolve } from 'path'

/**
 * VuePress Shiki plugin for Paradox Localisation.
 *
 * Not enabled by default. Import and add to `plugins` in `docs/.vuepress/config.ts` when needed.
 */
export default function shikiParadoxLocalisationPlugin(): Plugin {
  const __filename = fileURLToPath(import.meta.url)
  const __dirname = dirname(__filename)
  const grammarPath = resolve(__dirname, '../highlighters/shiki/paradox-localisation.tmLanguage.json')
  return shikiPlugin({
    langs: [
      {
        id: 'paradox_localisation',
        scopeName: 'source.paradoxlocalisation',
        path: grammarPath,
        aliases: ['paradox-localisation']
      },
    ],
  })
}
