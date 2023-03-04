package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.psi.*

abstract class ParadoxDiagramProvider: DiagramProvider<PsiElement>() {
    override fun getVfsResolver() = ParadoxRootVfsResolver
    
    override fun createVisibilityManager() = EmptyDiagramVisibilityManager
}