package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

abstract class ParadoxDefinitionDiagramProvider(gameType: ParadoxGameType) : ParadoxDiagramProvider(gameType) {
    abstract fun getItemPropertyKeys(): Array<String>
    
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
            return definitionInfo?.name?.orAnonymous()
        }
    }
    
    abstract class DataModel(
        project: Project,
        file: VirtualFile?,
        provider: ParadoxDiagramProvider,
    ): ParadoxDiagramDataModel(project, file, provider)  {
        protected fun getDefinitions(typeExpression: String): Set<ParadoxScriptDefinitionElement> {
            val searchScope = scopeManager?.currentScope?.let { GlobalSearchScopes.filterScope(project, it) }
            val searchScopeType = provider.getDiagramSettings(project)?.state?.scopeType
            val selector = definitionSelector(project, originalFile)
                .withGameType(gameType)
                .withSearchScope(searchScope)
                .withSearchScopeType(searchScopeType)
                .contextSensitive()
                .distinctByName()
            return ParadoxDefinitionSearch.search(typeExpression, selector).findAll()
        }
    }
}