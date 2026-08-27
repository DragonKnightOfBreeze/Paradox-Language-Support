package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.expressions.ParadoxConditionalExpression
import icu.windea.pls.script.psi.ParadoxConditionParameter
import java.util.*

/**
 * 参数上下文信息。
 */
class ParadoxParameterContextInfo(
    val parameters: Map<String, List<Parameter>>,
    val project: Project,
    val gameType: ParadoxGameType,
) {
    override fun toString(): String {
        return "ParadoxParameterContextInfo(parameters=$parameters, project=$project, gameType=$gameType)"
    }

    /**
     * @property conditionalExpressions 文件中从上到下，链表中从左到右，记录参数化块表达式的堆栈。如果 [element] 是 [ParadoxConditionParameter]，则应当为 null。
     */
    class Parameter(
        private val elementPointer: SmartPsiElementPointer<PsiElement>, // ParadoxConditionParameter | ParadoxParameter
        val name: String,
        val defaultValue: String? = null,
        val conditionalExpressions: Deque<ParadoxConditionalExpression>? = null,
        val project: Project,
        val gameType: ParadoxGameType,
    ) {
        val element: PsiElement? get() = elementPointer.element
        val parentElement: PsiElement? get() = elementPointer.element?.parent
        val parameterElement: ParadoxParameterLightElement? get() = elementPointer.element?.let { ParadoxParameterManager.getParameterElement(it) }

        override fun toString(): String {
            return "ParadoxParameterContextInfo.Parameter(name=$name, defaultValue=$defaultValue, project=$project, gameType=$gameType)"
        }
    }
}
