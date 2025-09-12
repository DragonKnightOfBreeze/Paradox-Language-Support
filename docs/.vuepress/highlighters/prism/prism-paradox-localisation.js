// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

/**
 * Register Prism language definition for Paradox Localisation.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerParadoxLocalisation } and call registerParadoxLocalisation(Prism)
 *
 * References:
 * - https://github.com/PrismJS/prism
 * - https://prismjs.com
 * - https://prismjs.com/extending#language-definitions
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-localisation
 *
 * @author windea
 */
export function registerParadoxLocalisation(Prism) {
  if (!Prism || Prism.languages.paradox_localisation) return;

  const parameter = {
    pattern: /\$(?:@[A-Za-z_]\w*|[A-Za-z0-9_.\-']+)(?:\|[^"§$\[\]\r\n\\]+)?\$/,
    greedy: true,
    alias: 'class-name',
    inside: {
      'scripted-variable': {
        pattern: /@[A-Za-z_$\[][^@#={}\s"]*/,
        alias: 'variable',
      }
    },
  }
  const escape = { pattern: /\\./ }

  Prism.languages.paradox_localisation = {
    // line comment (# ...)
    'comment': {
      pattern: /#.*$/m,
    },
    // locale (l_english:)
    'locale': {
      pattern: /^[a-z_]+:\s*$/m,
      alias: 'symbol'
    },
    // key
    'property': {
      pattern: /(^|\s)[A-Za-z0-9_.\-']+/m,
      lookbehind: true
    },
    'number': /\b\d+\b/,
    'string': {
      pattern: /"[^\r\n]+(?=\s*(?:#[^"\r\n]*)?$)/m,
      greedy: true,
      inside: {
        'color-end': { pattern: /§!/, alias: 'important' },
        'color-start': { pattern: /§[A-Za-z0-9]/, alias: 'important' },
        'parameter': parameter,
        'icon': { pattern: /£[A-Za-z0-9\-_\/\\]+(?:\|[^"§$\[\]\r\n\\]+)?£/, greedy: true, alias: 'symbol' },
        'command': { pattern: /\[[^\]\r\n]*]/, greedy: true, alias: 'function' },
        'text-format-start': { pattern: /#(?!!)[\w:;]+/, alias: 'symbol' },
        'text-format-end': { pattern: /#!/, alias: 'symbol' },
        'text-icon': { pattern: /@[A-Za-z0-9_]+!/, alias: 'symbol' },
        'escape': escape,
      }
    },
    'punctuation': /:/,
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try {
    registerParadoxLocalisation(window.Prism);
  } catch {
  }
}
