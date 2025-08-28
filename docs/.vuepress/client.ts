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
      if (Prism) {
        // CWT
        if (!Prism.languages.cwt) {
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

        // Paradox Script
        if (!Prism.languages.paradox_script) {
          Prism.languages.paradox_script = {
            comment: /^#.*$/m,
            string: { pattern: /"(?:[^"\\]|\\[\s\S])*"/, greedy: true },
            boolean: /\b(?:yes|no)\b/,
            number: /[+-]?(?:\d*\.\d+|\d+)/,
            // $param$ or $param|arg$
            variable: [
              { pattern: /\$[A-Za-z_][\w]*(?:\|[^$#{}\[\]\s]+)?\$/ , greedy: true },
              { pattern: /@[A-Za-z0-9_]+/, alias: 'symbol' }
            ],
            // rgb{...}, hsv{...}, hsv360{...}
            function: /\b(?:rgb|hsv|hsv360)\b/,
            // @[ ... ] inline math
            'inline-math': {
              pattern: /@\[[\s\S]*?\]/,
              greedy: true,
              inside: {
                operator: /[+\-*/%]/,
                number: /[+-]?(?:\d*\.\d+|\d+)/,
                punctuation: /[()\[\]|]/
              }
            },
            // key before separator (=, !=, <, >, <=, >=, ?=)
            property: {
              pattern: /(^|[\r\n{\s])(?:\"(?:[^"\\\r\n]|\\[\s\S])*\"|[^#=<>!?{}\s"\r\n]+)(?=\s*(?:!=|<=|>=|\?=|=|<|>))/m,
              lookbehind: true,
              alias: 'attr-name'
            },
            operator: /!=|<=|>=|\?=|=|<|>|[+\-*/%]/,
            punctuation: /[{}\[\](),]/,
            'class-name': { pattern: /\b[a-zA-Z_][\w-]*?(?=\[)/ }
          }
        }

        // Paradox Localisation
        if (!Prism.languages.paradox_localisation) {
          Prism.languages.paradox_localisation = {
            comment: /^#.*$/m,
            // l_english:
            locale: { pattern: /^[a-z_]+\s*:/m, alias: 'symbol' },
            // key: before colon
            property: { pattern: /(^|[\r\n])\s*[A-Za-z0-9_.\-']+(?=\s*:)/m, lookbehind: true, alias: 'attr-name' },
            number: /\b\d+(?:\.\d+)?\b/,
            string: {
              pattern: /"(?:[^"\r\n]|\\[\s\S])*"/,
              greedy: true,
              inside: {
                'color-end': { pattern: /§!/, alias: 'important' },
                'color-start': { pattern: /§[A-Za-z0-9]/, alias: 'keyword' },
                parameter: { pattern: /\$(?:@[A-Za-z_]\w*|[A-Za-z0-9_.\-']+)(?:\|[^"§\$\[\]\r\n\\]+)?\$/ , greedy: true, alias: 'variable' },
                icon: { pattern: /£[A-Za-z0-9\-_/\\]+(?:\|[^"§\$\[\]\r\n\\]+)?£/, greedy: true },
                command: { pattern: /\[[^\]\r\n]*\]/, greedy: true },
                'text-format-start': { pattern: /#(?!\!)[\w:;]+/, alias: 'function' },
                'text-format-end': { pattern: /#!/, alias: 'important' },
                'text-icon': { pattern: /@[A-Za-z0-9_]+!/, alias: 'function' },
                escape: { pattern: /\\./ }
              }
            },
            punctuation: /[:\[\]]/
          }
        }

        // Paradox CSV (semicolon-separated; # as line comment)
        if (!Prism.languages.paradox_csv) {
          Prism.languages.paradox_csv = {
            comment: /^#.*$/m,
            string: { pattern: /"(?:[^"\r\n]|"")*"/, greedy: true },
            number: /\b[+-]?(?:\d*\.\d+|\d+)\b/,
            punctuation: /[;,\t]/
          }
        }
      }
    }
  },
})
