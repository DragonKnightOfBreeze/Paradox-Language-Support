import { defineUserConfig } from "vuepress"
import { viteBundler } from "@vuepress/bundler-vite"
import { head } from "./configs/index.js"
import theme from "./theme.js"
import { getPrismPlugins } from "./configs/highlighters.js";

export default defineUserConfig({
  base: "/Paradox-Language-Support/",
  head,
  locales: {
    "/zh/": {
      lang: "zh-CN",
      title: "Paradox Chronicle"
    },
    "/en/": {
      lang: "en-US",
      title: "Paradox Chronicle"
    }
  },
  plugins: [
    ...getPrismPlugins()
  ],
  bundler: viteBundler(),
  theme: theme,
})
