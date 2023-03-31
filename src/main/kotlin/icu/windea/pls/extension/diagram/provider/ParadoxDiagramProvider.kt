package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.psi.*
import icu.windea.pls.lang.model.*

abstract class ParadoxDiagramProvider(
    val gameType: ParadoxGameType
) : DiagramProvider<PsiElement>() {
    companion object {
        private val EmptyDiagramVisibilityManager: DiagramVisibilityManager = EmptyDiagramVisibilityManager()
    }
    
    override fun createVisibilityManager() = EmptyDiagramVisibilityManager
}