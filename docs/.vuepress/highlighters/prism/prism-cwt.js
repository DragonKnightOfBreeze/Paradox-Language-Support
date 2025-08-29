// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

/**
 * Register Prism language definition for CWT.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerCwt } and call registerCwt(Prism)
 *
 * References:
 * - https://github.com/PrismJS/prism
 * - https://prismjs.com
 * - https://prismjs.com/extending#language-definitions
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#cwt
 *
 * @author windea
 */
export function registerCwt(Prism) {
  if (!Prism || Prism.languages.cwt) return;

  Prism.languages.cwt = {
    // doc comment (### ...)
    'doc-comment': {
      pattern: /(^|\s)###.*/,
      lookbehind: true,
      alias: 'comment',
    },
    // option comment (## ...)
    'option-comment': {
      pattern: /(^|\s)##.*/,
      lookbehind: true,
      alias: 'comment',
    },
    // line comment (# ...)
    'comment': {
      pattern: /(^|\s)#.*/,
      lookbehind: true,
    },
    'boolean': /\b(?:yes|no)\b/,
    'number': /\b[+-]?\d+(?:\.\d+)?\b/,
    // property key (before separator ==, =, !=, <>)
    'property': [
      { pattern: /[^#={}\s"]+"?(?=\s*(?:==|=|!=|<>))/ },
      { pattern: /"([^"\\\r\n]|\\[\s\S])*"?(?=\s*(?:==|=|!=|<>))/, greedy: true },
    ],
    'string': [
      {
        pattern: /"([^"\\\r\n]|\\[\s\S])*"?/,
        greedy: true,
        inside: {
          'escape': { pattern: /\\./ }
        }
      },
      {
        pattern: /[^#={}\s"]+"?/,
        inside: {
          'escape': { pattern: /\\./ }
        }
      },
    ],
    'operator': /==|=|!=|<>/,
    'punctuation': /[{}\[\]]/,
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try {
    registerCwt(window.Prism);
  } catch {
  }
}
