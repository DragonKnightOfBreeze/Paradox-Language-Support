package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.DiagramProvider
import com.intellij.diagram.DiagramPsiScopeManager
import com.intellij.diagram.DiagramRelationshipManager
import com.intellij.diagram.DiagramScopeManager
import com.intellij.diagram.DiagramVfsResolver
import com.intellij.diagram.DiagramVisibilityManager
import com.intellij.diagram.EmptyDiagramVisibilityManager
import com.intellij.diagram.extras.DiagramExtras
import com.intellij.diagram.settings.DiagramConfigGroup
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.extension.diagram.ParadoxRelationshipManager
import icu.windea.pls.extension.diagram.ParadoxRootVfsResolver
import icu.windea.pls.extension.diagram.extras.ParadoxDiagramExtras
import icu.windea.pls.extension.diagram.settings.ParadoxDiagramSettings
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeType
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeTypes
import icu.windea.pls.model.ParadoxGameType

abstract class ParadoxDiagramProvider(
    val gameType: ParadoxGameType
) : DiagramProvider<PsiElement>() {
    companion object {
        private val EmptyDiagramVisibilityManager: DiagramVisibilityManager = EmptyDiagramVisibilityManager()
        private val VfsResolver = ParadoxRootVfsResolver()
    }

    private val _relationshipManager by lazy { ParadoxRelationshipManager() }
    private val _extra by lazy { ParadoxDiagramExtras(this) }

    override fun getVfsResolver(): DiagramVfsResolver<PsiElement> {
        return VfsResolver
    }

    override fun createVisibilityManager(): DiagramVisibilityManager {
        return EmptyDiagramVisibilityManager
    }

    override fun createScopeManager(project: Project): DiagramScopeManager<PsiElement>? {
        return DiagramPsiScopeManager(project)
    }

    override fun getRelationshipManager(): DiagramRelationshipManager<PsiElement> {
        return _relationshipManager
    }

    override fun getExtras(): DiagramExtras<PsiElement> {
        return _extra
    }

    open fun getAdditionalDiagramSettings(): Array<out DiagramConfigGroup> {
        return DiagramConfigGroup.EMPTY
    }

    open fun getScopeTypes(project: Project, context: PsiElement?): List<ParadoxSearchScopeType>? {
        return ParadoxSearchScopeTypes.getScopeTypes(project, context)
    }

    open fun getDiagramSettings(project: Project): ParadoxDiagramSettings<*>? {
        return null
    }
}
