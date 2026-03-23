<!-- Context: project-intelligence/nav | Priority: critical | Version: 1.0 | Updated: 2026-03-23 -->

# Project Intelligence

> 本目录存放 Paradox Language Support 插件的项目专属智能上下文。
> AI 代理应在执行任务前优先加载相关文件。

## 结构

```
.opencode/context/project-intelligence/
├── navigation.md          # 本文件 - 快速导航
└── technical-domain.md    # 技术栈、架构、核心开发模式
```

## Quick Routes

| 需要了解 | 文件 | 说明 |
|----------|------|------|
| 技术栈与架构 | `technical-domain.md` | 主要技术、项目结构、核心 API |
| 核心代码模式 | `technical-domain.md` | Manager/Service 模式、缓存、命名规范 |
| 代码规范 | `technical-domain.md` | 索引策略、缓存策略、注释规范 |
| 安全与稳定性 | `technical-domain.md` | 注入子系统、IDE 工具使用注意事项 |

## 与全局上下文的关系

本目录为**项目本地**上下文（`.opencode/`），优先级高于全局上下文（`~/.config/opencode/`）。

全局通用规范仍适用：
- `~/.config/opencode/context/core/standards/code-quality.md`
- `~/.config/opencode/context/core/standards/test-coverage.md`
- `~/.config/opencode/context/core/workflows/code-review.md`
