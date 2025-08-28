// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

/**
 * Register Prism language definition for Paradox Script.
 *
 * Usage:
 * - Browser: include after Prism, it will auto-register if window.Prism exists
 * - Module: import { registerParadoxScript } and call registerParadoxScript(Prism)
 *
 * @see https://github.com/PrismJS/prism
 * @see https://prismjs.com
 * @see https://prismjs.com/extending#language-definitions
 * @see https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-script
 *
 * @author windea
 */
export function registerParadoxScript(Prism) {
  if (!Prism || Prism.languages.paradox_script) return;

  Prism.languages.paradox_script = {
    // line comment (# ...)
    'comment': /^#.*$/m,
    'boolean': /\b(?:yes|no)\b/,
    'number': /\b[+-]?\d+(?:\.\d+)?\b/,
    // color (rgb{...}, hsv{...}, hsv360{...})
    'function': /\b(?:rgb|hsv|hsv360)\b/,
    // inline math (@[ ... ])
    'inline-math': {
      pattern: /@\[[\s\S]*?]/,
      greedy: true,
      inside: {
        'operator': /[+\-*/%]/,
        'number': /[+-]?(?:\d*\.\d+|\d+)/,
        'punctuation': /[()\[\]|]/
      }
    },
    'operator': /!=|<=|>=|\?=|=|<|>|[+\-*/%]/,
    'punctuation': /[{}\[\](),]/,
    // scripted variable (identifier after @)
    'variable': [
      // declaration
      { pattern: /@[a-zA-Z_$\\[][^@#={}\\s"]*(?!\s*=)/, greedy: true },
      // reference
      { pattern: /@[a-zA-Z0-9_]+/, greedy: true },
    ],
    // property key (before separator =, !=, <, >, <=, >=, ?=)
    'property': {
      pattern: /(^|[\r\n{\s])(?:"(?:[^"\\\r\n]|\\[\s\S])*"|[^#=<>!?{}\s"\r\n]+)(?=\s*(?:!=|<=|>=|\?=|=|<|>))/m,
      lookbehind: true,
    },
    // string (must not before separator)
    'string': {
      // pattern: /[^#$=<>?{}\[\]\s"]+"?|"([^"\\\r\n]|\\[\s\S])*"(?!=|!=|<>)?/,
      pattern: /[^\s"]+"?|"([^"\\\r\n]|\\[\s\S])*"(?!=|!=|<>)?/,
    },
    // parameter ($param$ or $param|arg$)
    'symbol': {
      pattern: /\$[A-Za-z_]\w*(?:\|[^$#{}\[\]\s]+)?\$/,
    },
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try {
    registerParadoxScript(window.Prism);
  } catch {
  }
}
