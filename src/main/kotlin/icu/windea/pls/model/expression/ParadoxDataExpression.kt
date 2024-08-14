package icu.windea.pls.model.expression

import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * 数据表达式，对应脚本语言中的某处键/值。
 */
class ParadoxDataExpression private constructor(
    val text: String,
    val type: ParadoxType,
    val quoted: Boolean,
    val isKey: Boolean?
) {
    private val regex by lazy { text.toRegexWhenIsParameterized() }
    
    fun isParameterized(): Boolean {
        return type == ParadoxType.String && text.isParameterized()
    }
    
    fun isFullParameterized(): Boolean {
        return type == ParadoxType.String && text.isFullParameterized()
    }
    
    fun matchesConstant(v: String): Boolean {
        if(text.isParameterized()) {
            //兼容带参数的情况（此时先转化为正则表达式，再进行匹配）
            return regex.matches(v)
        }
        return text.equals(v, true) //忽略大小写
    }
    
    override fun equals(other: Any?): Boolean {
        return other is ParadoxDataExpression && (text == other.text && quoted == other.quoted)
    }
    
    override fun hashCode(): Int {
        return Objects.hash(text, type)
    }
    
    override fun toString(): String {
        return text
    }
    
    companion object Resolver {
        val BlockExpression: ParadoxDataExpression = ParadoxDataExpression(PlsConstants.Folders.block, ParadoxType.Block, false, false)
        val UnknownExpression: ParadoxDataExpression = ParadoxDataExpression(PlsConstants.unknownString, ParadoxType.Unknown, false, false)
        
        fun resolve(element: ParadoxScriptExpressionElement, matchOptions: Int = CwtConfigMatcher.Options.Default): ParadoxDataExpression {
            return when {
                element is ParadoxScriptBlock -> {
                    ParadoxDataExpression(PlsConstants.Folders.block, ParadoxType.Block, false, false)
                }
                element is ParadoxScriptScriptedVariableReference -> {
                    ProgressManager.checkCanceled()
                    val valueElement = when {
                        BitUtil.isSet(matchOptions, CwtConfigMatcher.Options.SkipIndex) -> return ParadoxDataExpression(PlsConstants.unknownString, ParadoxType.Unknown, false, false)
                        else -> element.referenceValue ?: return ParadoxDataExpression(PlsConstants.unknownString, ParadoxType.Unknown, false, false)
                    }
                    ParadoxDataExpression(valueElement.value, valueElement.type, valueElement.text.isLeftQuoted(), false)
                }
                else -> {
                    val isKey = element is ParadoxScriptPropertyKey
                    ParadoxDataExpression(element.value, element.type, element.text.isLeftQuoted(), isKey)
                }
            }
        }
        
        fun resolve(value: String, isQuoted: Boolean, isKey: Boolean? = null): ParadoxDataExpression {
            val expressionType = ParadoxType.resolve(value)
            return ParadoxDataExpression(value, expressionType, isQuoted, isKey)
        }
        
        fun resolve(text: String, isKey: Boolean? = null): ParadoxDataExpression {
            val value = text.unquote()
            val expressionType = ParadoxType.resolve(text)
            val quoted = text.isLeftQuoted()
            return ParadoxDataExpression(value, expressionType, quoted, isKey)
        }
    }
}
