package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.DiagramRelationshipInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScopes
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.extension.diagram.ParadoxDiagramDataModel
import icu.windea.pls.extension.diagram.ParadoxDiagramEdge
import icu.windea.pls.extension.diagram.ParadoxDiagramElementManager
import icu.windea.pls.extension.diagram.ParadoxDiagramNode
import icu.windea.pls.extension.diagram.settings.ParadoxDiagramSettings
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeTypes
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.lang.search.selector.withSearchScopeType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile

abstract class ParadoxDefinitionDiagramProvider(gameType: ParadoxGameType) : ParadoxDiagramProvider(gameType) {
    open fun getItemPropertyKeys(): List<String> = emptyList()

    open fun getItemPropertyKeysInDetail(): List<String> = emptyList()

    open class Edge(
        override val source: Node,
        override val target: Node,
        relationship: DiagramRelationshipInfo
    ) : ParadoxDiagramEdge(source, target, relationship)

    open class Node(
        element: ParadoxScriptDefinitionElement,
        override val provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxDiagramNode(element, provider) {
        val definitionInfo get() = identifyingElement.definitionInfo

        override fun getIdentifyingElement(): ParadoxScriptDefinitionElement {
            return super.getIdentifyingElement() as ParadoxScriptDefinitionElement
        }

        override fun getTooltip(): String? {
            return definitionInfo?.name?.or?.anonymous()
        }
    }

    abstract class ElementManager(
        override val provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxDiagramElementManager(provider)

    abstract class DataModel(
        project: Project,
        file: VirtualFile?,
        override val provider: ParadoxDefinitionDiagramProvider,
    ) : ParadoxDiagramDataModel(project, file, provider) {
        protected fun getDefinitions(typeExpression: String): Set<ParadoxScriptDefinitionElement> {
            val originalFile = originalFile
            val searchScope = scopeManager?.currentScope?.let { GlobalSearchScopes.filterScope(project, it) }
            val searchScopeType = provider.getDiagramSettings(project)?.state?.scopeType?.orNull()
            val finalSearchScopeType = when {
                searchScopeType == ParadoxSearchScopeTypes.File.id && originalFile?.language !is ParadoxBaseLanguage -> null
                searchScopeType != null -> searchScopeType
                originalFile is ParadoxScriptFile -> ParadoxSearchScopeTypes.File.id
                else -> null
            }
            val selector = selector(project, originalFile).definition()
                .withGameType(gameType)
                .withSearchScope(searchScope)
                .withSearchScopeType(finalSearchScopeType)
                .contextSensitive()
                .distinctByName()
            return ParadoxDefinitionSearch.search(typeExpression, selector).findAll()
        }

        protected abstract fun showNode(definition: ParadoxScriptDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean
    }
}
