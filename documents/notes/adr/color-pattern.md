# 颜色处理

> 所属模块：`后端 > 通用`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.4
> - 日期：2026-09-30
> - 状态：已实施

说明：
- 作为颜色支持的基础设施，插件目前提供了 `ColorService`，用于处理颜色参数，包括验证有效性和合法性，以及转换为颜色对象，等等。
- 存在不同的颜色类型（hex/rgb/hsv/hsv360），每种颜色类型都需要提供对应的处理方法，这并不怎么优雅。
- 因此，考虑到已存在 `QuotePattern` 和 `EscapePattern`，可以同样引入 `ColorPatten`。
- 这同样是与文本相关的，只是颜色参数可能是一段文本，也可能是一组文本。
- 除了必要的处理方法之外，还可以提供 `name` 字段，以避免硬编码的颜色类型字符串（如 `"rgb"`）。

相关代码位置：
- [x] `icu.windea.pls.core.text.ColorPattern`
- [x] `icu.windea.pls.core.text.ColorPatterns`
- [x] ~~`icu.windea.pls.core.ui.ColorService`~~ - 移除
