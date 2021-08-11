const columnLineRegex = /[ \t]*\|(.*)\|[ \t]*/g
const codeRegex = /(`[^`\r\n]+`)/g
const anchorRegex = /([^\r\n]*?){#([^\r\n}]+)}/g
const footNoteRegex = /\[\^(\d+)](?!: )/g
const footNoteReferenceRegex = /^\[\^(\d+)]:\s*(.*)$/gm

window.$docsify = {
  name: "Paradox-Language-Support",
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
      "/en/": "No results!"
    },
    path: "auto",
    placeholder: {
      "/zh/": "搜索文档",
      "/en/": "Search Document"
    }
  },
  copyCode: {
    buttonText: 'Copy Code',
    errorText: 'Error',
    successText: 'Copied'
  },
  "flexible-alerts": {
    style: 'callout', //flat, callout
    note: {
      label: {
        "/zh/": "注意",
        "/en/": "Note"
      }
    },
    tip: {
      label: {
        "/zh/": "提示",
        "/en/": "Tip"
      }
    },
    warning: {
      label: {
        "/zh/": "警告",
        "/en/": "Warning"
      }
    },
    info: {
      label: {
        "/zh/": "说明",
        "/en/": "Information"
      },
      icon: "fa fa-info-circle",
      className: "info"
    }
  },
  pagination: {
    previousText: "Prev",
    nextText: "Next",
    crossChapter: true,
    crossChapterText: true
  },

  markdown: {
    renderer: {
      //渲染rowspan和colspan
      tablecell(content, flags) {
        if(content === "^") {
          return `<td class="rowspan"></td>`
        } else if(content === "<"){
          return `<td class="colspan"></td>>`
        } else {
          return `<td>${content}</td>`
        }
      }
    }
  },

  plugins: [
    function(hook, vm) {
      hook.init(function() {
        redirectLocation()
        bindDeviceCssClass()
      })

      hook.beforeEach(function(html) {
        bindVariables(vm)

        html = escapeInCode(html)
        html = resolveAnchor(html)
        html = resolveFootNote(html)
        return html
      })
      hook.afterEach(function(html, next) {
        next(html)
      });
      hook.doneEach(function() {
        $(document).ready(function() {
          bindFootNote()
        })
      })
    }
  ],

  fileName: "",
  fileUrl: "",
  isMobile: false
}

window.onload = function() {
  redirectLocation()
  bindDeviceCssClass()
}

//推断语言区域
function inferLocale() {
  const locale = navigator.language
  if(locale.startsWith("zh")) {
    return "zh"
  } else if(locale.startsWith("en")) {
    return "en"
  } else {
    return "zh"
  }
}

//地址重定向
function redirectLocation() {
  let url = window.location.href
  if(url.charAt(url.length - 1) === "/") url = url.substring(0, url.length - 1)
  if(url.indexOf("/#") === -1) {
    window.location.replace(`${url}/#/${inferLocale()}/`)
  } else if(url.endsWith("/#")) {
    window.location.replace(`${url}/${inferLocale()}/`)
  } else if(url.endsWith("/#/zh")) {
    window.location.replace(`${url}/`)
  } else if(url.endsWith("/#/en")) {
    window.location.replace(`${url}/`)
  }
}

//绑定自定义变量
function bindVariables(vm) {
  //绑定window.$docsify.fileName，以斜线开始
  window.$docsify.fileName = `/${vm.route.file}`
  //绑定windows.$docsify.fileUrl，以#开始，没有文件后缀名
  window.$docsify.fileUrl = `#/${vm.route.path}`
}

//绑定判断设备的css class
function bindDeviceCssClass() {
  const isMobile = /ipad|iphone|ipod|android|blackberry|windows phone|opera mini|silk/i.test(navigator.userAgent)
  const bodyElement = document.querySelector("body")
  if(isMobile) {
    bodyElement.classList.add("mobile")
    window.$docsify.isMobile = true
  } else {
    bodyElement.classList.add("web")
  }
}

//需要转义表格单元格中的内联代码中的管道符
function escapeInCode(html) {
  return html.replace(columnLineRegex,(s,content)=>{
    return content.replace(codeRegex,(ss,c)=>{
      return c.replace("|","\\|")
    })
  })
}

//解析markdown锚点，绑定heading的id
function resolveAnchor(html) {
  return html.replace(anchorRegex, (s, prefix, id) => {
    if(prefix.startsWith("#")) return `${prefix} :id=${id}`
    else return `${prefix}<span id="${id}"></span>`
  })
}

//解析markdown尾注，生成bootstrap4的tooltip
function resolveFootNote(html) {
  const footNotes = {}
  return html.replace(footNoteReferenceRegex, (s, id, text) => {
    footNotes[id] = text
    return ""
  }).replace(footNoteRegex, (s, id) => {
    return `<a href="javascript:void(0);" data-toggle="tooltip" title="${footNotes[id]}">[${id}]</a>`
  })
}

//绑定footNote对应的tooltip
function bindFootNote() {
  $('[data-toggle="tooltip"]').tooltip()
}
