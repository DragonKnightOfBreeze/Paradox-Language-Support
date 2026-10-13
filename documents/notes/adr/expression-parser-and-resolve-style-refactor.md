# 重构 path/expression/complexExpression/config/configExpression - 更改解析器与解析方法的代码风格

> 所属模块：`后端 > 通用`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v2.1.10
> - 日期：2026-06-14
> - 状态：已实施

说明：
- 将对外的解析方法改为模型接口上的静态方法，委托给解析器类（可能是单例对象，可能是私有的）的对应方法。
- 解析器类通常是同文件中的私有类，少数情况下也可以是不同文件中的公开类。
- 偏好上，将同文件中的私有实现方法放到公开接口方法之后，并且使用 `region...endregion` 注释包围。
- 偏好上，将文档注释放到接口代码上，实现代码（以及公开的解析器类的代码）通常不需要文档注释。

目的：
- 为了统一代码风格。

示例（不需要公开解析类的情况）：

```kotlin
interface Expression {
    val value: String

    companion object {
        @JvmStatic
        fun resolve(text: String): Expression = ExpressionResolver.resolve(text)
    }
}

// region Implementations

private object ExpressionResolver {
    fun resolve(text: String): Expression {
        return ExpressionImpl(text)
    }
}

private data class ExpressionImpl(override val value: String)

// endregion
```

相关代码位置：
- `icu.windea.pls.model.scope`
- `icu.windea.pls.model.paths`
- `icu.windea.pls.model.expressions`
- `icu.windea.pls.config.config`
- `icu.windea.pls.config.configExpression`
- `icu.windea.pls.expression.expressions` (since v3.1.0)
