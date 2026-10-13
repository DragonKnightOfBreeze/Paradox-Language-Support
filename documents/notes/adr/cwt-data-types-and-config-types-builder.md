# 重构 `CwtDataTypes` `CwtConfigTypes` - 改为偏好使用 lambda 式构建器

> 所属模块：`后端 > 通用`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v2.1.8
> - 日期：2026-05-04
> - 状态：已实施

说明：
- 改为偏好使用 lambda 式构建器，而非多行的链式构建器。
- `.reference()` 这样的“重要信息”需要紧随 `builder()` 之后，其余细节信息放到 `builder {...}` 中。
- 如果没有细节信息，直接使用单行的链式构建器即可。

目的：
- 为了适用代码折叠，从而隐藏细节，方便查看。

相关代码位置：
- `icu.windea.pls.config.CwtConfigTypes`
- `icu.windea.pls.config.CwtDataTypes`
