package icu.windea.pls.model

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import java.util.*

class ParadoxParameterContextInfo(
    val parameters: Map<String, List<Parameter>>,
    val project: Project,
    val gameType: ParadoxGameType
) {
    /**
     * @property conditionStack 文件中从上到下，链表中从左到右，记录条件表达式的堆栈。仅适用于[ParadoxParameter]。
     */
    class Parameter(
        private val elementPointer: SmartPsiElementPointer<PsiElement>, //ParadoxConditionParameter | ParadoxParameter
        val name: String,
        val defaultValue: String? = null,
        val conditionStack: Deque<ReversibleValue<String>>? = null,
    ) {
        val element: PsiElement? get() = elementPointer.element
        val parentElement: PsiElement? get() = elementPointer.element?.parent
        val parameterElement: ParadoxParameterElement? get() = elementPointer.element?.let { ParadoxParameterHandler.getParameterElement(it) }
        
        /**
         * 获取此参数对应的脚本表达式所对应的CWT规则列表。此参数可能整个作为一个脚本表达式，或者被一个脚本表达式所包含。
         */
        val expressionConfigs: List<CwtMemberConfig<*>>
            get() {
                val expressionElement = parentElement.castOrNull<ParadoxScriptStringExpressionElement>()
                if(expressionElement == null) return emptyList()
                return when {
                    expressionElement is ParadoxScriptPropertyKey -> {
                        val configs = ParadoxExpressionHandler.getConfigs(expressionElement)
                        configs.mapNotNull { if(it is CwtPropertyConfig) it else null }
                        configs
                    }
                    expressionElement is ParadoxScriptString && expressionElement.isExpression() -> {
                        val configs = ParadoxExpressionHandler.getConfigs(expressionElement)
                        configs.mapNotNull { if(it is CwtValueConfig) it else null }
                    }
                    else -> {
                        emptyList()
                    }
                }
            }
    }
}
