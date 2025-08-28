// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

/**
 * Register Prism language definition for Paradox CSV (semicolon-separated; # as line comment).
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerParadoxCsv } and call registerParadoxCsv(Prism)
 *
 * @see https://github.com/PrismJS/prism
 * @see https://prismjs.com
 * @see https://prismjs.com/extending#language-definitions
 * @see https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-csv
 *
 * @author windea
 */
export function registerParadoxCsv(Prism) {
  if (!Prism || Prism.languages.paradox_csv) return;

  Prism.languages.paradox_csv = {
    // line comment (# ...) (must at line start)
    'comment': {
      pattern: /(^|\s+)#.*/,
      lookbehind: true,
    },
    'boolean': /\b(?:yes|no)\b/,
    'number': /\b[+-]?\d+(?:\.\d+)?\b/,
    'string': [
      { pattern: /[^#;"\s]([^#;"\r\n]*[^#;\s])?/ }, // middle whitespaces are permitted
      { pattern: /"([^"\\\r\n]|\\[\s\S])*"?/, greedy: true },
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
