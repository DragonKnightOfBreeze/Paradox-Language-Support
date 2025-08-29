import { hopeTheme } from "vuepress-theme-hope"
import { navbarEn, navbarZh, sidebarEn, sidebarZh } from "./configs/index.js"
import shikiCwt from "./highlighters/shiki/shiki-cwt.js";
import shikiParadoxScript from "./highlighters/shiki/shiki-paradox-script.js";
import shikiParadoxLocalisation from "./highlighters/shiki/shiki-paradox-localisation.js";
import shikiParadoxCsvPlugin from "./plugins/shiki/shiki-paradox-csv-plugin.js";
import shikiParadoxCsv from "./highlighters/shiki/shiki-paradox-csv.js";

export default hopeTheme({
  hostname: "https://windea.icu",
  logo: null, // no logo
  repo: "DragonKnightOfBreeze/Paradox-Language-Support",

  author: {
    name: "DragonKnightOfBreeze",
    url: "https://github.com/DragonKnightOfBreeze",
    email: "dk_breeze@qq.com"
  },

  pageInfo: ["Author", "Date", "Word", "ReadingTime"],

  docsDir: "docs",
  docsBranch: "master",

  pure: true,
  breadcrumb: false,
  navbarLayout: {
    start: ["Brand"],
    center: [],
    end: ["Links", "Language", "Repo", "Outlook", "Search"]
  },

  locales: {
    "/zh/": {
      navbar: navbarZh,
      sidebar: sidebarZh,
      metaLocales: {
        editLink: "在 GitHub 上编辑此页"
      }
    },
    "/en/": {
      navbar: navbarEn,
      sidebar: sidebarEn,
      metaLocales: {
        editLink: "Edit this page on GitHub"
      }
    }
  },

  plugins: {
    components: {
      components: [
        "ArtPlayer"
      ],
      componentOptions: {
        artPlayer: {
          muted: true // muted by default
        }
      }
    },

    redirect: {
      autoLocale: true,
      switchLocale: "direct",
      localeConfig: {
        "/zh/": ["zh-CN", "zh-TW", "zh"],
        "/en/": ["en-US", "en-UK", "en"],
      }
    },
    // note that fulltext search is not supported by @vuepress/plugin-search (only for titles)
    search: {
      maxSuggestions: 20,
      locales: {
        "/zh/": { placeholder: "搜索" },
        "/en/": { placeholder: "Search" }
      }
    },
  },

  markdown: {
    highlighter: {
      type: "shiki",
      // Custom language supports
      langs: [
        () => shikiCwt(),
        () => shikiParadoxScript(),
        () => shikiParadoxLocalisation(),
        () => shikiParadoxCsv(),
      ]
    },
    gfm: true,
    breaks: false,
    attrs: true,
    alert: true,
    footnote: true,

    stylize: [
      // generate "New in {version}" badges
      {
        matcher: /^\(New in .*\)$/,
        replacer: ({ tag, content }) => {
          if (tag === "em") {
            return { tag: "Badge", attrs: { type: "tip" }, content: content.substring(1, content.length - 1) }
          }
        }
      }
    ],
  },
})

