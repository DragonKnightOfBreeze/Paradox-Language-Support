// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

/**
 * Shiki language registration factory for Paradox Script.
 *
 * Usage:
 * - Import in VuePress Shiki plugin and register lazily: `langs: [() => shikiParadoxScript()]`.
 * - See plugin: `docs/.vuepress/plugins/shiki/shiki-paradox-script-plugin.ts`.
 *
 * References:
 * - https://github.com/shikijs/shiki
 * - https://shiki.style/guide/load-lang#custom-languages
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-script
 *
 * @author windea
 */
import { LanguageRegistration } from "shiki";
import { resolve } from "path";
import { readFileSync } from "fs";

export default function shikiParadoxScript(): LanguageRegistration {
  // Get the path to the TextMate grammar file
  const grammarPath = resolve(__dirname, '../text-mate/paradox-script.tmLanguage.json')
  // Load TextMate grammar JSON and adapt it to Shiki's LanguageRegistration
  const grammarJson = JSON.parse(readFileSync(grammarPath, 'utf-8'))
  return <LanguageRegistration>{
    ...(grammarJson as object),
    // Ensure required and friendly fields
    name: 'paradox_script',
    scopeName: 'source.paradoxscript',
    aliases: ['paradox-script'],
  }
}
