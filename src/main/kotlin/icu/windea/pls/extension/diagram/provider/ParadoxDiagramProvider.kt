package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.settings.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import icu.windea.pls.core.search.scope.type.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.model.*

abstract class ParadoxDiagramProvider(
    val gameType: ParadoxGameType
) : DiagramProvider<PsiElement>() {
    companion object {
        private val EmptyDiagramVisibilityManager: DiagramVisibilityManager = EmptyDiagramVisibilityManager()
        private val VfsResolver = ParadoxRootVfsResolver()
    }
    
    override fun getVfsResolver(): DiagramVfsResolver<PsiElement> {
        return VfsResolver
    }
    
    override fun createVisibilityManager(): DiagramVisibilityManager {
        return EmptyDiagramVisibilityManager
    }
    
    override fun createScopeManager(project: Project): DiagramScopeManager<PsiElement>? {
        return DiagramPsiScopeManager(project)
    }
    
    open fun getAdditionalDiagramSettings(): Array<out DiagramConfigGroup> {
        return DiagramConfigGroup.EMPTY
    }
    
    open fun getScopeTypes(project: Project, context: PsiElement?): List<ParadoxSearchScopeType>? {
        return ParadoxSearchScopeTypes.getScopeTypes(project, context)
    }
    
    open fun getDiagramSettings(): ParadoxDiagramSettings<*>? {
        return null
    }
}