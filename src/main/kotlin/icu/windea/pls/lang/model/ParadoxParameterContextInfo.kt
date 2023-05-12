package icu.windea.pls.lang.model

import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import java.util.*

class ParadoxParameterContextInfo(
    val parameters: Map<String, List<ParadoxParameterInfo>>
) {
    fun isOptional(parameterName: String, argumentNames: Set<String>? = null): Boolean {
        val parameterInfos = parameters.get(parameterName) ?: return true
        if(parameterInfos.isEmpty()) return true
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
    
    val configs: List<CwtDataConfig<*>> by lazy {
        val parent = element?.parent
        when {
            parent is ParadoxScriptPropertyKey -> ParadoxConfigHandler.getConfigs(parent)
            parent is ParadoxScriptString -> ParadoxConfigHandler.getConfigs(parent)
            else -> emptyList()
        }
    }
}