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
    scopeName: 'source.paradox-script',
    aliases: ['paradox-script'],
  }
}
