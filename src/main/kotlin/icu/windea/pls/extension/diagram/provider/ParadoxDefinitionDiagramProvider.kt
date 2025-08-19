package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.scope.type.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

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
