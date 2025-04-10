package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.CwtLocalisationLocationExpression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * CWT本地化位置表达式。
 *
 * 用于定位定义的相关本地化。
 *
 * 如果包含占位符`$`，将其替换成定义的名字后，尝试得到对应名字的本地化，否则尝试得到对应名字的属性的值对应的本地化。
 *
 * 示例：`"$"`, `"$_desc"`, `"$_DESC|u"` , `"title"`
 *
 * @property placeholder 占位符文本。其中的`"$"`会在解析时被替换成定义的名字。
 * @property propertyName 属性名，用于获取本地化的名字。
 * @property upperCase 本地化的名字是否强制大写。
 */
interface CwtLocalisationLocationExpression : CwtExpression {
    val placeholder: String?
    val propertyName: String?
    val upperCase: Boolean

    fun resolvePlaceholder(name: String): String?

    data class ResolveResult(
        val name: String,
        val element: ParadoxLocalisationProperty?,
        val message: String? = null
    )

    data class ResolveAllResult(
        val name: String,
        val elements: Set<ParadoxLocalisationProperty>,
        val message: String? = null
    )

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

private fun doResolveEmpty() = CwtLocalisationLocationExpressionImpl("", propertyName = "")

private fun doResolve(expressionString: String): CwtLocalisationLocationExpression {
    return when {
        expressionString.isEmpty() -> Resolver.EmptyExpression
        expressionString.contains('$') -> {
            val placeholder = expressionString.substringBefore('|')
            val upperCase = expressionString.substringAfter('|', "") == "u"
            CwtLocalisationLocationExpressionImpl(expressionString, placeholder = placeholder, upperCase = upperCase)
        }
        else -> {
            val propertyName = expressionString
            CwtLocalisationLocationExpressionImpl(expressionString, propertyName = propertyName)
        }
    }
}

private class CwtLocalisationLocationExpressionImpl(
    override val expressionString: String,
    override val placeholder: String? = null,
    override val propertyName: String? = null,
    override val upperCase: Boolean = false
) : CwtLocalisationLocationExpression {
    override fun resolvePlaceholder(name: String): String? {
        if (placeholder == null) return null
        return buildString { for (c in placeholder) if (c == '$') append(name) else append(c) }
            .letIf(upperCase) { it.uppercase() }
    }

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
