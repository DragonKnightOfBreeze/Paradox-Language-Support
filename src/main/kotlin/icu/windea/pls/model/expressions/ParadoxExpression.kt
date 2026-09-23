@file:Optimized

package icu.windea.pls.model.expressions

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.equalsFast
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.match.TextMatcher
import icu.windea.pls.core.quote
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.ep.match.expression.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.ep.resolve.expression.ParadoxCsvExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxLocalisationExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.isFullParameterized
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchOptionsService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.model.type.ParadoxExpressionType
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 脚本文件、本地化文件或者 CSV 文件中的各种表达式，
 *
 * @property text 文本。保留括起的双引号。
 * @property value 值，不保留括起的双引号。
 * @property type 类型。
 * @property role 角色。分为键/值/其他。
 *
 * @see ParadoxExpressionElement
 * @see ParadoxScriptExpressionMatcher
 * @see ParadoxCsvExpressionMatcher
 * @see ParadoxScriptExpressionSupport
 * @see ParadoxLocalisationExpressionSupport
 * @see ParadoxCsvExpressionSupport
 */
interface ParadoxExpression {
    val text: String
    val value: String
    val quoted: Boolean
    val type: ParadoxExpressionType
    val role: ParadoxExpressionRole

    fun isScalar(): Boolean
    fun isBoolean(): Boolean
    fun isInt(): Boolean
    fun isFloat(): Boolean
    fun isParameterized(): Boolean
    fun isFullParameterized(): Boolean
    fun isFullParameterizedWithLeadingUnary(): Boolean

    fun matchesRegex(input: String): Boolean
    fun matchesConstant(input: String): Boolean

    override fun equals(other: Any?): Boolean // NOTE 3.0.1 only based on `text`
    override fun hashCode(): Int // NOTE 3.0.1 only based on `text`
    override fun toString(): String

    companion object {
        @JvmStatic
        fun resolveBlock(): ParadoxExpression {
            return ParadoxExpressionResolver.resolveBlock()
        }

        @JvmStatic
        fun resolveUnknown(): ParadoxExpression {
            return ParadoxExpressionResolver.resolveUnknown()
        }

        @JvmStatic
        fun resolve(text: String, role: ParadoxExpressionRole = ParadoxExpressionRole.Other): ParadoxExpression {
            return ParadoxExpressionResolver.resolve(text, role)
        }

        @JvmStatic
        fun resolve(value: String, quoted: Boolean, role: ParadoxExpressionRole = ParadoxExpressionRole.Other): ParadoxExpression {
            return ParadoxExpressionResolver.resolve(value, quoted, role)
        }

        @JvmStatic
        fun resolve(element: ParadoxExpressionElement, options: ParadoxMatchOptions? = null): ParadoxExpression {
            return ParadoxExpressionResolver.resolve(element, options)
        }
    }
}

// region Implementations

private object ParadoxExpressionResolver {
    private val blockExpression: ParadoxExpression = ParadoxExpressionImpl(ChronicleStrings.blockFolder, ChronicleStrings.blockFolder, false, ParadoxExpressionType.Block, ParadoxExpressionRole.Value)
    private val unknownExpression: ParadoxExpression = ParadoxExpressionImpl("", "", false, ParadoxExpressionType.Unknown, ParadoxExpressionRole.Other)

    fun resolveBlock(): ParadoxExpression = blockExpression

    fun resolveUnknown(): ParadoxExpression = unknownExpression

    fun resolve(text: String, role: ParadoxExpressionRole): ParadoxExpression {
        return ParadoxTextBasedExpression(text, role)
    }

    fun resolve(value: String, quoted: Boolean, role: ParadoxExpressionRole): ParadoxExpression {
        return when {
            quoted -> ParadoxQuotedValueBasedExpression(value, role)
            else -> ParadoxUnquotedValueBasedExpression(value, role)
        }
    }

    fun resolve(element: ParadoxExpressionElement, options: ParadoxMatchOptions?): ParadoxExpression {
        return when (element) {
            is ParadoxScriptBlock -> blockExpression
            is ParadoxScriptScriptedVariableReference -> ParadoxScriptedVariableReferenceBasedExpression(element, options)
            else -> ParadoxPsiBasedExpression(element)
        }
    }
}

private sealed class ParadoxExpressionBase : ParadoxExpression {
    // 3.0.1 optimize: cache status
    // 3.0.1 optimize: use more memory-friendly lazy property

