// Prism language definition for Paradox CSV (semicolon-separated; # as line comment)
// Usage:
// - Browser: include after Prism, it will auto-register if window.Prism exists
// - Module: import { registerParadoxCsv } and call registerParadoxCsv(Prism)

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
