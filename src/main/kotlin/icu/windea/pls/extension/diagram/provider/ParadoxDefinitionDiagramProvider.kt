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
    abstract fun getItemPropertyKeys(): Array<String>

    protected fun getProperties(nodeElement: ParadoxScriptProperty): Set<ParadoxScriptProperty> {
        val itemPropertyKeys = getItemPropertyKeys()
        val properties = sortedSetOf<ParadoxScriptProperty>(compareBy { itemPropertyKeys.indexOf(it.name.lowercase()) })
        nodeElement.block?.properties(conditional = true, inline = true)?.forEach {
            if (it.name.lowercase() in itemPropertyKeys) properties.add(it)
        }
        return properties
    }

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

    abstract class DataModel(
        project: Project,
        file: VirtualFile?,
        provider: ParadoxDiagramProvider,
    ) : ParadoxDiagramDataModel(project, file, provider) {
        protected fun getDefinitions(typeExpression: String): Set<ParadoxScriptDefinitionElement> {
            val originalFile = originalFile
            val searchScope = scopeManager?.currentScope?.let { GlobalSearchScopes.filterScope(project, it) }
            val searchScopeType = provider.getDiagramSettings(project)?.state?.scopeType?.orNull()
            val finalSearchScopeType = when {
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
