import { defineClientConfig } from 'vuepress/client'
import GameTypeNote from './components/notes/GameTypeNote.vue'
import DefinitionTypeNote from './components/notes/DefinitionTypeNote.vue'

export default defineClientConfig({
  enhance({ app }) {
    app.component('GameTypeNote', GameTypeNote)
    app.component('DefinitionTypeNote', DefinitionTypeNote)

    // register PrismJS language: cwt (client side only)
    if (typeof window !== 'undefined') {
      const Prism: any = (window as any).Prism
      if (Prism && !Prism.languages.cwt) {
        Prism.languages.cwt = {
          // doc comment (### ...), option comment (## ...), line comment (# ...)
          comment: [
            { pattern: /^###.*$/m },
            { pattern: /^##(?!#).*$/m },
            { pattern: /^#(?!#).*$/m }
          ],
          string: {
            pattern: /"(?:[^"\\\r\n]|\\[\s\S])*"/,
            greedy: true
          },
          boolean: /\b(?:yes|no)\b/,
          number: /[+-]?(?:\d*\.\d+|\d+)/,
          operator: /==|=|!=|<>/,
          punctuation: /[{}\[\]]/,
          // key before separator (=, ==, !=, <>)
          property: {
            pattern: /(^|[\r\n{\s])(?:\"(?:[^"\\\r\n]|\\[\s\S])*\"|[^#={}\s"\r\n]+)(?=\s*(?:==|=|!=|<>))/m,
            lookbehind: true,
            alias: 'attr-name'
          },
          // words followed by '[' like alias_name[...]
          'class-name': {
            pattern: /\b[a-zA-Z_][\w-]*?(?=\[)/
          },
          // builtin/type-like identifiers commonly used in CWT
          builtin: /\b(?:int|float|boolean|string|enum|value_set|type|subtype|localisation|images|path|modifier|alias_name|alias_match_left)\b/,
          // hash symbols like #icon
          symbol: /#[A-Za-z0-9_\-]+/
        }
      }
    }
  },
})
