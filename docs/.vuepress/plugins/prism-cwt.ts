import Prism from 'prismjs'
import { registerCwt } from '../highlighters/prism/cwt.js'

export default function prismCwtPlugin() {
  return {
    name: 'prism-cwt',
    extendsMarkdown() {
      // Register CWT config file language for SSR highlighting
      registerCwt(Prism)
    },
  }
}
