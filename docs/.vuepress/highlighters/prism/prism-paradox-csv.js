// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

/**
 * Register Prism language definition for Paradox CSV (semicolon-separated; # as line comment).
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerParadoxCsv } and call registerParadoxCsv(Prism)
 *
 * References:
 * - https://github.com/PrismJS/prism
 * - https://prismjs.com
 * - https://prismjs.com/extending#language-definitions
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-csv
 *
 * @author windea
 */
export function registerParadoxCsv(Prism) {
  if (!Prism || Prism.languages.paradox_csv) return;

  const escape = { pattern: /\\./ }

  Prism.languages.paradox_csv = {
    // line comment (# ...) (must at line start)
    'comment': {
      pattern: /(^\s*)#.*$/m,
    },
    'boolean': /\b(?:yes|no)\b/,
    'number': /\b[+-]?\d+(?:\.\d+)?\b/,
    'string': [
      {
        pattern: /"([^"\\\r\n]|\\[\s\S])*"?/,
        greedy: true,
        inside: {
          'escape': escape
        }
      },
      {
        pattern: /[^#;"\s]+"?/,
        inside: {
          'escape': escape
        }
      },
    ],
    'punctuation': /;/, // only for semicolons
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try {
    registerParadoxCsv(window.Prism);
  } catch {
  }
}
