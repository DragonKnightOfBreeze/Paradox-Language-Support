package icu.windea.pls.lang.inspections

import com.intellij.psi.PsiElement
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement

object ParadoxSuppressionService {
    fun isSuppressedForDefinition(element: PsiElement, toolId: String): Boolean {
        if (element !is ParadoxDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false
        val suppressedToolIds = ParadoxInspectionService.getSuppressedToolIds(element, definitionInfo)
        return toolId in suppressedToolIds
    }
}
