package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression
import java.util.*

class ParadoxParameterContextInfo(
    val parameters: Map<String, List<Parameter>>,
    val project: Project,
    val gameType: ParadoxGameType
) {
    /**
     * @property conditionStack 文件中从上到下，链表中从左到右，记录条件表达式的堆栈。如果[element]不是[ParadoxParameter]，则应当为null。
     */
    class Parameter(
        private val elementPointer: SmartPsiElementPointer<PsiElement>, //ParadoxConditionParameter | ParadoxParameter
        val name: String,
        val defaultValue: String? = null,
        val conditionStack: Deque<ReversibleValue<String>>? = null,
    ) {
        val element: PsiElement? get() = elementPointer.element
        val parentElement: PsiElement? get() = elementPointer.element?.parent
        val parameterElement: ParadoxParameterElement? get() = elementPointer.element?.let { ParadoxParameterManager.getParameterElement(it) }

        /**
         * 获取此参数对应的脚本表达式所对应的上下文规则列表。
         */
        val expressionContextConfigs: List<CwtMemberConfig<*>>
            get() {
                val expressionElement = parentElement?.castOrNull<ParadoxScriptStringExpressionElement>()
                if (expressionElement == null) return emptyList()
                if (!expressionElement.value.isParameterized(full = true)) return emptyList()
                val expressionContextConfigs = ParadoxExpressionManager.getConfigContext(expressionElement)?.getConfigs()
                return expressionContextConfigs.orEmpty()
            }

        /**
         * 获取此参数对应的脚本表达式所对应的规则列表。此参数可能整个作为一个脚本表达式，或者被一个脚本表达式所包含。
         */
        val expressionConfigs: List<CwtMemberConfig<*>>
            get() {
                val expressionElement = parentElement.castOrNull<ParadoxScriptStringExpressionElement>()
                if (expressionElement == null) return emptyList()
                return when {
                    expressionElement is ParadoxScriptPropertyKey -> {
                        val configs = ParadoxExpressionManager.getConfigs(expressionElement)
                        configs.filterIsInstance<CwtPropertyConfig>()
                    }
                    expressionElement is ParadoxScriptString && expressionElement.isExpression() -> {
                        val configs = ParadoxExpressionManager.getConfigs(expressionElement)
                        configs.filterIsInstance<CwtValueConfig>()
                    }
                    else -> {
                        emptyList()
                    }
                }
            }
    }
}
