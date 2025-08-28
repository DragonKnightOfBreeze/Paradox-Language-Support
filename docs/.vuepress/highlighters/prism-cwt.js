// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

/**
 * Register Prism language definition for CWT.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerCwt } and call registerCwt(Prism)
 *
 * @see https://github.com/PrismJS/prism
 * @see https://prismjs.com
 * @see https://prismjs.com/extending#language-definitions
 * @see https://windea.icu/Paradox-Language-Support/ref-syntax.html#cwt
 *
 * @author windea
 */
export function registerCwt(Prism) {
  if (!Prism || Prism.languages.cwt) return;

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
      pattern: /(^|[\r\n{\s])(?:"(?:[^"\\\r\n]|\\[\s\S])*"|[^#={}\s"\r\n]+)(?=\s*(?:==|=|!=|<>))/m,
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
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try { registerCwt(window.Prism); } catch {}
}
