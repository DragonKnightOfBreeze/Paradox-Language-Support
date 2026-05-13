package icu.windea.pls.model.expressions

import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.quote
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.type.ParadoxExpressionType
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference

/**
 * 脚本表达式，
 *
 * 用途：
 * - 记录脚本中的键与值（LHS & RHS）的值、类型、是否用引号括起，是否是键等信息。
 *
 * @see ParadoxScriptExpressionElement
 */
interface ParadoxScriptExpression {
    val text: String
    val value: String
    val quoted: Boolean
    val type: ParadoxExpressionType
    val isKey: Boolean?

    fun isParameterized(): Boolean
    fun isFullParameterized(): Boolean
    fun matchesConstant(v: String): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveBlock(): ParadoxScriptExpression
        fun resolveUnknown(): ParadoxScriptExpression
        fun resolve(text: String, isKey: Boolean? = null): ParadoxScriptExpression
        fun resolve(value: String, quoted: Boolean, isKey: Boolean? = null): ParadoxScriptExpression
        fun resolve(element: ParadoxScriptExpressionElement, options: ParadoxMatchOptions? = null): ParadoxScriptExpression
    }

    companion object : Resolver by ParadoxScriptExpressionResolverImpl()
}

// region Implementations

private class ParadoxScriptExpressionResolverImpl : ParadoxScriptExpression.Resolver {
    private val blockExpression: ParadoxScriptExpression = ParadoxScriptExpressionImpl1("{...}", "{...}", false, ParadoxExpressionType.Block, false)
    private val unknownExpression: ParadoxScriptExpression = ParadoxScriptExpressionImpl1("", "", false, ParadoxExpressionType.Unknown, false)

    override fun resolveBlock(): ParadoxScriptExpression {
        return blockExpression
    }

    override fun resolveUnknown(): ParadoxScriptExpression {
        return unknownExpression
    }

    override fun resolve(text: String, isKey: Boolean?): ParadoxScriptExpression {
        return ParadoxScriptExpressionImpl2(text, isKey)
    }

    override fun resolve(value: String, quoted: Boolean, isKey: Boolean?): ParadoxScriptExpression {
        return ParadoxScriptExpressionImpl3(value, quoted, isKey)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, options: ParadoxMatchOptions?): ParadoxScriptExpression {
        return when (element) {
            is ParadoxScriptBlock -> blockExpression
            is ParadoxScriptScriptedVariableReference -> ParadoxScriptExpressionImpl5(element, options)
            else -> ParadoxScriptExpressionImpl4(element)
        }
    }
}

private sealed class ParadoxScriptExpressionBase : ParadoxScriptExpression {
    private val regex by lazy { ParadoxExpressionManager.toRegex(value) }

    override fun isParameterized(): Boolean {
        return type == ParadoxExpressionType.String && value.isParameterized()
    }

    override fun isFullParameterized(): Boolean {
        return type == ParadoxExpressionType.String && value.isParameterized(full = true)
    }

    override fun matchesConstant(v: String): Boolean {
        if (value.isParameterized()) {
            // 兼容带参数的情况（此时先转化为正则表达式，再进行匹配）
            return regex.matches(v)
        }
        return value.equals(v, true) // 忽略大小写
    }

    override fun equals(other: Any?) = this === other || other is ParadoxScriptExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

private class ParadoxScriptExpressionImpl1(
    override val text: String,
    override val value: String,
    override val quoted: Boolean,
    override val type: ParadoxExpressionType,
    override val isKey: Boolean?,
) : ParadoxScriptExpressionBase()

private class ParadoxScriptExpressionImpl2(
    override val text: String,
    override val isKey: Boolean?,
) : ParadoxScriptExpressionBase() {
    override val value: String = text.unquote()
    override val quoted: Boolean = text.isLeftQuoted()
    override val type: ParadoxExpressionType = ParadoxTypeResolver.resolveType(value)
}

private class ParadoxScriptExpressionImpl3(
    override val value: String,
    override val quoted: Boolean,
    override val isKey: Boolean?,
) : ParadoxScriptExpressionBase() {
    override val text: String = if (quoted) value.quote() else value
    override val type: ParadoxExpressionType = ParadoxTypeResolver.resolveType(value)
}

private class ParadoxScriptExpressionImpl4(
    element: ParadoxScriptExpressionElement,
) : ParadoxScriptExpressionBase() {
    override val text: String = element.text
    override val value: String = element.value
    override val quoted: Boolean = text.isLeftQuoted()
    override val type: ParadoxExpressionType = ParadoxTypeResolver.resolveType(value)
    override val isKey: Boolean = element is ParadoxScriptPropertyKey
}

private class ParadoxScriptExpressionImpl5(
    private val element: ParadoxScriptScriptedVariableReference,
    private val options: ParadoxMatchOptions?
) : ParadoxScriptExpressionBase() {
    private val resolvedExpression by lazy { computeResolvedExpression() }

    private fun computeResolvedExpression(): ParadoxScriptExpression {
        if (ParadoxMatchService.isDumb(options)) return ParadoxScriptExpression.resolveUnknown()
        val resolved = element.resolved() ?: return ParadoxScriptExpression.resolveUnknown()
        return ParadoxScriptExpressionImpl4(resolved)
    }

    override val text: String get() = resolvedExpression.text
    override val value: String get() = resolvedExpression.value
    override val type: ParadoxExpressionType get() = resolvedExpression.type
    override val quoted: Boolean get() = resolvedExpression.quoted
    override val isKey: Boolean get() = false
}

// endregion
