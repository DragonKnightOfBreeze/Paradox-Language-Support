# VuePress docs:dev 启动失败（Vite 7 与 Node 版本不兼容，crypto.hash is not a function）

> - 模块：前端（参考文档）

- **背景**
  - 使用 VuePress 2 搭建文档，`docs/package.json` 主要版本：
    - `vuepress@2.0.0-rc.25`
    - `@vuepress/bundler-vite@2.0.0-rc.25`
    - `vuepress-theme-hope@2.0.0-rc.94`
    - 一些官方插件为 `2.0.0-rc.112`
  - 先前为修复 `TemplateRendererOutlet` 导出错误，已将 VuePress 核心统一到 `rc.25`，并显式使用 `viteBundler()`。

- **现象**
  - 启动开发服务器：
    ```powershell
    pnpm.cmd run docs:dev
    ```
  - 报错（摘要）：
    ```text
    TypeError: crypto.hash is not a function
        at getHash (.../docs/node_modules/.pnpm/vite@7.1.3.../vite/dist/node/chunks/dep-*.js:2649:21)
    ```

- **排查过程**
  - 查看 Vite 产物 `dep-*.js`，`getHash()` 使用 `crypto.hash("sha256", ...)`，你的当前 Node 版本为 `v20.10.0`，并不提供该 API。
  - 查看 `vite@7.1.3` 的 `package.json`：
    - `"engines": { "node": "^20.19.0 || >=22.12.0" }`
    - 结论：需要 Node `>=20.19.0`（或 `>=22.12.0`）。
  - 查看 `@vuepress/bundler-vite@2.0.0-rc.25/package.json`：
    - 依赖 `"vite": "~7.1.3"`，意味着 Bundler 锁定 Vite 7，不宜简单降级 Vite。
  - 运行安装日志存在大量 peer dependency 警告（主题/插件多数声明 `vuepress@2.0.0-rc.24`），但不影响当前报错，属于兼容范围提示。

- **结论**
  - 根因：Node 版本过低（`v20.10.0`），不满足 `vite@7.1.3` 的 engines 要求，导致 `crypto.hash` 不存在，从而在 dev 启动阶段抛错。
  - 方向：升级 Node 到满足要求的版本（推荐 `>=22.12.0`，或至少 `20.19.0`）。

- **解决方案（Windows / nvm）**
  - 使用 nvm（已安装 `1.1.12`）升级并切换 Node：
    ```powershell
    nvm install 22.12.0
    nvm use 22.12.0
    node -v     # 确认版本 ≥ v22.12.0
    pnpm.cmd run docs:dev
    ```
  - 若需最低满足要求：
    ```powershell
    nvm install 20.19.0
    nvm use 20.19.0
    node -v
    pnpm.cmd run docs:dev
    ```

- **补充说明**
  - 目前的 peer 警告（如主题/插件期望 `vuepress@rc.24`，而项目为 `rc.25`）非致命，可暂时忽略；`@vuepress/utils@rc.25` 已包含 `TemplateRendererOutlet`，与主题期望一致。
  - 若后续遇到奇怪缓存，可清理（若存在）`docs/node_modules/.vite` 后重试。
  - 在 PowerShell 上建议使用 `pnpm.cmd` 以绕过执行策略限制。

- **经验教训 / Checklist**
  - 先根据堆栈定位到具体包与实现位置，判断是运行时 API 缺失还是导出不匹配。
  - 检查第三方包的 `engines` 字段与本地运行时（Node）是否匹配。
  - 核对 Bundler/框架是否锁定底层工具版本（例如 `@vuepress/bundler-vite` 锁定 Vite 7）。
  - Windows 终端下注意用 `pnpm.cmd`，避免被执行策略阻挡。
