package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.settings.*
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

abstract class ParadoxDiagramProvider(
    val gameType: ParadoxGameType
) : DiagramProvider<PsiElement>() {
    companion object {
        private val EmptyDiagramVisibilityManager: DiagramVisibilityManager = EmptyDiagramVisibilityManager()
    }
    
    override fun createVisibilityManager(): DiagramVisibilityManager {
        return EmptyDiagramVisibilityManager
    }
    
    open fun getAdditionalDiagramSettings(): Array<out DiagramConfigGroup> {
        return DiagramConfigGroup.EMPTY
    }
}