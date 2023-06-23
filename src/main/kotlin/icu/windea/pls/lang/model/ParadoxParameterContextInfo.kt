package icu.windea.pls.lang.model

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Options
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.script.psi.*
import java.util.*

class ParadoxParameterContextInfo(
    val project: Project,
    val gameType: ParadoxGameType,
    val parameters: Map<String, List<ParadoxParameterInfo>>
) {
    fun isOptional(parameterName: String, argumentNames: Set<String>? = null): Boolean {
        val parameterInfos = parameters.get(parameterName)
        if(parameterInfos.isNullOrEmpty()) return true
        for(parameterInfo in parameterInfos) {
            //检查参数是否带有默认值
            if(parameterInfo.defaultValue != null) return true
            //检查参数的条件表达式上下文，基于参数名以及一组传入参数名，是否被认为是可选的
            if(parameterInfo.conditionStack.isNotNullOrEmpty()) {
                val r = parameterInfo.conditionStack.all { rv ->
                    rv.where { parameterName == it || (argumentNames != null && argumentNames.contains(it)) }
                }
                if(r) return true
            }
        }
        //基于参数对应的CWT规则，判断参数是否被认为是可选的
        for(parameterInfo in parameterInfos) {
            val configs = parameterInfo.expressionConfigs
            if(configs.isNotEmpty()) {
                //如果作为传入参数的值，直接认为是可选的，没有太大必要进一步检查……
                val r = configs.any { it is CwtValueConfig && it.propertyConfig?.expression?.type == CwtDataType.Parameter }
                if(r) return true
            }
        }
        return false
    }
}

/**
 * @property conditionStack，文件中从上到下，链表中从左到右，记录条件表达式的堆栈。
 */
class ParadoxParameterInfo(
    private val elementPointer: SmartPsiElementPointer<ParadoxParameter>,
    val name: String,
    val defaultValue: String?,
    val conditionStack: LinkedList<ReversibleValue<String>>? = null,
) {
    val element: ParadoxParameter? get() = elementPointer.element
    val expressionElement: ParadoxScriptStringExpressionElement? get() = elementPointer.element?.parent?.castOrNull()
    
    val rangeInExpressionElement: TextRange? by lazy {
        if(expressionElement == null) return@lazy null
        element?.textRangeInParent
    }
    
    val isEntireExpression: Boolean by lazy {
        val element = element
        element != null
            && element.prevSibling.let { it == null || it.text == "\"" }
            && element.nextSibling.let { it == null || it.text == "\"" }
    }
    
    /**
     * 获取此参数对应的脚本表达式所对应的CWT规则列表。此参数可能整个作为一个脚本表达式，或者被一个脚本表达式所包含。
     */
    val expressionConfigs: List<CwtMemberConfig<*>> by lazy {
        val parent = element?.parent
        when {
            parent is ParadoxScriptPropertyKey -> ParadoxConfigHandler.getConfigs(parent, matchOptions = Options.Default or Options.AcceptDefinition)
            parent is ParadoxScriptString -> ParadoxConfigHandler.getConfigs(parent, matchOptions = Options.Default or Options.AcceptDefinition)
            else -> emptyList()
        }
    }
}