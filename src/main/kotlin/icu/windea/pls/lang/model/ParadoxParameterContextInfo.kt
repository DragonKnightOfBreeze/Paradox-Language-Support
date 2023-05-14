package icu.windea.pls.lang.model

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*
import java.util.*

class ParadoxParameterContextInfo(
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
            val configs = parameterInfo.configs
            if(configs.isNotEmpty()) {
                //如果作为传入参数的值，直接认为是可选的，没有太大必要进一步检查……
                val r = configs.any { it is CwtValueConfig && it.propertyConfig?.expression?.type == CwtDataType.Parameter }
                if(r) return true
            }
        }
        return false
    }
    
    fun getEntireConfig(parameterName: String): CwtValueConfig? {
        //如果推断得到的规则不唯一，则返回null
        val parameterInfos = parameters.get(parameterName)
        if(parameterInfos.isNullOrEmpty()) return null
        var result: CwtValueConfig? = null
        var passingResult: CwtValueConfig? = null
        for(parameterInfo in parameterInfos) {
            if(parameterInfo.template != "$") continue //要求整个作为脚本表达式
            val configs = parameterInfo.configs
            val config = configs.firstOrNull() as? CwtValueConfig ?: continue
            when(config.expression.type) {
                CwtDataType.ParameterValue -> {
                    //处理参数传递的情况
                    //这里需要尝试避免SOE
                    val passingConfig = withRecursionGuard("ParadoxParameterContextInfo.getEntireConfig") action@{
                        val argumentNameElement = parameterInfo.element?.parent?.castOrNull<ParadoxScriptValue>()?.propertyKey ?: return@action null
                        val argumentNameConfig = config.propertyConfig ?: return@action null
                        val passingParameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return@action null
                        withCheckRecursion(passingParameterElement.contextKey) {
                            ParadoxParameterHandler.inferEntireConfig(passingParameterElement)
                        }
                    } ?: continue
                    if(passingResult == null) {
                        passingResult = passingConfig
                    } else {
                        if(passingResult.expression != passingConfig.expression) {
                            passingResult = null
                            break
                        }
                    }
                }
                CwtDataType.Any, CwtDataType.Other -> {
                    //任意类型或者其他类型 - 忽略
                    pass()
                }
                else -> {
                    if(result == null) {
                        result = config
                    } else {
                        if(result.expression != config.expression) {
                            result = null
                            break
                        }
                    }
                }
            }
        }
        return result ?: passingResult
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
    
    /**
     * 获取模版表达式，用于此参数在整个脚本表达式中的位置。用$表示此参数，用#表示其他参数。
     */
    val template: String by lazy {
        val element = element ?: return@lazy "$"
        val builder = StringBuilder("$")
        element.siblings(forward = false, withSelf = false).forEach {
            builder.insert(0, doGetTemplateSnippet(it))
        }
        element.siblings(forward = true, withSelf = false).forEach {
            builder.append(doGetTemplateSnippet(it))
        }
        builder.toString()
    }
    
    private fun doGetTemplateSnippet(it: PsiElement): String {
        val elementType = it.elementType
        val s = when(elementType) {
            ParadoxScriptElementTypes.PARAMETER -> "#"
            else -> it.text
        }
        return s
    }
    
    /**
     * 获取此参数对应的脚本表达式所对应的CWT规则列表。此参数可能整个作为一个脚本表达式，或者被一个脚本表达式所包含。
     */
    val configs: List<CwtDataConfig<*>> by lazy {
        val parent = element?.parent
        when {
            parent is ParadoxScriptPropertyKey -> ParadoxConfigHandler.getConfigs(parent)
            parent is ParadoxScriptString -> ParadoxConfigHandler.getConfigs(parent)
            else -> emptyList()
        }
    }
}