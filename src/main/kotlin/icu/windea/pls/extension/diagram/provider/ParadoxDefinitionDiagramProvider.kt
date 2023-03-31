package icu.windea.pls.extension.diagram.provider

import com.intellij.openapi.util.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

abstract class ParadoxDefinitionDiagramProvider(gameType: ParadoxGameType) : ParadoxDiagramProvider(gameType) {
    abstract fun getItemPropertyKeys(): Array<String>
    
    open fun showNode(element: ParadoxScriptDefinitionElement): Boolean {
        return true
    }
    
    open fun handleNode(node: ParadoxDefinitionDiagramNode) {
        
    }
    
    open fun handleEdge(edge: ParadoxDefinitionDiagramEdge) {
        
    }
    
    inline fun <reified T : ParadoxDefinitionData> putDefinitionData(node: ParadoxDefinitionDiagramNode, key: Key<T>) {
        val element = node.identifyingElement
        if(element !is ParadoxScriptProperty) return
        val data = element.getData<T>()
        node.putUserData(key, data)
    }
}