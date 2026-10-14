# 问题解决与避坑手册

## 概述

本目录记录开发途中发现的、需要进一步研究与记录的各类问题排查技巧、构建配置方案与规避指南。通常涉及外部开发环境、第三方库冲突或特定 DSL 的技术陷阱与应对方案。

## 目录索引

### 构建

- [依赖处理](solutions/dependency-handling.md)

### 前端（参考文档）

- [如何为 gradlew 配置代理](solutions/gradle-proxy-setup.md)

### 后端（语法支持）

- [textMate JSON 文件中的正则匹配的问题](solutions/textmate-regex-matching.md)

### 后端

- [关于 JFLEX 优先级规则](solutions/jflex-priority-rules.md)
- [确保能读取到最新的内置规则文件](solutions/ensure-latest-bundled-configs.md)
- [规则数据可能会被不定期清空的问题](solutions/config-cache-eviction.md)
- [jackson 解析 json 失败的问题](solutions/jackson-json-parsing-failure.md)
- [对于 non-declarative 的内嵌提示，使用 `psiSingleReference` 不会自动更改配色](solutions/inlay-hint-psi-single-reference-colors.md)

### 测试

- [对于对应多个 PSI 引用的脚本表达式（如 `religion:catholic`），光标位置的用法高亮可能不正确](solutions/multi-psi-reference-highlighting.md)
- [按条件忽略的测试被不正确地忽略了](solutions/conditional-tests-ignored.md)
- [需要在单元测试（集成测试）中加载并获取规则数据](solutions/load-configs-in-tests.md)
- [测试代码补全时的注意事项](solutions/testing-code-completion-notes.md)
