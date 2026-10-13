# 问题解决与避坑手册 (Solutions & Troubleshooting)

## 概述 / Overview

本目录记录开发途中发现的、需要进一步研究与沉淀的各类问题排查技巧、构建配置方案与规避指南。这些问题通常不被视为明确的插件 Bug，而是外部环境、第三方库或特定 DSL 的技术陷阱与应对方案。

---

This directory contains troubleshooting guides, environment workarounds, and implementation solutions for common pitfalls encountered during development.

## 目录索引 / Catalog

### 构建

- [依赖处理](dependency-handling.md)

### 前端（参考文档）

- [如何为 gradlew 配置代理](gradle-proxy-setup.md)

### 后端（语法支持）

- [textMate JSON 文件中的正则匹配的问题](textmate-regex-matching.md)

### 后端

- [关于 JFLEX 优先级规则](jflex-priority-rules.md)
- [确保能读取到最新的内置规则文件](ensure-latest-bundled-configs.md)
- [规则数据可能会被不定期清空的问题](config-cache-eviction.md)
- [jackson 解析 json 失败的问题](jackson-json-parsing-failure.md)
- [对于 non-declarative 的内嵌提示，使用 `psiSingleReference` 不会自动更改配色](inlay-hint-psi-single-reference-colors.md)

### 测试

- [对于对应多个 PSI 引用的脚本表达式（如 `religion:catholic`），光标位置的用法高亮可能不正确](multi-psi-reference-highlighting.md)
- [按条件忽略的测试被不正确地忽略了](conditional-tests-ignored.md)
- [需要在单元测试（集成测试）中加载并获取规则数据](load-configs-in-tests.md)
- [测试代码补全时的注意事项](testing-code-completion-notes.md)

