# 重构 JSON 服务，提供统一入口，额外兼容 UTF-8 BOM

> - 模块：后端 > 通用
> - 版本：v3.0.5
> - 日期：2026-11-3 ~ 2026-11-3
> - 状态：已完成（告一段落，存在后续任务）
> - 相关问题：[#438](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/438)

## 说明

使用 kotlinx-serialization 解析 JSON 文件/文本时，应额外兼容 UTF-8 BOM。

插件在 v3.0.4 中，将使用的 JSON 序列化框架从 jackson 迁移到了 kotlinx-serialization（参见：[replace-jackson-with-kotlinx-serialization.md](replace-jackson-with-kotlinx-serialization.md)）。
当时的主要目的是为了解决平台版本兼容性问题，以及为插件的未来版本和愿景做准备。
然而，kotlinx-serialization 与 jackson 在约定和实现细节上存在一些区别，比如这次遇到的这个 UTF-8 BOM 的坑……

这一边界情况要求重构相当代码，尤其是 `JsonService`。

## 笔记

### 概述

这个问题的解决方案是相当简单的，如何决策则要更加复杂和困难一些。

目前的决策：
- 重构 `JsonService`，提供 read/write 方法，作为读写 JSON 的统一入口。
- 读取时统一兼容 UTF-8 BOM（参见 `InputStream.skipUtf8Bom`）。
- 写入时统一使用不包含 BOM 的 UTF-8 编码。
- 可以通过指定 `json5 = true` 来使用 JSON5 模式（更加宽松，允许注释和尾随逗号）。

待评估：
- 进一步重构 `JsonService`，对于其中的读写方法，可以传入 `JsonOptions` 来控制额外的处理逻辑，并且提供默认选项。

### 来自 Gemini

Jackson 底层的 `ByteSourceJsonBootstrapper` 在读取 `InputStream` 时会自动探测并吞掉开头的 `0xEF 0xBB 0xBF`，但 kotlinx-serialization 在这方面有着近乎偏执的“严格遵循 RFC 8259 标准”（标准规定 JSON 文本不能带 BOM，因此它遇到 `\uFEFF` 会直接炸 `JsonDecodingException`）。偏偏在 Paradox 的生态和 Windows 环境下，带 BOM 的 UTF-8 文件到处都是。

## 任务

### 主要

- [x] 需要重构 `JsonService`，提供 read/write 方法，作为读写 JSON 的统一入口。
- [x] 需要提供 `icu.windea.pls.core.util.io.SkipInputStream`（类似 `PushbackStream`，但可以直接指定 byteArray）。
- [x] 需要在 `IoStreams.kt` 中提供 `InputStream.skip(ByteArray)` 和 `InputStream.skipUtf8Bom()`
- [x] 需要清理和补充测试（`JsonServiceTest`，等等）
- [x] （人工）需要补充 ADR（`documents/notes/adr/json-service-refactoring.md`）
- [x] （人工）需要更新 `guidelines.md` `AGENTS.md` 中的相关章节（`core` 模块：JSON 处理、匹配器、递归守卫、缓存、Key/UserDataHolder、等等）

### 评估

- [ ] P3 重构：优化 `JsonService`：新增 `JsonOptions` 并应用
  - 这里的选项应能在合适的范围内控制要使用的底层实现（参见 `Json`），并且可以控制额外的处理逻辑（如对 utf8 bom 的处理）
  - 提供 `DEFAULT` `JSON5` 等默认选项
- [ ] P3 重构：优化 `MarkdownService`：新增 `MarkdownOptions` 并应用
  - 这里的选项应能在合适的范围内控制要使用的底层实现（参见 `MarkdownFlavourDescriptor`），并且可以控制额外的处理逻辑
  - 提供 `GFM` `CommonMark` 等默认选项

## 参见

### 相关代码位置

- [x] `icu.windea.pls.core.data.JsonService`