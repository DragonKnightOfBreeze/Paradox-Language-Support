// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

/**
 * Register Prism language definition for Paradox Script.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerParadoxScript } and call registerParadoxScript(Prism)
 *
 * References:
 * - https://github.com/PrismJS/prism
 * - https://prismjs.com
 * - https://prismjs.com/extending#language-definitions
 * - https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-script
 *
 * @author windea
 */
export function registerParadoxScript(Prism) {
  if (!Prism || Prism.languages.paradox_script) return;

  Prism.languages.paradox_script = {
    // line comment (# ...)
    'comment': {
      pattern: /(^|\s)#.*/,
      lookbehind: true,
    },
    // scripted variable (identifier after @)
    'variable': {
      pattern: /(^|[\s\[{<>=])@[A-Za-z_$\[][^@#={}\s"]*/,
      lookbehind: true,
      greedy: true,
      alias: 'symbol',
    },
    'boolean': /\b(?:yes|no)\b/,
    'number': /\b[+-]?\d+(?:\.\d+)?\b/,
    // color (rgb{...}, hsv{...}, hsv360{...})
    'function': /\b(?:rgb|hsv|hsv360)\b/,
    // inline math (@[ ... ])
    'inline-math': {
      pattern: /@\[[\s\S]*?]/,
      greedy: true,
      inside: {
        'variable': /@[A-Za-z_$\[][^@#={}\s"]*/,
        'operator': /[+\-*/%]/,
        'number': /[+-]?(?:\d*\.\d+|\d+)/,
        'punctuation': /@\[|[()\[\]|]/
      }
    },
    // property key (before separator =, !=, <, >, <=, >=, ?=)
    'property': [
      {
        pattern: /"(?:[^"\\\r\n]|\\[\s\S])*"?(?=\s*(?:=|!=|<|>|<=|>=|\?=))/,
        greedy: true,
        inside: {
          'escape': { pattern: /\\./ }
        }
      },
      {
        pattern: /[^@#=<>?{}\[\]\s"]+"?(?=\s*(?:=|!=|<|>|<=|>=|\?=))/,
        inside: {
          'escape': { pattern: /\\./ }
        }
      },
    ],
    // string (must not before separator)
    'string': [
      // can be multiline
      {
        pattern: /"(?:[^"\\]|\\[\s\S])*"?/,
        greedy: true,
        inside: {
          'escape': { pattern: /\\./ }
        }
      },
      {
        pattern: /[^@#=<>?{}\[\]\s"]+"?/,
        inside: {
          'escape': { pattern: /\\./ }
        }
      },
    ],
    'operator': /!=|<=|>=|\?=|=|<|>|[+\-*/%]/,
    'punctuation': /[{}\[\](),]/,
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try {
    registerParadoxScript(window.Prism);
  } catch {
  }
}
