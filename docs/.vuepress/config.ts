import { defineUserConfig } from "vuepress"
import { viteBundler } from "@vuepress/bundler-vite"
import { head } from "./configs/index.js"
import theme from "./theme.js"
import prismCwtPlugin from "./plugins/prism-cwt.js"
import prismParadoxScriptPlugin from "./plugins/prism-paradox-script.js"
import prismParadoxLocalisationPlugin from "./plugins/prism-paradox-localisation.js"
import prismParadoxCsvPlugin from "./plugins/prism-paradox-csv.js"

export default defineUserConfig({
  base: "/Paradox-Language-Support/",
  head,
  locales: {
    "/zh/": {
      lang: "zh-CN",
      title: "Paradox Language Support"
    },
    "/en/": {
      lang: "en-US",
      title: "Paradox Language Support"
    }
  },
  plugins: [
    prismCwtPlugin(),
    prismParadoxScriptPlugin(),
    prismParadoxLocalisationPlugin(),
    prismParadoxCsvPlugin(),
  ],
  bundler: viteBundler(),
  theme: theme,
})
