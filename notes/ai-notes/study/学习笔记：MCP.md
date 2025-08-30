# 学习笔记：MCP（Model Context Protocol）

> MCP 是一套让「AI 应用（客户端）」与「外部数据/工具（服务端）」以统一协议对接的标准。你可以把它理解为 AI 时代的“USB-C”：统一接入、能力协商、可移植。

- **官方站点**：https://modelcontextprotocol.io/
- **TS SDK（npm）**：@modelcontextprotocol/sdk
- **Python SDK（pip/uv）**：mcp（可选 extras：cli）

---

## 核心概念速览

- **参与者（Participants）**：`Host`、`Client`、`Server`
  - `Server` 暴露能力；`Client` 连接并调用；`Host` 作为宿主协调会话、权限与上下文。
- **能力构件（Building Blocks）**：
  - **Tools（工具）**：可调用的动作（如“查询数据库”、“发起 HTTP 请求”）。
  - **Resources（资源）**：结构化/非结构化上下文数据（支持订阅/模板）。
  - **Prompts（提示）**：标准化可复用的提示模板。
  - 进阶：`Sampling`、`Elicitation`、`Roots` 等。
- **传输层（Transports）**：
  - `stdio`（本地/命令行集成）。
  - `Streamable HTTP`（远端/浏览器友好，支持会话与 SSE 通知）。
- **协议消息**：JSON-RPC 2.0 扩展（请求、响应、通知），含初始化、工具调用、资源列表/读取、提示列表/获取等。

---

## TypeScript 最小服务端（stdio）

下面示例基于官方 TS SDK，注册一个最小的 Echo 能力集（Tool/Resource/Prompt），并通过 `stdio` 传输层对接到 MCP 客户端（如 Claude Desktop 或 Inspector）。

### 1) 初始化项目

```bash
# 新建目录并初始化
mkdir mcp-ts-echo && cd mcp-ts-echo
npm init -y

# 依赖：TS SDK 与 zod（用于输入校验，可按需替换）
npm i @modelcontextprotocol/sdk zod

# 使用 ESM（重要）：以支持 import 语法
npm pkg set type=module
```

### 2) 编写 `server.js`

```js
import { McpServer, ResourceTemplate } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";

const server = new McpServer({ name: "echo-server", version: "1.0.0" });

// Resource：通过 URI 模板暴露回显数据
overrideResource();
function overrideResource() {
  server.registerResource(
    "echo",
    new ResourceTemplate("echo://{message}", { list: undefined }),
    { title: "Echo Resource", description: "Echoes back messages as resources" },
    async (uri, { message }) => ({ contents: [{ uri: uri.href, text: `Resource echo: ${message}` }] })
  );
}

// Tool：回显入参
server.registerTool(
  "echo",
  {
    title: "Echo Tool",
    description: "Echoes back the provided message",
    inputSchema: { message: z.string() },
  },
  async ({ message }) => ({ content: [{ type: "text", text: `Tool echo: ${message}` }] })
);

// Prompt：生成一个简单的交互模板
server.registerPrompt(
  "echo",
  {
    title: "Echo Prompt",
    description: "Creates a prompt to process a message",
    argsSchema: { message: z.string() },
  },
  ({ message }) => ({
    messages: [
      { role: "user", content: { type: "text", text: `Please process this message: ${message}` } },
    ],
  })
);

// 连接 stdio 传输层（等待 MCP 客户端对接）
const transport = new StdioServerTransport();
await server.connect(transport);
```

### 3) 运行（等待 MCP 客户端连接）

```bash
node server.js
```

- **说明**：`stdio` 服务器本身不会主动输出业务日志，它等待 MCP 客户端发起初始化/调用。你可以用 Claude Desktop 或 MCP Inspector 连接测试。

---

## TypeScript 远程服务端（Streamable HTTP）要点

- 使用 `StreamableHTTPServerTransport` 可通过 HTTP 对外服务，同时用 SSE 下行通知；支持可选的「会话管理」。
- 会话场景下：
  - 客户端初始化时生成 `Mcp-Session-Id`（响应头/或用 `mcp-session-id` 请求头传递）。
  - 需要在 CORS 中允许并暴露 `Mcp-Session-Id`，且允许请求头 `mcp-session-id`。
