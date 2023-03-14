package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*

class ParadoxScriptConfigAwareInspectionSupressor: InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        TODO("Not yet implemented")
    }
    
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        TODO("Not yet implemented")
    }
}