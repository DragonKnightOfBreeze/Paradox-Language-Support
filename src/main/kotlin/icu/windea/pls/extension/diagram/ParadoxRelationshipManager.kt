package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.psi.*

class ParadoxRelationshipManager : DiagramRelationshipManager<PsiElement> {
    override fun getDependencyInfo(s: PsiElement?, t: PsiElement?, category: DiagramCategory?): DiagramRelationshipInfo? {
        return null
    }
}