- 安全项：可启用 DNS 重绑定防护（`enableDnsRebindingProtection` 与 `allowedHosts`）。

> 提示：远程 HTTP 的最小样例可参考官方 npm 文档中的 Express 片段；生产环境务必收紧 CORS 和 allowedHosts。

---

## Python 最小服务端（FastMCP）

Python SDK 推荐使用 `FastMCP` 快速注册工具/资源，并提供 `mcp` CLI 的开发模式与运行模式（需安装 `mcp[cli]`）。

### 1) 安装依赖（推荐 uv）

```powershell
# 新建并进入项目
uv init mcp-py-echo
cd mcp-py-echo

# 添加 SDK 与 CLI
uv add "mcp[cli]"
```

如使用 pip：

```powershell
pip install "mcp[cli]"
```

### 2) 编写 `server.py`

```python
from mcp.server.fastmcp import FastMCP

mcp = FastMCP("Demo")

@mcp.tool()
def hello(name: str = "World") -> str:
    """Say hello to someone."""
    return f"Hello, {name}!"

if __name__ == "__main__":
    # 直接执行（高级场景可考虑流式 HTTP，参见官方文档）
    mcp.run()
```

### 3) 开发/调试（MCP Inspector）

```powershell
# 开发模式：启动 Inspector 并加载你的 Server（支持 --with、--with-editable）
uv run mcp dev server.py
```

### 4) 直接运行（FastMCP）

```powershell
# 直接运行（需 FastMCP；低层版本不受此命令支持）
uv run mcp run server.py

# 或 Python 直接启动（等价于 __main__ 的 mcp.run()）
python server.py
```

---

## 调试与开发工具

- **MCP Inspector（推荐）**：
  - 快速联调、查看资源/提示/工具、查看通知日志。
  - 在 Python 项目中可通过 `uv run mcp dev server.py` 启动；TS 项目可使用远端 HTTP + 浏览器客户端或桌面端客户端连接。
- **Claude Desktop 集成**：
  - 通过 `uv run mcp install server.py` 安装本地 Server，支持 `--name`、`-v KEY=VAL`、`-f .env` 等。

```powershell
# 典型安装命令（Python/FastMCP）
uv run mcp install server.py --name "My Analytics Server" -f .env
```

---

## 安全与合规要点

- **最小权限**：工具要最小化作用域，谨慎传递凭据（防止 Token Passthrough）。
- **Confused Deputy**：为不同上游客户端区分权限/审计轨迹，避免“被借权”。
- **用户批准**：对具副作用的工具调用启用显式同意（Host/Client 层）。
- **网络安全**：
  - 远端 HTTP：
    - 暴露并允许 `Mcp-Session-Id`/`mcp-session-id` 头（浏览器客户端读取会话）。
    - 启用并配置 `enableDnsRebindingProtection` 与 `allowedHosts`。
    - CORS 生产配置要细化 `origin`、`allowedHeaders`、`exposedHeaders`。
- **日志与机密**：避免在日志中输出敏感信息。

---

## 常见问题（FAQ）

- **Bad Request: No valid session ID provided**
  - 未提供 `mcp-session-id` 请求头或未正确暴露 `Mcp-Session-Id` 响应头；会话未建立或丢失。
- **浏览器无法读取会话 ID**
  - CORS 未暴露 `Mcp-Session-Id`；请在服务端 `exposedHeaders` 添加并允许 `mcp-session-id` 请求头。
- **Node 导入失败**
  - 确保 `package.json` 设置 `"type": "module"`；导入路径形如 `@modelcontextprotocol/sdk/server/mcp.js`。
- **Python 无法找到 mcp CLI**
  - 确认已安装 `mcp[cli]`，并用 `uv run mcp ...` 或在虚拟环境中运行。

---

## 参考链接

- **TypeScript SDK（npm）**：https://www.npmjs.com/package/@modelcontextprotocol/sdk
- **TypeScript SDK（GitHub）**：https://github.com/modelcontextprotocol/typescript-sdk
- **Python SDK（GitHub）**：https://github.com/modelcontextprotocol/python-sdk
- **官方规范与示例**：
  - Transports（Streamable HTTP、stdio）：https://modelcontextprotocol.io/specification/2025-03-26/basic/transports
  - Example Servers & Inspector：官方站点导航
