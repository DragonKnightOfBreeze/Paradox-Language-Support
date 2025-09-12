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

  const escape = { pattern: /\\./ }

  Prism.languages.cwt = {
    // doc comment (### ...) (must at line start)
    'doc-comment': {
      pattern: /(^\s*)###.*$/m,
      lookbehind: true,
      alias: 'comment',
    },
    // option comment (## ...) (must at line start)
    'option-comment': {
      pattern: /(^\s*)##.*$/m,
      lookbehind: true,
      alias: 'comment',
    },
    // line comment (# ...)
    'comment': {
      pattern: /#.*$/m,
    },
    'boolean': /\b(?:yes|no)\b/,
    'number': /\b[+-]?\d+(?:\.\d+)?\b/,
    // property key (before separator ==, =, !=, <>)
    'property': [
      {
        pattern: /"(?:[^"\\\r\n]|\\[\s\S])*"?(?=\s*(?:==|=|!=|<>))/,
        greedy: true,
        inside: {
          'escape': escape
        }
      },
      {
        pattern: /[^#={},\s"]+"?(?=\s*(?:==|=|!=|<>))/,
        inside: {
          'escape': escape
        }
      },
    ],
    'string': [
      {
        pattern: /"(?:[^"\\\r\n]|\\[\s\S])*"?/,
        greedy: true,
        inside: {
          'escape': escape
        }
      },
      {
        pattern: /[^#={},\s"]+"?/,
        inside: {
          'escape': escape
        }
      },
    ],
    'operator': /==|=|!=|<>/,
    'punctuation': /[{}]/,
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try {
    registerCwt(window.Prism);
  } catch {
  }
}
