package icu.windea.pls.model.expressions

import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.quote
import icu.windea.pls.core.match.TextMatcher
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.model.type.ParadoxExpressionType
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher
import icu.windea.pls.ep.match.ParadoxCsvExpressionMatcher

/**
 * 脚本文件、本地化文件或者 CSV 文件中的表达式，
 *
 * @see ParadoxExpressionElement
 * @see ParadoxScriptExpressionMatcher
 * @see ParadoxCsvExpressionMatcher
 */
interface ParadoxExpression {
    val text: String
    val value: String
    val quoted: Boolean
    val type: ParadoxExpressionType
    val role: ParadoxExpressionRole

    fun isParameterized(): Boolean
    fun isFullParameterized(): Boolean

    fun matchesInt(): Boolean
    fun matchesFloat(): Boolean
    fun matchesConstant(v: String): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveBlock(): ParadoxExpression
        fun resolveUnknown(): ParadoxExpression
        fun resolve(text: String, role: ParadoxExpressionRole = ParadoxExpressionRole.Other): ParadoxExpression
        fun resolve(value: String, quoted: Boolean, role: ParadoxExpressionRole = ParadoxExpressionRole.Other): ParadoxExpression
        fun resolve(element: ParadoxExpressionElement, options: ParadoxMatchOptions? = null): ParadoxExpression
    }

    companion object : Resolver by ParadoxScriptExpressionResolverImpl()
}

// region Implementations

private class ParadoxScriptExpressionResolverImpl : ParadoxExpression.Resolver {
    private val blockExpression: ParadoxExpression = ParadoxExpressionImpl("{...}", "{...}", false, ParadoxExpressionType.Block, ParadoxExpressionRole.Value)
    private val unknownExpression: ParadoxExpression = ParadoxExpressionImpl("", "", false, ParadoxExpressionType.Unknown, ParadoxExpressionRole.Other)

    override fun resolveBlock(): ParadoxExpression {
        return blockExpression
    }

    override fun resolveUnknown(): ParadoxExpression {
        return unknownExpression
    }

    override fun resolve(text: String, role: ParadoxExpressionRole): ParadoxExpression {
        return ParadoxTextBasedExpression(text, role)
    }

    override fun resolve(value: String, quoted: Boolean, role: ParadoxExpressionRole): ParadoxExpression {
        return when {
            quoted -> ParadoxQuotedValueBasedExpression(value, role)
            else -> ParadoxUnquotedValueBasedExpression(value, role)
        }
    }

    override fun resolve(element: ParadoxExpressionElement, options: ParadoxMatchOptions?): ParadoxExpression {
        return when (element) {
            is ParadoxScriptBlock -> blockExpression
            is ParadoxScriptScriptedVariableReference -> ParadoxScriptedVariableReferenceBasedExpression(element, options)
            else -> ParadoxPsiBasedExpression(element)
        }
    }
}

private sealed class ParadoxExpressionBase : ParadoxExpression {
    private val regex by lazy { ParadoxExpressionManager.toRegex(value) }

    override fun isParameterized(): Boolean {
        return type == ParadoxExpressionType.String && value.isParameterized()
    }

    override fun isFullParameterized(): Boolean {
        return type == ParadoxExpressionType.String && value.isParameterized(full = true)
    }

    override fun matchesInt(): Boolean {
        return type.isLenientInt() || TextMatcher.matchesInt(value)
    }

    override fun matchesFloat(): Boolean {
        return type.isLenientFloat() || TextMatcher.matchesFloat(value)
    }

    override fun matchesConstant(v: String): Boolean {
        // 兼容带参数的情况（此时先转化为正则表达式，再进行匹配）
        if (value.isParameterized()) return regex.matches(v)
        // 忽略大小写
        return value.equals(v, true)
    }

    override fun equals(other: Any?) = this === other || other is ParadoxExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

private class ParadoxExpressionImpl(
    override val text: String,
    override val value: String,
    override val quoted: Boolean,
    override val type: ParadoxExpressionType,
    override val role: ParadoxExpressionRole,
) : ParadoxExpressionBase()

private class ParadoxTextBasedExpression(
    override val text: String,
    override val role: ParadoxExpressionRole,
) : ParadoxExpressionBase() {
    override val value: String = text.unquote()
    override val quoted: Boolean = text.isLeftQuoted()
    override val type: ParadoxExpressionType = if (quoted) ParadoxExpressionType.String else ParadoxTypeResolver.resolveType(value)
}

private class ParadoxQuotedValueBasedExpression(
    override val value: String,
    override val role: ParadoxExpressionRole,
) : ParadoxExpressionBase() {
    override val text: String = value.quote()
    override val quoted: Boolean get() = true
    override val type: ParadoxExpressionType get() = ParadoxExpressionType.String
}

private class ParadoxUnquotedValueBasedExpression(
    override val value: String,
    override val role: ParadoxExpressionRole,
) : ParadoxExpressionBase() {
    override val text: String get() = value
    override val quoted: Boolean get() = false
    override val type: ParadoxExpressionType = ParadoxTypeResolver.resolveType(value)
}

private class ParadoxPsiBasedExpression(
    element: ParadoxExpressionElement,
) : ParadoxExpressionBase() {
    override val text: String = element.text
    override val value: String = element.value
    override val quoted: Boolean = text.isLeftQuoted()
    override val type: ParadoxExpressionType = if (quoted) ParadoxExpressionType.String else ParadoxTypeResolver.resolveType(value)
    override val role: ParadoxExpressionRole = ParadoxTypeResolver.resolveExpressionRole(element)
}

private class ParadoxScriptedVariableReferenceBasedExpression(
    private val element: ParadoxScriptedVariableReference,
    private val options: ParadoxMatchOptions?,
) : ParadoxExpressionBase() {
    private val resolvedExpression by lazy { computeResolvedExpression() }

    private fun computeResolvedExpression(): ParadoxExpression {
        if (ParadoxMatchService.isDumb(options)) return ParadoxExpression.resolveUnknown()
        val resolved = element.resolved() ?: return ParadoxExpression.resolveUnknown()
        return ParadoxPsiBasedExpression(resolved)
    }

    override val text: String get() = resolvedExpression.text
    override val value: String get() = resolvedExpression.value
    override val type: ParadoxExpressionType get() = resolvedExpression.type
    override val quoted: Boolean get() = resolvedExpression.quoted
    override val role: ParadoxExpressionRole get() = ParadoxExpressionRole.Value
}

// endregion
