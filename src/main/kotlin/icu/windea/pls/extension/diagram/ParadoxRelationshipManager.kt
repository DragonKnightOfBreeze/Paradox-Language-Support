package icu.windea.pls.extension.diagram

import com.intellij.diagram.DiagramCategory
import com.intellij.diagram.DiagramRelationshipInfo
import com.intellij.diagram.DiagramRelationshipManager
import com.intellij.psi.PsiElement

class ParadoxRelationshipManager : DiagramRelationshipManager<PsiElement> {
    override fun getDependencyInfo(s: PsiElement?, t: PsiElement?, category: DiagramCategory?): DiagramRelationshipInfo? {
        return null
    }
}
