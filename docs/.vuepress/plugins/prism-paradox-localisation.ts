import Prism from 'prismjs'
import { registerParadoxLocalisation } from '../highlighters/prism/paradox-localisation.js'

export default function prismParadoxLocalisationPlugin() {
  return {
    name: 'prism-paradox-localisation',
    extendsMarkdown() {
      // Register Paradox Localisation language for SSR highlighting
      registerParadoxLocalisation(Prism)
    },
  }
}
