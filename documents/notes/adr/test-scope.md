# 重构测试作用域方法，提取到 `ChronicleTestScope`

> - 模块：测试 > 通用
> - 版本：v3.0.1
> - 日期：2026-07-25
> - 状态：已实施

说明：
- 将原本位于 `src/test/kotlin/icu/windea/pls/test/_scopeExtensions.kt` 中分散的，带上下文参数的扩展方法，以及 `icu.windea.pls.test.HighlightingTestScope` 中的方法，全部统一移到 `icu.windea.pls.test.ChronicleTestScope`。
- 同时也补充初步的文档注释。

目的：
- 为了与其他地方的代码风格保持一致（如 `CwtConfigResolutionScope` `ParadoxSelectScope`）。
- 为了统一标准，以及减少显式导入语句。如果必要，直接让测试类实现 `ChronicleTestScope` 即可。

相关代码位置：
- [x] `icu.windea.pls.test.ChronicleTestScope`
- [x] `icu.windea.pls.test.chronicle.ChronicleHelloWorldTest`
