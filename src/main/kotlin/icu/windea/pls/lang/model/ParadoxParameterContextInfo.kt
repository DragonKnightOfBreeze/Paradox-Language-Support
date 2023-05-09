package icu.windea.pls.lang.model

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import java.util.*

class ParadoxParameterContextInfo(
    val parameters: Map<String, List<ParadoxParameterInfo>>
) {
    fun isOptional(parameterName: String, argumentNames: Set<String>? = null): Boolean {
        val parameterInfos = parameters.get(parameterName) ?: return true
        if(parameterInfos.isEmpty()) return true
        for(parameterInfo in parameterInfos) {
            if(parameterInfo.defaultValue != null) return true
            if(parameterInfo.conditionStack.isNotNullOrEmpty()) {
                val r = parameterInfo.conditionStack.all { rv ->
                    rv.where { parameterName == it || (argumentNames != null && argumentNames.contains(it)) }
                }
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
}