    private inline val int: Boolean // region by lazy { computeInt() }
        get() = LazyValue.ofBoolean({ _int }, { _int = it }) { computeInt() }
    @Volatile private var _int = LazyValue.UNINITIALIZED_BOOLEAN // endregion
    private inline val float: Boolean // region by lazy { computeFloat() }
        get() = LazyValue.ofBoolean({ _float }, { _float = it }) { computeFloat() }
    @Volatile private var _float = LazyValue.UNINITIALIZED_BOOLEAN // endregion
    private inline val parameterized: Boolean // region by lazy { computeParameterized() }
        get() = LazyValue.ofBoolean({ _parameterized }, { _parameterized = it }) { computeParameterized() }
    @Volatile private var _parameterized = LazyValue.UNINITIALIZED_BOOLEAN // endregion
    private inline val fullParameterized: Boolean // region by lazy { computeFullParameterized() }
        get() = LazyValue.ofBoolean({ _fullParameterized }, { _fullParameterized = it }) { computeFullParameterized() }
    @Volatile private var _fullParameterized = LazyValue.UNINITIALIZED_BOOLEAN // endregion
    private inline val fullParameterizedWithLeadingUnary: Boolean // region by lazy { computeFullParameterizedWithLeadingUnary() }
        get() = LazyValue.ofBoolean({ _fullParameterizedWithLeadingUnary }, { _fullParameterizedWithLeadingUnary = it }) { computeFullParameterizedWithLeadingUnary() }
    @Volatile private var _fullParameterizedWithLeadingUnary = LazyValue.UNINITIALIZED_BOOLEAN // endregion
    private inline val regex: Regex // region by lazy { computeRegex() }
        get() = LazyValue.of({ _regex }, { _regex = it }) { computeRegex() }
    @Volatile private var _regex: Regex? = null // endregion

    private fun computeScalar(): Boolean {
        return when {
            role == ParadoxExpressionRole.Key -> true // key -> ok
            type.isLenientBooleanLiteral() -> true // boolean -> sadly, also ok for compatibility
            type.isLenientNumberLiteral() -> true // number -> ok according to vanilla game files
            type.isLenientStringLiteral() -> true // unquoted/quoted string -> ok
            else -> false
        }
    }

    private fun computeBoolean(): Boolean {
        return type.isLenientBooleanLiteral()
    }

    private fun computeInt(): Boolean {
        return type.isLenientInt() || TextMatcher.matchesInt(value)
    }

    private fun computeFloat(): Boolean {
        return type.isLenientFloat() || TextMatcher.matchesFloat(value)
    }

    private fun computeParameterized(): Boolean {
        return type.isStringLiteral() && value.isParameterized()
    }

    private fun computeFullParameterized(): Boolean {
        return type.isStringLiteral() && value.isFullParameterized()
    }

    private fun computeFullParameterizedWithLeadingUnary(): Boolean {
        return type.isStringLiteral() && TextMatcher.isNumberUnaryChar(value.first()) && value.isFullParameterized(1)
    }

    private fun computeRegex(): Regex {
        return ParadoxExpressionManager.toRegex(value)
    }

    override fun isScalar(): Boolean = computeScalar()
    override fun isBoolean(): Boolean = computeBoolean()
    override fun isInt(): Boolean = int
    override fun isFloat(): Boolean = float
    override fun isParameterized(): Boolean = parameterized
    override fun isFullParameterized(): Boolean = fullParameterized
    override fun isFullParameterizedWithLeadingUnary(): Boolean = fullParameterizedWithLeadingUnary

    override fun matchesRegex(input: String): Boolean = regex.matches(input)

    override fun matchesConstant(input: String): Boolean {
        // 3.0.1 radical optimization
        // 如果表达式未用引号括起，不能用来匹配布尔关键字
        if (quoted && (ChronicleStrings.yesKeyword.equalsFast(input) || ChronicleStrings.noKeyword.equalsFast(input))) return false
        // 忽略大小写
        return value.equalsFast(input, true)
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
    override val type: ParadoxExpressionType = if (quoted) ParadoxExpressionType.String else ParadoxTypeResolver.resolveExpressionType(value)
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
    override val type: ParadoxExpressionType = ParadoxTypeResolver.resolveExpressionType(value)
}

private class ParadoxPsiBasedExpression(
    element: ParadoxExpressionElement,
) : ParadoxExpressionBase() {
    override val text: String = element.text
    override val value: String = element.value
    override val quoted: Boolean = if (element is ParadoxScriptStringExpressionElement) text.isLeftQuoted() else false
    override val type: ParadoxExpressionType = ParadoxTypeResolver.resolveExpressionType(element)
    override val role: ParadoxExpressionRole = ParadoxTypeResolver.resolveExpressionRole(element)
}

private class ParadoxScriptedVariableReferenceBasedExpression(
    private val element: ParadoxScriptedVariableReference,
    private val options: ParadoxMatchOptions?,
) : ParadoxExpressionBase() {
    private inline val resolvedExpression: ParadoxExpression // region by lazy { computeResolvedExpression() }
        get() = LazyValue.of(this, { _resolvedExpression }, { _resolvedExpression = it }) { computeResolvedExpression() }
    @Volatile private var _resolvedExpression: ParadoxExpression? = null // endregion

    private fun computeResolvedExpression(): ParadoxExpression {
        if (ParadoxMatchOptionsService.isDumb(options)) return ParadoxExpression.resolveUnknown()
        val resolved = element.resolved() ?: return ParadoxExpression.resolveUnknown()
        return ParadoxPsiBasedExpression(resolved)
    }

    override val text: String get() = resolvedExpression.text
    override val value: String get() = resolvedExpression.value
    override val quoted: Boolean get() = resolvedExpression.quoted
    override val type: ParadoxExpressionType get() = resolvedExpression.type
    override val role: ParadoxExpressionRole get() = ParadoxExpressionRole.Value
}

// endregion
