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

  const parameter = {
    pattern: /\$[A-Za-z_][A-Za-z0-9_]*(?:\|[^#$=<>?{}\[\]\s]+)?\$/,
    greedy: true,
    alias: 'class-name',
  }
  const escape = { pattern: /\\./ }

  Prism.languages.paradox_script = {
    // line comment (# ...)
    'comment': {
      pattern: /#.*$/m,
    },
    'boolean': /\b(?:yes|no)\b/,
    'number': /\b[+-]?\d+(?:\.\d+)?\b/,
    // color (rgb {...}, hsv {...}, hsv360 {...})
    'function': /\b(?:rgb|hsv|hsv360)\b/,
    // inline math (@[ ... ])
    'inline-math': {
      pattern: /@\[[\s\S]*?]/,
      greedy: true,
      inside: {
        'punctuation': /@\\\[|@\[|]|[()|]/,
        'operator': /[+\-*/%]/,
        'number': /[+-]?(?:\d*\.\d+|\d+)/,
        'parameter': parameter,
        'scripted-variable': {
          pattern: /[A-Za-z_$\[][^@#={}\s"]*/,
          alias: [ 'variable' ],
          inside: {
            'parameter': parameter,
          },
        },
      }
    },
    'condition-expression': {
      pattern: /(?<=\[\[)!?[A-Za-z_][A-Za-z0-9_]*(?=])/,
      alias: 'keyword',
    },
    // scripted variable (identifier after @)
    'scripted-variable': {
      pattern: /(^|[\s\[\]{}<>=])@[A-Za-z_$\[][^@#={}\s"]*/,
      lookbehind: true,
      greedy: true,
      alias: [ 'variable' ],
      inside: {
        'parameter': parameter,
      }
    },
    // property key (before separator =, !=, <, >, <=, >=, ?=)
    'property': [
      {
        pattern: /"(?:[^"\\\r\n]|\\[\s\S])*"?(?=\s*(?:=|!=|<|>|<=|>=|\?=))/,
        greedy: true,
        inside: {
          'parameter': parameter,
          'escape': escape,
        }
      },
      {
        pattern: /[^@#=<>?{}\[\]\s"]+"?(?=\s*(?:=|!=|<|>|<=|>=|\?=))/,
        inside: {
          'parameter': parameter,
          'escape': escape,
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
          'parameter': parameter,
          'escape': escape,
        }
      },
      {
        pattern: /[^@#=<>?{}\[\]\[\]\s"]+"?/, // exclude brackets here for compatibility
        inside: {
          'parameter': parameter,
          'escape': escape,
        }
      },
    ],
    'punctuation': [
      { pattern: /[{},]/ },
      { pattern: /[\[\]]/, greedy: true },
    ],
    'operator': /!=|<=|>=|\?=|=|<|>|[+\-*/%]/,
  };
}

// auto-register for browser usage
if (typeof window !== 'undefined' && window.Prism) {
  try {
    registerParadoxScript(window.Prism);
  } catch {
  }
}
