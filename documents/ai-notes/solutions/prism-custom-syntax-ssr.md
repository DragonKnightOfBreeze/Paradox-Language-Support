# Prism 自定义语法（SSR 扩展，避免 window.Prism 依赖）

> - 模块：前端（参考文档）

- **背景**
  - 文档中的代码块需要自定义高亮：**CWT config file**、Paradox Script、Paradox Localisation、Paradox CSV。
  - VuePress 2 使用 `@vuepress/plugin-prismjs` 在 SSR 阶段完成高亮，不会在浏览器端暴露 `window.Prism`。
  - 结论：不能在 `client.ts` 中通过 `window.Prism` 注册语言，需在 SSR 阶段注册。

- **落地方案**
  - 将自定义 Prism 语言定义拆分为可复用的纯 JS 文件，放置于 `docs/.vuepress/highlighters/`：
    - `prism-cwt.js` → `registerCwt(Prism)`
    - `prism-paradox-script.js` → `registerParadoxScript(Prism)`
    - `prism-paradox-localisation.js` → `registerParadoxLocalisation(Prism)`
    - `prism-paradox-csv.js` → `registerParadoxCsv(Prism)`
    - 以上脚本均支持浏览器直引（检测 `window.Prism` 自动注册），也支持模块化调用。
  - 为每种语言创建 VuePress 插件（SSR 阶段注册语言），放置于 `docs/.vuepress/plugins/`：
    - `prism-cwt-plugin.ts`
    - `prism-paradox-script-plugin.ts`
    - `prism-paradox-localisation-plugin.ts`
    - `prism-paradox-csv-plugin.ts`
    - 插件在其 `extendsMarkdown()` 中执行 `register*(Prism)`，直接修改由 `prismjs` 导入的同一 Prism 实例。
  - 在 `docs/.vuepress/config.ts` 中：
    - **显式启用** `@vuepress/plugin-prismjs`。
    - 注册以上 4 个自定义插件。
  - 在 `docs/.vuepress/client.ts` 中：
    - **移除** 一切对 `window.Prism` 的访问和客户端注册逻辑，仅保留组件注册。

- **语言标识（Markdown 代码块围栏）**
  - 使用以下语言键与定义保持一致：
    - `cwt`
    - `paradox_script`
    - `paradox_localisation`
    - `paradox_csv`
  - 文档中已按以上键编写，匹配无误（`docs/zh/*.md`、`docs/en/*.md`）。

- **目录与关键文件**
  - 语言定义脚本：`docs/.vuepress/highlighters/prism-*.js`
  - VuePress 插件：`docs/.vuepress/plugins/*.ts`
  - 配置：`docs/.vuepress/config.ts`
  - 客户端配置：`docs/.vuepress/client.ts`

- **常见问题与排查**
  - **未生效/无高亮**：检查语言键是否拼写一致；确认插件已在 `config.ts` 的 `plugins` 中启用。
  - **构建时报错找不到语言**：确保在 SSR 插件中已调用相应 `register*(Prism)`；不要依赖 `window.Prism`。
  - **顺序问题**：SSR 渲染前，所有插件的 `extendsMarkdown()` 都会执行；保证已在注册阶段修改了 `Prism.languages`。

- **验证**
  - 执行：
    ```powershell
    pnpm.cmd run docs:build
    ```
  - 预期构建成功，生成页面中 `cwt`、`paradox_script`、`paradox_localisation`、`paradox_csv` 代码块具备高亮。
