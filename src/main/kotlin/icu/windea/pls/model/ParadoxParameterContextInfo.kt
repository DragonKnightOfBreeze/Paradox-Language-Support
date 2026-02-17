package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.resolve.expression.ParadoxParameterConditionExpression
import icu.windea.pls.lang.util.ParadoxParameterManager
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
    /**
     * @property conditionExpressions 文件中从上到下，链表中从左到右，记录参数条件表达式的堆栈。如果 [element] 是 [ParadoxConditionParameter]，则应当为 null。
     */
    class Parameter(
        private val elementPointer: SmartPsiElementPointer<PsiElement>, // ParadoxConditionParameter | ParadoxParameter
        val name: String,
        val defaultValue: String? = null,
        val conditionExpressions: Deque<ParadoxParameterConditionExpression>? = null,
    ) {
        val element: PsiElement? get() = elementPointer.element
        val parentElement: PsiElement? get() = elementPointer.element?.parent
        val parameterElement: ParadoxParameterElement? get() = elementPointer.element?.let { ParadoxParameterManager.getParameterElement(it) }
    }
}
