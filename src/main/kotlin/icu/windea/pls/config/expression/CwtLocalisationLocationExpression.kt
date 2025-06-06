package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.localisation.psi.*

/**
 * CWT本地化位置表达式。用于定位定义的相关本地化。
 *
 * 示例：
 *
 * * `"$_desc"` -> 用当前定义的名字替换占位符，解析为本地化的名字。
 * * `"$_desc|$name"` -> 在前者的基础上，改为用指定路径（`name`）的属性的值替换占位符（如果值存在且可以获取），解析为本地化的名字。管道符后的路径可以有多个，逗号分割。
 * * `"$_desc|$name|u"` -> 在前者的基础上，强制解析为大写的本地化的名字。
 * * `"title"` -> 得到当前定义声明中指定路径（`title`）的属性的值，解析为本地化的名字。
 *
 * @property namePaths 用于获取名字文本的一组表达式路径。名字文本用于替换占位符。
 * @property forceUpperCase 本地化的名字是否需要强制大写。
 */
interface CwtLocalisationLocationExpression : CwtLocationExpression {
    val namePaths: Set<String>
    val forceUpperCase: Boolean

    operator fun component3() = namePaths
    operator fun component4() = forceUpperCase

    class ResolveResult(
        val name: String,
        val message: String? = null,
        resolveAction: () -> ParadoxLocalisationProperty? = { null },
        resolveAllAction: () -> Collection<ParadoxLocalisationProperty> = { emptySet() },
    ) {
        val element: ParadoxLocalisationProperty? by lazy { resolveAction() }
        val elements: Collection<ParadoxLocalisationProperty> by lazy { resolveAllAction() }
    }

    companion object Resolver {
        val EmptyExpression: CwtLocalisationLocationExpression = doResolveEmpty()

        fun resolve(expressionString: String): CwtLocalisationLocationExpression {
            if (expressionString.isEmpty()) return EmptyExpression
            return cache.get(expressionString)
        }
    }
}

//Implementations (cached & not interned)

private val cache = CacheBuilder.newBuilder().buildCache<String, CwtLocalisationLocationExpression> { doResolve(it) }

private fun doResolveEmpty() = CwtLocalisationLocationExpressionImpl("", "")

private fun doResolve(expressionString: String): CwtLocalisationLocationExpression {
    if (expressionString.isEmpty()) return CwtLocalisationLocationExpression.EmptyExpression
    val tokens = expressionString.split('|')
    if (tokens.size == 1) return CwtLocalisationLocationExpressionImpl(expressionString, expressionString)
    val location = tokens.first()
    val args = tokens.drop(1)
    var namePaths: Set<String>? = null
    var forceUpperCase = false
    args.forEach { arg ->
        if (arg.startsWith('$')) {
            namePaths = arg.drop(1).toCommaDelimitedStringSet()
        } else if (arg == "u") {
            forceUpperCase = true
        }
    }
    return CwtLocalisationLocationExpressionImpl(expressionString, location, namePaths.orEmpty(), forceUpperCase)
}

private class CwtLocalisationLocationExpressionImpl(
    override val expressionString: String,
    override val location: String,
    override val namePaths: Set<String> = emptySet(),
    override val forceUpperCase: Boolean = false,
) : CwtLocalisationLocationExpression {
    override val isPlaceholder: Boolean = location.contains('$')

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocalisationLocationExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int {
        return expressionString.hashCode()
    }

    override fun toString(): String {
        return expressionString
    }
}
