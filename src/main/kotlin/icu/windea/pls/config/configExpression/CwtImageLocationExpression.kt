package icu.windea.pls.config.configExpression

import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.toCommaDelimitedStringSet

/**
 * 图片位置表达式。
 *
 * 用于定位定义的相关图片。
 *
 * 语法与约定：
 * - 以 `|` 分隔参数：`<location>|<args...>`。
 * - 以 `$` 开头的参数表示从指定路径读取“名称文本”以替换占位符（支持逗号分隔多路径），映射到 [namePaths]。
 * - 其他参数表示帧数来源路径（支持逗号分隔多路径），映射到 [framePaths]。
 * - 当 [location] 含 `$` 时表示存在占位符，需要在后续步骤以“定义名或属性值”等替换。
 *
 * CWTools 兼容性：兼容，但存在一些扩展。
 *
 * 示例：
 * ```cwt
 * icon = "gfx/interface/icons/modifiers/mod_$.dds" # 用当前定义的名字替换占位符，解析为图片路径。
 * icon = "gfx/interface/icons/modifiers/mod_$.dds|$name" # 以上改为从指定路径（`name`）的属性值替换占位符（多路径逗号分隔）。
 * icon = "GFX_$" # 用当前定义的名字替换占位符，解析为 sprite 名，再解析到图片路径。
 * icon = "icon" # 读取定义声明的 `icon` 属性，解析为图片路径、sprite 名或定义名；若为定义名则继续解析其最相关图片。
 * icon = "icon|p1,p2" # 以上并从 `p1`/`p2` 路径读取帧数用于后续切分。
 * ```
 *
 * @property namePaths 用于获取名字文本的一组成员路径。名字文本用于替换占位符。
 * @property framePaths 用于获取帧数的一组成员路径。帧数用于后续切分图片。
 *
 * @see icu.windea.pls.config.config.delegated.CwtTypeImagesConfig
 */
interface CwtImageLocationExpression : CwtLocationExpression {
    val namePaths: Set<String>
    val framePaths: Set<String>

    operator fun component3() = namePaths
    operator fun component4() = framePaths

    interface Resolver {
        fun resolveEmpty(): CwtImageLocationExpression
        fun resolve(expressionString: String): CwtImageLocationExpression
    }

    companion object : Resolver by CwtImageLocationExpressionResolverImpl()
}

// region Implementations

private class CwtImageLocationExpressionResolverImpl : CwtImageLocationExpression.Resolver {
    private val cache = CacheBuilder("expireAfterAccess=30m")
        .build<String, CwtImageLocationExpression> { doResolve(it) }

    private val emptyExpression = CwtImageLocationExpressionImpl("", "")

    override fun resolveEmpty(): CwtImageLocationExpression = emptyExpression

    override fun resolve(expressionString: String): CwtImageLocationExpression {
        if (expressionString.isEmpty()) return emptyExpression
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtImageLocationExpression {
        // 以 '|' 切分：首段为 location，其余为参数
        val tokens = expressionString.split('|')
        // 仅包含 location，无额外参数
        if (tokens.size == 1) return CwtImageLocationExpressionImpl(expressionString, expressionString)
        val location = tokens.first()
        val args = tokens.drop(1)
        var namePaths: Set<String>? = null
        var framePaths: Set<String>? = null
        args.forEach { arg ->
            // 以 '$' 开头：表示 namePaths；否则为 framePaths
            // 若出现多次，同类参数以后者覆盖（按实现顺序）
            if (arg.startsWith('$')) {
                namePaths = arg.drop(1).toCommaDelimitedStringSet()
            } else {
                framePaths = arg.toCommaDelimitedStringSet()
            }
        }
        return CwtImageLocationExpressionImpl(expressionString, location, namePaths.orEmpty(), framePaths.orEmpty())
    }
}

private class CwtImageLocationExpressionImpl(
    override val expressionString: String,
    override val location: String,
    override val namePaths: Set<String> = emptySet(),
    override val framePaths: Set<String> = emptySet(),
) : CwtImageLocationExpression {
    // 当 location 包含占位符 '$' 时，需要在后续步骤以名称文本替换
    override val isPlaceholder: Boolean = location.contains('$')

    override fun equals(other: Any?) = this === other || other is CwtImageLocationExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

// endregion
