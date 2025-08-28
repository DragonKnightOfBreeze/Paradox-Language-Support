import Prism from 'prismjs'
import { registerParadoxScript } from '../highlighters/prism/paradox-script.js'

export default function prismParadoxScriptPlugin() {
  return {
    name: 'prism-paradox-script',
    extendsMarkdown() {
      // Register Paradox Script language for SSR highlighting
      registerParadoxScript(Prism)
    },
  }
}
