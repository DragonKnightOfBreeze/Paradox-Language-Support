package icu.windea.pls.model.expression

import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * 数据表达式，对应脚本语言中的一个键或值。
 *
 * 可以指定是否用引号括起。
 */
interface ParadoxDataExpression {
    val text: String
    val type: ParadoxType
    val quoted: Boolean
    val isKey: Boolean?
    
    companion object Resolver {
        val BlockExpression: ParadoxDataExpression = icu.windea.pls.model.expression.BlockParadoxDataExpression
        val UnknownExpression: ParadoxDataExpression = icu.windea.pls.model.expression.UnknownParadoxDataExpression
        
        fun resolve(element: ParadoxScriptExpressionElement, matchOptions: Int = CwtConfigMatcher.Options.Default): ParadoxDataExpression =
            doResolve(element, matchOptions)
        
        fun resolve(value: String, isQuoted: Boolean, isKey: Boolean? = null): ParadoxDataExpression =
            doResolve(value, isQuoted, isKey)
        
        fun resolve(text: String, isKey: Boolean? = null): ParadoxDataExpression =
            doResolve(text, isKey)
    }
}

fun ParadoxDataExpression.isParameterized() = type == ParadoxType.String && text.isParameterized()

//Implementations

private fun doResolve(element: ParadoxScriptExpressionElement, matchOptions: Int): ParadoxDataExpression {
    return when {
        element is ParadoxScriptBlock -> {
            icu.windea.pls.model.expression.BlockParadoxDataExpression
        }
        element is ParadoxScriptScriptedVariableReference -> {
            ProgressManager.checkCanceled()
            val valueElement = when {
                BitUtil.isSet(matchOptions, CwtConfigMatcher.Options.SkipIndex) -> return icu.windea.pls.model.expression.UnknownParadoxDataExpression
                else -> element.referenceValue ?: return icu.windea.pls.model.expression.UnknownParadoxDataExpression
            }
            ParadoxDataExpressionImpl(valueElement.value, valueElement.type, valueElement.text.isLeftQuoted(), false)
        }
        else -> {
            val isKey = element is ParadoxScriptPropertyKey
            ParadoxDataExpressionImpl(element.value, element.type, element.text.isLeftQuoted(), isKey)
        }
    }
}

private fun doResolve(value: String, isQuoted: Boolean, isKey: Boolean?): ParadoxDataExpression {
    val expressionType = ParadoxType.resolve(value)
    return ParadoxDataExpressionImpl(value, expressionType, isQuoted, isKey)
}

private fun doResolve(text: String, isKey: Boolean?): ParadoxDataExpression {
    val value = text.unquote()
    val expressionType = ParadoxType.resolve(text)
    val quoted = text.isLeftQuoted()
    return ParadoxDataExpressionImpl(value, expressionType, quoted, isKey)
}

private class ParadoxDataExpressionImpl(
    override val text: String,
    override val type: ParadoxType,
    override val quoted: Boolean,
    override val isKey: Boolean?
) : ParadoxDataExpression {
    override fun equals(other: Any?): Boolean {
        return other is ParadoxDataExpression && (text == other.text && quoted == other.quoted)
    }
    
    override fun hashCode(): Int {
        return Objects.hash(text, type)
    }
    
    override fun toString(): String {
        return text
    }
}

private val BlockParadoxDataExpression = ParadoxDataExpressionImpl(PlsConstants.blockFolder, ParadoxType.Block, false, false)

private val UnknownParadoxDataExpression = ParadoxDataExpressionImpl(PlsConstants.unknownString, ParadoxType.Unknown, false, false)

