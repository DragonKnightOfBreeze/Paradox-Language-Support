package icu.windea.pls.lang.resolve.expression

import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.codeInsight.ParadoxTypeResolver
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchOptionsUtil
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import java.util.*

/**
 * 脚本表达式，
 *
 * 用途：
 * - 记录脚本中的键与值（LHS & RHS）的值、类型、是否用引号括起，是否是键等信息。
 *
 * @see ParadoxScriptExpressionElement
 */
interface ParadoxScriptExpression {
    val value: String
    val type: ParadoxType
    val quoted: Boolean
    val isKey: Boolean?

    fun isParameterized(): Boolean
    fun isFullParameterized(): Boolean
    fun matchesConstant(v: String): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveBlock(): ParadoxScriptExpression
        fun resolve(value: String, quoted: Boolean, isKey: Boolean? = null): ParadoxScriptExpression
        fun resolve(text: String, isKey: Boolean? = null): ParadoxScriptExpression
        fun resolve(element: ParadoxScriptExpressionElement, options: ParadoxMatchOptions? = null): ParadoxScriptExpression
    }

    companion object : Resolver by ParadoxScriptExpressionResolverImpl()
}

// region Implementations

private class ParadoxScriptExpressionResolverImpl : ParadoxScriptExpression.Resolver {
    private val blockExpression: ParadoxScriptExpression = ParadoxScriptExpressionImpl(PlsStrings.blockFolder, ParadoxType.Block, false, false)

    override fun resolveBlock(): ParadoxScriptExpression {
        return blockExpression
    }

    override fun resolve(value: String, quoted: Boolean, isKey: Boolean?): ParadoxScriptExpression {
        return ParadoxScriptExpressionImpl(value, ParadoxTypeResolver.resolve(value), quoted, isKey)
    }

    override fun resolve(text: String, isKey: Boolean?): ParadoxScriptExpression {
        return ParadoxScriptExpressionImpl(text.unquote(), ParadoxTypeResolver.resolve(text), text.isLeftQuoted(), isKey)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, options: ParadoxMatchOptions?): ParadoxScriptExpression {
        return when (element) {
            is ParadoxScriptBlock -> blockExpression
            is ParadoxScriptScriptedVariableReference -> ParadoxScriptExpressionLazyImpl(element, options, false)
            else -> ParadoxScriptExpressionImpl(element.value, element.type, element.text.isLeftQuoted(), element is ParadoxScriptPropertyKey)
        }
    }
}

private sealed class ParadoxScriptExpressionBase : ParadoxScriptExpression {
    private val regex by lazy { ParadoxExpressionManager.toRegex(value) }

    override fun isParameterized(): Boolean {
        return type == ParadoxType.String && value.isParameterized()
    }

    override fun isFullParameterized(): Boolean {
        return type == ParadoxType.String && value.isParameterized(full = true)
    }

    override fun matchesConstant(v: String): Boolean {
        if (value.isParameterized()) {
            // 兼容带参数的情况（此时先转化为正则表达式，再进行匹配）
            return regex.matches(v)
        }
        return value.equals(v, true) // 忽略大小写
    }

    override fun equals(other: Any?) = other is ParadoxScriptExpression && (value == other.value && quoted == other.quoted)
    override fun hashCode() = Objects.hash(value, type)
    override fun toString() = value
}

private class ParadoxScriptExpressionImpl(
    override val value: String,
    override val type: ParadoxType,
    override val quoted: Boolean,
    override val isKey: Boolean?
) : ParadoxScriptExpressionBase()

private class ParadoxScriptExpressionLazyImpl(
    private val element: ParadoxScriptScriptedVariableReference,
    private val options: ParadoxMatchOptions?,
    override val isKey: Boolean?
) : ParadoxScriptExpressionBase() {
    // 1.3.28 lazy resolve scripted variable value for data expressions to optimize config resolving (and also indexing) logic
    val valueElement by lazy {
        if (ParadoxMatchOptionsUtil.skipIndex(options)) return@lazy null
        element.resolved()
    }

    override val value: String get() = valueElement?.value.orEmpty()
    override val type: ParadoxType get() = valueElement?.type ?: ParadoxType.Unknown
    override val quoted: Boolean get() = valueElement?.text?.isLeftQuoted() ?: false
}

// endregion
