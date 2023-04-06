package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

abstract class ParadoxDefinitionDiagramProvider(gameType: ParadoxGameType) : ParadoxDiagramProvider(gameType) {
    abstract fun getItemPropertyKeys(): Array<String>
    
    abstract fun getDefinitionType(): String
    
    override fun refreshDataModel(dataModel: ParadoxDiagramDataModel) {
        super.refreshDataModel(dataModel)
        val definitions = getDefinitions(dataModel, getDefinitionType())
        if(definitions.isEmpty()) return
        refreshDataModel(dataModel, definitions)
    }
    
    abstract fun refreshDataModel(dataModel: ParadoxDiagramDataModel, definitions: Set<ParadoxScriptDefinitionElement>)
    
    protected fun getDefinitions(dataModel: ParadoxDiagramDataModel, typeExpression: String): Set<ParadoxScriptDefinitionElement> {
        val searchScope = dataModel.scopeManager?.currentScope?.let { GlobalSearchScopes.filterScope(dataModel.project, it) }
        val searchScopeType = getDiagramSettings()?.state?.scopeType
        val selector = definitionSelector(dataModel.project, dataModel.originalFile)
            .withGameType(gameType)
            .withSearchScope(searchScope)
            .withSearchScopeType(searchScopeType)
            .contextSensitive()
            .distinctByName()
        return ParadoxDefinitionSearch.search(typeExpression, selector).findAll()
    }
    
    protected inline fun <reified T : ParadoxDefinitionData> putDefinitionData(node: Node, key: Key<T>) {
        val element = node.identifyingElement
        if(element !is ParadoxScriptProperty) return
        val data = element.getData<T>()
        node.putUserData(key, data)
    }
    
    open class Edge(
        override val source: ParadoxDefinitionDiagramProvider.Node,
        override val target: ParadoxDefinitionDiagramProvider.Node,
        relationship: DiagramRelationshipInfo
    ) : ParadoxDiagramEdge(source, target, relationship)
    
    
    open class Node(
        element: ParadoxScriptDefinitionElement,
        override val provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxDiagramNode(element, provider) {
        override fun getIdentifyingElement(): ParadoxScriptDefinitionElement {
            return super.getIdentifyingElement() as ParadoxScriptDefinitionElement
        }
        
        override open fun getTooltip(): String? {
            return identifyingElement.definitionInfo?.name
        }
    }
}