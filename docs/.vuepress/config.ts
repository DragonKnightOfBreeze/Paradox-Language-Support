import { defineUserConfig } from "vuepress"
import { viteBundler } from "@vuepress/bundler-vite"
import { head } from "./configs/index.js"
import theme from "./theme.js"

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
  bundler: viteBundler(),
  theme: theme,
})
