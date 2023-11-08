package icu.windea.pls.core.expression

import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDataExpression.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于表达式脚本语言中的表达式，对应一个key或value。
 * 可以指定是否用引号括起。
 */
interface ParadoxDataExpression : Expression {
    val text: String
    val type: ParadoxType
    val quoted: Boolean
    val isKey: Boolean?
    
    companion object Resolver
}

fun ParadoxDataExpression.isParameterized() = type == ParadoxType.String && text.isParameterized()

class ParadoxDataExpressionImpl(
    override val text: String,
    override val type: ParadoxType,
    override val quoted: Boolean,
    override val isKey: Boolean?
) : AbstractExpression(text), ParadoxDataExpression

object BlockParadoxDataExpression : AbstractExpression(PlsConstants.blockFolder), ParadoxDataExpression {
    override val text: String get() = PlsConstants.blockFolder
    override val type: ParadoxType = ParadoxType.Block
    override val quoted: Boolean = false
    override val isKey: Boolean = false
}

object UnknownParadoxDataExpression : AbstractExpression(PlsConstants.unknownString), ParadoxDataExpression {
    override val text: String get() = PlsConstants.unknownString
    override val type: ParadoxType = ParadoxType.Unknown
    override val quoted: Boolean = false
    override val isKey: Boolean = false
}

fun Resolver.resolve(element: ParadoxScriptExpressionElement, matchOptions: Int = CwtConfigMatcher.Options.Default): ParadoxDataExpression {
    return when {
        element is ParadoxScriptBlock -> {
            BlockParadoxDataExpression
        }
        element is ParadoxScriptScriptedVariableReference -> {
            ProgressManager.checkCanceled()
            val valueElement = when {
                BitUtil.isSet(matchOptions, CwtConfigMatcher.Options.SkipIndex) -> return UnknownParadoxDataExpression
                else -> element.referenceValue ?: return UnknownParadoxDataExpression
            }
            ParadoxDataExpressionImpl(valueElement.value, valueElement.type, valueElement.text.isLeftQuoted(), false)
        }
        else -> {
            val isKey = element is ParadoxScriptPropertyKey
            ParadoxDataExpressionImpl(element.value, element.type, element.text.isLeftQuoted(), isKey)
        }
    }
}

fun Resolver.resolve(value: String, isQuoted: Boolean, isKey: Boolean? = null): ParadoxDataExpression {
    val expressionType = ParadoxType.resolve(value)
    return ParadoxDataExpressionImpl(value, expressionType, isQuoted, isKey)
}

fun Resolver.resolve(text: String, isKey: Boolean? = null): ParadoxDataExpression {
    val value = text.unquote()
    val expressionType = ParadoxType.resolve(text)
    val quoted = text.isLeftQuoted()
    return ParadoxDataExpressionImpl(value, expressionType, quoted, isKey)
}
