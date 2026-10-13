# 增强对数据类型 `ShaderEffect` 和 `MeshLocator` 的支持

> 所属模块：`后端 > 语言构造支持`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v2.1.9
> - 日期：2026-05-14
> - 状态：已实施

说明：
- `ShaderEffect` - 匹配对着色器效果（shader effect）的引用。目前将这些引用视为动态引用，尽管其声明实际上位于 `.shader` 文件中。
- `MeshLocator` - 匹配对网格定位器（mesh locator）的引用。目前将这些引用视为动态引用，尽管其声明实际上位于 `.mesh` 文件中。
- “动态引用”意味着不存在实际上的声明处，仅区分读写访问，如同动态值一样。
- 对于以上两种数据类型来说，仅存在读访问（即引用）。

相关代码位置：
- [x] `icu.windea.pls.config.CwtDataTypes.ShaderEffect`
- [x] `icu.windea.pls.config.CwtDataTypes.MeshLocator`
- [x] `icu.windea.pls.lang.psi.light.ParadoxShaderEffectLightElement`
- [x] `icu.windea.pls.lang.psi.light.ParadoxMeshLocatorLightElement`
- [x] `icu.windea.pls.ep.resolve.expression.ParadoxScriptShaderEffectExpressionSupport`
- [x] `icu.windea.pls.ep.resolve.expression.ParadoxScriptMeshLocatorExpressionSupport`
