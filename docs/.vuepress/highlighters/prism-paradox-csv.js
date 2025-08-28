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
 * @see https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-csv
 *
 * @author windea
 */
export function registerParadoxCsv(Prism) {
  if (!Prism || Prism.languages.paradox_csv) return;

  Prism.languages.paradox_csv = {
    comment: /^#.*$/m,
    string: { pattern: /"(?:[^"\r\n]|"")*"/, greedy: true },
    number: /\b[+-]?(?:\d*\.\d+|\d+)\b/,
    punctuation: /[;,\t]/
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try { registerParadoxCsv(window.Prism); } catch {}
}
