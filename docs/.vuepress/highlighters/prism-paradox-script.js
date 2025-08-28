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
 * @see https://windea.icu/Paradox-Language-Support/ref-syntax.html#paradox-script
 *
 * @author windea
 */
export function registerParadoxScript(Prism) {
  if (!Prism || Prism.languages.paradox_script) return;

  Prism.languages.paradox_script = {
    comment: /^#.*$/m,
    string: { pattern: /"(?:[^"\\]|\\[\s\S])*"/, greedy: true },
    boolean: /\b(?:yes|no)\b/,
    number: /[+-]?(?:\d*\.\d+|\d+)/,
    // $param$ or $param|arg$
    variable: [
      { pattern: /\$[A-Za-z_]\w*(?:\|[^$#{}\[\]\s]+)?\$/ , greedy: true },
      { pattern: /@[A-Za-z0-9_]+/, alias: 'symbol' }
    ],
    // rgb{...}, hsv{...}, hsv360{...}
    function: /\b(?:rgb|hsv|hsv360)\b/,
    // @[ ... ] inline math
    'inline-math': {
      pattern: /@\[[\s\S]*?\]/,
      greedy: true,
      inside: {
        operator: /[+\-*/%]/,
        number: /[+-]?(?:\d*\.\d+|\d+)/,
        punctuation: /[()\[\]|]/
      }
    },
    // key before separator (=, !=, <, >, <=, >=, ?=)
    property: {
      pattern: /(^|[\r\n{\s])(?:"(?:[^"\\\r\n]|\\[\s\S])*"|[^#=<>!?{}\s"\r\n]+)(?=\s*(?:!=|<=|>=|\?=|=|<|>))/m,
      lookbehind: true,
      alias: 'attr-name'
    },
    operator: /!=|<=|>=|\?=|=|<|>|[+\-*/%]/,
    punctuation: /[{}\[\](),]/,
    'class-name': { pattern: /\b[a-zA-Z_][\w-]*?(?=\[)/ }
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try { registerParadoxScript(window.Prism); } catch {}
}
