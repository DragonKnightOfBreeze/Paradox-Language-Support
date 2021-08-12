window.$docsify = {
  name: "Paradox-Language-Support",
  locales: ["zh"],
  repo: "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support",
  routeMode: "history",
  relativePath: true,
  auto2top: true,
  fallbackLanguages: ["zh"],
  loadSidebar: true,
  loadNavbar: true,
  mergeNavbar: false,
  maxLevel: 4,
  subMaxLevel: 4,
  notFoundPage: true,
  topMargin: 80,

  search: {
    noData: {
      "/zh/": "没有结果！",
      "/en/": "No results!",
      "/": "没有结果！"
    },
    path: "auto",
    placeholder: {
      "/zh/": "搜索文档",
      "/en/": "Search Document",
      "/": "搜索文档"
    }
  },
  pagination: {
    previousText: "Prev",
    nextText: "Next",
    crossChapter: true,
    crossChapterText: true
  },
  copyCode: {
    buttonText: "Copy Code",
    errorText: "Error",
    successText: "Copied"
  },
  "flexible-alerts": {
    style: 'callout', //flat, callout
    note: {
      label: {
        "/zh/": "注意",
        "/en/": "Note",
        "/": "注意"
      }
    },
    tip: {
      label: {
        "/zh/": "提示",
        "/en/": "Tip",
        "/": "提示"
      }
    },
    warning: {
      label: {
        "/zh/": "警告",
        "/en/": "Warning",
        "/": "警告"
      }
    },
    info: {
      label: {
        "/zh/": "说明",
        "/en/": "Information",
        "/": "说明"
      },
      icon: "fa fa-info-circle",
      className: "info"
    },
  }
}
