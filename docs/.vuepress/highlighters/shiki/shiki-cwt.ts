// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

import { LanguageRegistration } from "shiki";
import { resolve } from "path";
import { readFileSync } from "fs";

/**
 * Shiki language registration factory for CWT.
 *
 * Usage:
 * - Import in VuePress Shiki plugin and register lazily: `langs: [() => shikiCwt()]`.
 * - See plugin: `docs/.vuepress/plugins/shiki/shiki-cwt-plugin.ts`.
 *
 * References:
 * - https://github.com/shikijs/shiki
 * - https://shiki.style/guide/load-lang#custom-languages
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#cwt
 *
 * @author windea
 */
export default function shikiCwt(): LanguageRegistration {
  // Get the path to the TextMate grammar file
  const grammarPath = resolve(__dirname, '../text-mate/cwt.tmLanguage.json')
  // Load TextMate grammar JSON and adapt it to Shiki's LanguageRegistration
  const grammarJson = JSON.parse(readFileSync(grammarPath, 'utf-8'))
  return <LanguageRegistration>{
    ...(grammarJson as object),
    // Ensure required and friendly fields
    name: 'cwt',
    scopeName: 'source.cwt',
  }
}
