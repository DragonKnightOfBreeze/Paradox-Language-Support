# 学习笔记：LSP（语言服务协议）

> 目标：梳理与具体语言无关的 LSP 通用知识与工程实践，并给出在 JetBrains IDE 与 VS Code 的集成思路。

---

## 核心概念与范围

- **角色与通信**：Client（编辑器/IDE）与 Server（语言服务）通过 JSON-RPC 通信，常见传输为 stdio、TCP、WebSocket。
- **生命周期**：`initialize` -> `initialized` -> 正常交互 -> `shutdown` -> `exit`。
- **能力协商**：Client/Server 在初始化时交换 capabilities，按需开启功能。
- **常用方法族**：
  - 诊断：`textDocument/publishDiagnostics`
  - 补全：`textDocument/completion`
  - 悬浮：`textDocument/hover`
  - 跳转/查找：`textDocument/definition`、`textDocument/references`
  - 代码操作：`textDocument/codeAction`、`workspace/executeCommand`
  - 语义高亮：`textDocument/semanticTokens/*`
- **文件同步**：`didOpen`/`didChange`/`didClose`。推荐使用增量变更（`TextDocumentSyncKind.Incremental`）。

---

## 最小实现（伪代码）

```pseudo
main():
  server = LspServer()
  server.onInitialize(req):
    return { capabilities: { completionProvider: {}, hoverProvider: true, ... } }
  server.onDidOpen(params):
    analyze(params.text)
  server.onDidChange(params):
    updateDiagnostics(params.changes)
  server.onCompletion(params):
    return items
  server.onHover(params):
    return { contents: MarkdownString("...") }
  server.listen(stdio)
```

---

## JetBrains IDE 集成（两种路径）

- **路径 A：JetBrains 平台 LSP API**
  - 适用：已有/第三方 LSP Server，希望在 IntelliJ 系列 IDE 中直接复用。
  - 思路：在插件中注册 LSP 集成，按文件类型/工程条件启动/复用 LSP 进程，映射诊断、补全、跳转等能力到 IDE。
  - 注意：JetBrains 原生 PSI/索引体系功能更强，如已实现原生 PSI 插件，可选择混合方案（部分能力走 LSP）。
- **路径 B：第三方库/插件（例如 lsp4intellij 等）**
  - 适用：旧版本 IDE 或需要更灵活的桥接。
  - 思路：在编辑器打开时启动 LSP、维护文档同步、将 LSP 响应转换为 IntelliJ 的 UI/动作。
- **通用建议**
  - 进程管理：复用 server、健康检查、崩溃自动重启、日志上下文。
  - 文件映射：确保虚拟文件到真实路径/Uri 的一致性。
  - 性能：批量诊断合并、去抖动变更、超时与取消、懒加载 capabilities。

---

## VS Code 集成（vscode-languageclient）

- **依赖**：`vscode-languageclient`（Node）。
- **最小客户端示例（TypeScript）**：
```ts
import { ExtensionContext, workspace } from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient/node';

let client: LanguageClient;

export function activate(ctx: ExtensionContext) {
  const serverOptions: ServerOptions = {
    run:   { command: 'my-lsp' },
    debug: { command: 'my-lsp', args: ['--debug'] },
  };
  const clientOptions: LanguageClientOptions = {
    documentSelector: [{ scheme: 'file', language: 'cwt' }],
    synchronize: { fileEvents: workspace.createFileSystemWatcher('**/*.cwt') },
  };
  client = new LanguageClient('pls-lsp', 'PLS Language Server', serverOptions, clientOptions);
  client.start();
}

export function deactivate() { return client?.stop(); }
```

---

## 能力矩阵与范围裁剪

- **必须优先**：Diagnostics、Hover、Go to Definition、Completion、References。
- **按需开启**：Code Action、Rename、Formatting、Semantic Tokens、Folding。
- **范围裁剪**：避免一次性实现全部方法；以“可验证价值 + 覆盖路径”为优先。

---

## 性能与调试

- **缓存与增量**：对 AST/索引/符号表构建做增量更新；热点路径打点与剖析。
- **并发与取消**：对长耗时请求支持取消；队列化同一文档的变更。
- **日志与回放**：保留请求/响应样本；构建最小复现用例。

---

## 与本项目的关联（Paradox Language Support）

- **跨编辑器覆盖**：在 JetBrains 平台已有 PSI/索引实现的基础上，提供一个 LSP Server 给 VS Code/NeoVim 等使用。
- **混合架构**：在 IntelliJ 内可选择通过 LSP 复用已有 Server 的部分能力（如诊断/补全），降低二次实现成本。
- **规范沉淀**：以 CWT 规则与 Paradox 脚本语义为中心，统一语义层，再分别适配 PSI 与 LSP。

---

## 可扩展内容

- **传输与部署**：stdio/TCP/WebSocket 的取舍与隧道化；跨平台打包与更新。
- **增量解析**：基于文本差量的 AST 增量维护与二阶段校验。
- **索引与引用**：跨文件符号表、前向声明、跨包/模组的引用跟踪。
- **测试与基准**：LSP 交互用例库、回放与性能基线、协议兼容回归。
- **与 PSI 融合**：在 IntelliJ 中将 LSP 诊断与 QuickFix 映射到 Intentions/Inspections。

---

## 参考链接

- **Language Server Protocol**：https://microsoft.github.io/language-server-protocol/
- **VS Code Language Client**：https://github.com/microsoft/vscode-languageserver-node
- **lsp4j（Java LSP）**：https://github.com/eclipse/lsp4j
- **JetBrains LSP（Platform LSP）文档**：https://plugins.jetbrains.com/docs/intellij/lsp.html
