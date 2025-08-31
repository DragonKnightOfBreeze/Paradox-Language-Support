package icu.windea.pls.lang.expression.impl

import com.intellij.util.BitUtil
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.codeInsight.ParadoxTypeResolver
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.lang.expression.ParadoxScriptExpression
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionMatcher
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import java.util.*

internal class ParadoxScriptExpressionResolverImpl : ParadoxScriptExpression.Resolver {
    private val blockExpression: ParadoxScriptExpression = ParadoxScriptExpressionImpl(PlsStringConstants.blockFolder, ParadoxType.Block, false, false)

    override fun resolveBlock(): ParadoxScriptExpression {
        return blockExpression
    }

    override fun resolve(value: String, quoted: Boolean, isKey: Boolean?): ParadoxScriptExpression {
        return ParadoxScriptExpressionImpl(value, ParadoxTypeResolver.resolve(value), quoted, isKey)
    }

    override fun resolve(text: String, isKey: Boolean?): ParadoxScriptExpression {
        return ParadoxScriptExpressionImpl(text.unquote(), ParadoxTypeResolver.resolve(text), text.isLeftQuoted(), isKey)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, matchOptions: Int): ParadoxScriptExpression {
        return when {
            element is ParadoxScriptBlock -> blockExpression
            element is ParadoxScriptScriptedVariableReference -> ParadoxScriptExpressionLazyImpl(element, matchOptions, false)
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
            //兼容带参数的情况（此时先转化为正则表达式，再进行匹配）
            return regex.matches(v)
        }
        return value.equals(v, true) //忽略大小写
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxScriptExpression && (value == other.value && quoted == other.quoted)
    }

    override fun hashCode(): Int {
        return Objects.hash(value, type)
    }

    override fun toString(): String {
        return value
    }
}

private class ParadoxScriptExpressionImpl(
    override val value: String,
    override val type: ParadoxType,
    override val quoted: Boolean,
    override val isKey: Boolean?
) : ParadoxScriptExpressionBase()

private class ParadoxScriptExpressionLazyImpl(
    private val element: ParadoxScriptScriptedVariableReference,
    private val matchOptions: Int,
    override val isKey: Boolean?
) : ParadoxScriptExpressionBase() {
    //1.3.28 lazy resolve scripted variable value for data expressions to optimize config resolving (and also indexing) logic
    val valueElement by lazy {
        when {
            BitUtil.isSet(matchOptions, ParadoxExpressionMatcher.Options.SkipIndex) -> null
            else -> element.resolved()?.scriptedVariableValue
        }
    }

    override val value get() = valueElement?.value ?: PlsStringConstants.unknown
    override val type get() = valueElement?.type ?: ParadoxType.Unknown
    override val quoted get() = valueElement?.text?.isLeftQuoted() ?: false
}
