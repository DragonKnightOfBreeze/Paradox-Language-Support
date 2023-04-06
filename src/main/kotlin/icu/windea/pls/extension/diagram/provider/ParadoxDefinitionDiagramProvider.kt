package icu.windea.pls.extension.diagram.provider

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
    
    abstract class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ): ParadoxDiagramDataModel(project, file, provider) {
        private val _nodes = mutableSetOf<ParadoxDefinitionDiagramNode>()
        private val _edges = mutableSetOf<ParadoxDefinitionDiagramEdge>()
        
        override fun getNodes() = _nodes
        
        override fun getEdges() = _edges
        
        abstract fun getDefinitionType(): String
        
        fun getDefinitions(typeExpression: String): Set<ParadoxScriptDefinitionElement> {
            val searchScope = scopeManager?.currentScope?.let { GlobalSearchScopes.filterScope(project, it) }
            val searchScopeType = provider.getDiagramSettings()?.state?.scopeType
            val selector = definitionSelector(project, originalFile)
                .withGameType(gameType)
                .withSearchScope(searchScope)
                .withSearchScopeType(searchScopeType)
                .contextSensitive()
                .distinctByName()
            return ParadoxDefinitionSearch.search(typeExpression, selector).findAll()
        }
        
        abstract fun showNode(definition: ParadoxScriptDefinitionElement): Boolean
        
        abstract fun createNode(definition: ParadoxScriptDefinitionElement): ParadoxDefinitionDiagramNode
        
        abstract fun createEdges(definitionMap: Map<String, ParadoxScriptDefinitionElement>, nodeMap: Map<ParadoxScriptDefinitionElement, ParadoxDefinitionDiagramNode>): Set<ParadoxDefinitionDiagramEdge>
        
        inline fun <reified T : ParadoxDefinitionData> putDefinitionData(node: ParadoxDefinitionDiagramNode, key: Key<T>) {
            val element = node.identifyingElement
            if(element !is ParadoxScriptProperty) return
            val data = element.getData<T>()
            node.putUserData(key, data)
        }
        
        override fun refreshDataModel() {
            provider as ParadoxDefinitionDiagramProvider
            
            ProgressManager.checkCanceled()
            nodes.clear()
            edges.clear()
            val definitions = getDefinitions(getDefinitionType())
            if(definitions.isEmpty()) return
            val nodeMap = mutableMapOf<ParadoxScriptDefinitionElement, ParadoxDefinitionDiagramNode>()
            val definitionMap = mutableMapOf<String, ParadoxScriptDefinitionElement>()
            for(definition in definitions) {
                ProgressManager.checkCanceled()
                if(!showNode(definition)) continue
                val node = createNode(definition)
                nodeMap.put(definition, node)
                val definitionName = definition.definitionInfo?.name.orAnonymous()
                definitionMap.put(definitionName, definition)
                nodes.add(node)
            }
            val createdEdges = createEdges(definitionMap, nodeMap)
            edges.addAll(createdEdges)
        }
    }
}