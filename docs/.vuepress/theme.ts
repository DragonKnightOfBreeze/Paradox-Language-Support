import { hopeTheme } from "vuepress-theme-hope"
import { navbarEn, navbarZh, sidebarEn, sidebarZh } from "./configs/index.js"

export default hopeTheme({
  hostname: "https://windea.icu",
  iconAssets: "fontawesome-with-brands",
  logo: null, // no logo
  repo: "DragonKnightOfBreeze/Paradox-Language-Support",

  author: {
    name: "DragonKnightOfBreeze",
    url: "https://github.com/DragonKnightOfBreeze",
    email: "dk_breeze@qq.com"
  },

  pageInfo: ["Author", "Date", "Word", "ReadingTime"],

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
    redirect: {
      autoLocale: true,
      switchLocale: "direct",
      localeConfig: {
        "/zh/": ["zh-CN", "zh-TW", "zh"],
        "/en/": ["en-US", "en-UK", "en"]
      }
    },
    search: {
      locales: {
        "/zh/": {
          placeholder: "搜索"
        },
        "/en/": {
          placeholder: "Search"
        }
      }
    },
    mdEnhance: {
      gfm: true,
      breaks: false,
      attrs: true,
      alert: true,
      footnote: true,

      stylize: [
        // generate "New in {version}" badges
        {
          matcher: /^\(New in .*\)$/,
          replacer: ({ tag, attrs, content }) => {
            if (tag === "em")
              return { tag: "Badge", attrs: { type: "tip" }, content: content.substring(1, content.length - 1) }
          }
        }
      ]
    }
  }
})

