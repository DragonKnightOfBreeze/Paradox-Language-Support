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
 * 数据表达式，对应脚本语言中的某处键/值。
 */
interface ParadoxDataExpression {
    val value: String
    val type: ParadoxType
    val quoted: Boolean
    val isKey: Boolean?
    
    fun isParameterized(): Boolean
    fun matchesConstant(v: String): Boolean
    
    companion object Resolver {
        val BlockExpression: ParadoxDataExpression = BlockParadoxDataExpression
        val UnknownExpression: ParadoxDataExpression = UnknownParadoxDataExpression
        
        fun resolve(element: ParadoxScriptExpressionElement, matchOptions: Int = CwtConfigMatcher.Options.Default): ParadoxDataExpression =
            doResolve(element, matchOptions)
        
        fun resolve(value: String, isQuoted: Boolean, isKey: Boolean? = null): ParadoxDataExpression =
            doResolve(value, isQuoted, isKey)
        
        fun resolve(text: String, isKey: Boolean? = null): ParadoxDataExpression =
            doResolve(text, isKey)
    }
}

//Implementations

private fun doResolve(element: ParadoxScriptExpressionElement, matchOptions: Int): ParadoxDataExpression {
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
    override val value: String,
    override val type: ParadoxType,
    override val quoted: Boolean,
    override val isKey: Boolean?
) : ParadoxDataExpression {
    private val regexWhenIsParameterized by lazy { value.toRegexWhenIsParameterized() }
    
    override fun isParameterized(): Boolean {
        return type == ParadoxType.String && value.isParameterized()
    }
    
    override fun matchesConstant(v: String): Boolean {
        if(value.isParameterized()) {
            //兼容带参数的情况（此时先转化为正则表达式，再进行匹配）
            return regexWhenIsParameterized.matches(v)
        }
        return value.equals(v, true) //忽略大小写
    }
    
    override fun equals(other: Any?): Boolean {
        return other is ParadoxDataExpression && (value == other.value && quoted == other.quoted)
    }
    
    override fun hashCode(): Int {
        return Objects.hash(value, type)
    }
    
    override fun toString(): String {
        return value
    }
}

private val BlockParadoxDataExpression = ParadoxDataExpressionImpl(PlsConstants.Folders.block, ParadoxType.Block, false, false)

private val UnknownParadoxDataExpression = ParadoxDataExpressionImpl(PlsConstants.unknownString, ParadoxType.Unknown, false, false)

