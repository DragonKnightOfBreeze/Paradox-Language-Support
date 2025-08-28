import Prism from 'prismjs'
import { registerParadoxCsv } from '../highlighters/prism/paradox-csv.js'

export default function prismParadoxCsvPlugin() {
  return {
    name: 'prism-paradox-csv',
    extendsMarkdown(/* md */) {
      // Register Paradox CSV language for SSR highlighting
      registerParadoxCsv(Prism)
    },
  }
}
