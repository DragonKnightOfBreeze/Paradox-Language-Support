// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

/**
 * Register Prism language definition for Paradox Localisation.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerParadoxLocalisation } and call registerParadoxLocalisation(Prism)
 *
 * @see https://github.com/PrismJS/prism
 * @see https://prismjs.com
 * @see https://prismjs.com/extending#language-definitions
 * @see https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-localisation
 *
 * @author windea
 */
export function registerParadoxLocalisation(Prism) {
  if (!Prism || Prism.languages.paradox_localisation) return;

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
        parameter: { pattern: /\$(?:@[A-Za-z_]\w*|[A-Za-z0-9_.\-']+)(?:\|[^"§$\[\]\r\n\\]+)?\$/ , greedy: true, alias: 'variable' },
        icon: { pattern: /£[A-Za-z0-9\-_\/\\]+(?:\|[^"§$\[\]\r\n\\]+)?£/, greedy: true },
        command: { pattern: /\[[^\]\r\n]*\]/, greedy: true },
        'text-format-start': { pattern: /#(?!!)[\w:;]+/, alias: 'function' },
        'text-format-end': { pattern: /#!/, alias: 'important' },
        'text-icon': { pattern: /@[A-Za-z0-9_]+!/, alias: 'function' },
        escape: { pattern: /\\./ }
      }
    },
    punctuation: /[:\[\]]/
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try { registerParadoxLocalisation(window.Prism); } catch {}
}
