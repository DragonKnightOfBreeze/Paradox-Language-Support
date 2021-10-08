const renderer = {
  //渲染rowspan和colspan
  tablecell(content, flags) {
    if(content === "^") {
      return `<td class="rowspan"></td>`
    } else if(content === "&lt;") { //不是"<"
      return `<td class="colspan"></td>`
    } else {
      return `<td>${content}</td>`
    }
  }
}

window.onload = function() {
  redirectLocation()
  bindDeviceCssClass()
}

window.$docsify.filePath = ""
window.$docsify.fileUrl = ""
window.$docsify.isMobile = false

window.$docsify.markdown = {
  renderer: renderer
}

window.$docsify.plugins = [
  function(hook, vm) {
    hook.init(function() {
      redirectLocation()
      bindDeviceCssClass()
    })
    hook.beforeEach(function(html) {
      //绑定window.$docsify.filePath
      window.$docsify.filePath = vm.route.file
      //绑定windows.$docsify.fileUrl
      window.$docsify.fileUrl = vm.route.path

      //预处理markdown
      let isCodeFence = false
      html = html.split("```").map(snippet =>{
        if(isCodeFence){
          isCodeFence = false
          return snippet
        }else{
          isCodeFence = true
          snippet = escapeInCode(snippet)
          snippet = resolveAnchor(snippet)
          snippet = resolveFootNote(snippet)
          return snippet
        }
      }).join("```")
      return html
    })
    hook.afterEach(function(html, next) {
      next(html)
    })
    hook.doneEach(function() {
      $(document).ready(function() {
        bindFootNote()
      })
    })
  }
]

//地址重定向
function redirectLocation() {
  let latestVersion = window.$docsify.version
  let latestVersionSuffix = latestVersion ? latestVersion + "/" : ""
  let locale = inferLocale()
  let url = window.location.href
  if(url.charAt(url.length - 1) === "/") url = url.substring(0, url.length - 1)
  if(url.indexOf("/#") === -1) {
    window.location.replace(`${url}/#/${locale}/${latestVersionSuffix}`)
  } else if(url.endsWith("/#")) {
    window.location.replace(`${url}/${locale}/${latestVersionSuffix}`)
  } else{
    const locales = window.$docsify.locales
    locales.forEach(it=>{
      if(url.endsWith(`/#/${it}`)){
        window.location.replace(`${url}/${latestVersionSuffix}`)
      }else if(url.endsWith(`/#/${it}/latest`)){
        window.location.replace(`${url.substring(0, url.length - 7)}/${latestVersionSuffix}`)
      }
    })
  }
}

//推断语言区域
function inferLocale() {
  const locales = window.$docsify.locales
  const locale = navigator.language
  locales.forEach(it =>{
    if(locale.startsWith(it)) return it
  })
  return "zh"
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
  return html.replace(/^[ \t]*\|(.*)\|[ \t]*$/gm, (s, content) => {
    return "|" + content.replace(/`([^`]+)`/g, (ss, c) => {
      return "`" + c.replace("|", "\\|") + "`"
    }) + "|"
  })
}

//解析markdown锚点，绑定heading的id
function resolveAnchor(html) {
  return html.replace(/(.*?){#(.+?)}/g, (s, prefix, id) => {
    if(prefix.startsWith("#")) return `${prefix} :id=${id}`
    else return `${prefix}<span id="${id}"></span>`
  })
}

//解析markdown尾注，生成bootstrap4的tooltip
function resolveFootNote(html) {
  const footNotes = {}
  return html.replace(/^\[\^(\d+)]:\s*(.*)$/gm, (s, id, text) => {
    footNotes[id] = text
    return ""
  }).replace(/\[\^(\d+)](?!: )/g, (s, id) => {
    return `<a href="javascript:void(0);" data-toggle="tooltip" title="${footNotes[id]}">[${id}]</a>`
  })
}

//绑定footNote对应的tooltip
function bindFootNote() {
  $('[data-toggle="tooltip"]').tooltip()
}
