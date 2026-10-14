# 转义模式与转义处理

> - 模块：后端 > 通用
> - 版本：v3.0.2
> - 日期：2026-08-23
> - 状态：已实施

说明：
- 进一步的对转义处理的相关重构：引入 `EscapePattern`。
- 这是一种“策略模型”类，但目前仅作为对转义逻辑的策略，不提供相关的元数据（如转义前后的特定字符串）。
- 相关的接口代码与实现代码统一移到 `icu.windea.pls.core.text`。
- 在 `icu.windea.pls.core.TextExtensions.kt` 中提供相关的扩展方法。
- 相关的语义层的代码，需要考虑移到语言基础设施层。

目前来说，这里的代码逻辑相对简单，通常只需要处理特殊空白字符（普通形式、转义形式、HTML形式之间的相互转换）。

相关代码位置：
- [x] `icu.windea.pls.core.text.EscapePattern`
- [x] `icu.windea.pls.core.text.EscapePatterns`
