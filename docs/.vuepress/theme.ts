import { hopeTheme } from "vuepress-theme-hope"
import { navbarEn, navbarZh, sidebarEn, sidebarZh } from "./configs/index.js"
import { getHighlighterOptions } from "./configs/highlighters.js";

export default hopeTheme({
  hostname: "https://windea.icu",
  favicon: "/images/favicon.png",
  repo: "DragonKnightOfBreeze/Paradox-Language-Support",

  author: {
    name: "DragonKnightOfBreeze",
    url: "https://github.com/DragonKnightOfBreeze",
    email: "dk_breeze@qq.com"
  },

  pageInfo: ["Author", "Date", "Word", "ReadingTime"],

  docsBranch: "master",
  docsDir: "docs",

  pure: true,
  focus: false,
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
      routerLocales: {
        notFoundMsg: [
          "这里什么也没有",
          "我们是怎么来到这儿的？",
          "这 是 四 零 四 !",
          "看起来你访问了一个失效的链接",
          "这里一片荒芜……",
          "你来到了一片未知的荒野……",
          "此地只有虚空……",
          "你踏入了一处被遗忘的古战场……",
        ]
      }
    },
    "/en/": {
      navbar: navbarEn,
      sidebar: sidebarEn,
      routerLocales: {
        notFoundMsg: [
          "There’s nothing here.",
          "How did we get here?",
          "That’s a Four-Oh-Four.",
          "Looks like we've got some broken links.",
          "This place is barren...",
          "You have arrived in an unknown wilderness…",
          "Here, only emptiness remains...",
          "You step into a forgotten ancient battlefield...",
        ]
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
    slimsearch: {
      indexContent: true,
      queryHistoryCount: 20,
      resultHistoryCount: 20,
    }
  },

  markdown: {
    highlighter: getHighlighterOptions(),
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
