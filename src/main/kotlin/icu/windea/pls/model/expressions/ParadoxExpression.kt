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
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
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

    fun isParameterized(): Boolean
    fun isFullParameterized(): Boolean

    fun matchesInt(): Boolean
    fun matchesFloat(): Boolean
    fun matchesRegex(v: String): Boolean
    fun matchesConstant(v: String): Boolean

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

    private inline val parameterized: Boolean // region by lazy { doIsParameterized() }
        get() = LazyValue.ofBoolean({ _parameterized }, { _parameterized = it }) { doIsParameterized() }
    @Volatile private var _parameterized = LazyValue.UNINITIALIZED_BOOLEAN // endregion
    private inline val fullParameterized: Boolean // region by lazy { doIsFullParameterized() }
        get() = LazyValue.ofBoolean({ _fullParameterized }, { _fullParameterized = it }) { doIsFullParameterized() }
    @Volatile private var _fullParameterized = LazyValue.UNINITIALIZED_BOOLEAN // endregion
    private inline val int: Boolean // region by lazy { doMatchInt() }
        get() = LazyValue.ofBoolean({ _int }, { _int = it }) { doMatchInt() }
    @Volatile private var _int = LazyValue.UNINITIALIZED_BOOLEAN // endregion
    private inline val float: Boolean // region by lazy { doMatchFloat() }
        get() = LazyValue.ofBoolean({ _float }, { _float = it }) { doMatchFloat() }
    @Volatile private var _float = LazyValue.UNINITIALIZED_BOOLEAN // endregion
    private inline val regex: Regex // region by lazy { computeRegex() }
        get() = LazyValue.of({ _regex }, { _regex = it }) { computeRegex() }
    @Volatile private var _regex: Regex? = null // endregion

    private fun doIsParameterized() = type == ParadoxExpressionType.String && value.isParameterized()
    private fun doIsFullParameterized() = type == ParadoxExpressionType.String && value.isParameterized(full = true)
    private fun doMatchInt() = type.isLenientInt() || TextMatcher.matchesInt(value)
    private fun doMatchFloat() = type.isLenientFloat() || TextMatcher.matchesFloat(value)
    private fun computeRegex() = ParadoxExpressionManager.toRegex(value)

    override fun isParameterized(): Boolean = parameterized

    override fun isFullParameterized(): Boolean = fullParameterized

    override fun matchesInt(): Boolean = int

    override fun matchesFloat(): Boolean = float

    override fun matchesRegex(v: String): Boolean {
        return regex.matches(v)
    }

    override fun matchesConstant(v: String): Boolean {
        // 兼容带参数的情况（此时先转化为正则表达式，再进行匹配）
        if (isParameterized()) return matchesRegex(v)
        // 忽略大小写
        return value.equalsFast(v, true) // 3.0.1 radical optimization
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
        if (ParadoxMatchService.isDumb(options)) return ParadoxExpression.resolveUnknown()
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
