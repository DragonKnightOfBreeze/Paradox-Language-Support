package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.psi.*

abstract class ParadoxDiagramProvider : DiagramProvider<PsiElement>() {
    companion object {
        private val EmptyDiagramVisibilityManager: DiagramVisibilityManager = EmptyDiagramVisibilityManager()
    }
    
    override fun createVisibilityManager() = EmptyDiagramVisibilityManager
}