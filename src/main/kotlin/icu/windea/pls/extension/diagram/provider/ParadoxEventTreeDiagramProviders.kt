package icu.windea.pls.extension.diagram.provider

import com.intellij.openapi.components.*
import com.intellij.openapi.util.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class StellarisEventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Stellaris) {
    companion object {
        const val ID = "Stellaris.EventTree"
        val nodeDataKey = Key.create<ParadoxEventDataProvider.Data>("paradox.eventTree.node.data")
    }
    
    private val _itemPropertyKeys = arrayOf(
        "picture",
        "hide_window", "is_triggered_only", "major", "diplomatic"
    )
    
    override fun getID() = ID
    
    override fun getItemPropertyKeys() = _itemPropertyKeys
    
    override fun getDiagramSettings() = service<StellarisEventTreeDiagramSettings>()
    
    override fun showNode(definition: ParadoxScriptDefinitionElement): Boolean {
        if(definition !is ParadoxScriptProperty) return false
        val data = definition.getData<ParadoxEventDataProvider.Data>()
        if(data == null) return true
        val settings = getDiagramSettings().state
        
        val hidden = data.hide_window
        val triggered = data.is_triggered_only
        val major = data.major
        val diplomatic = data.diplomatic
        val other = !hidden && !triggered && !major && !diplomatic
        
        //对于每组配置，只要其中任意一个配置匹配即可
        with(settings.type) {
            var enabled = false
            if(hidden) enabled = enabled || this.hidden
            if(triggered) enabled = enabled || this.triggered
            if(major) enabled = enabled || this.major
            if(diplomatic) enabled = enabled || this.diplomatic
            if(other) enabled = enabled || this.other
            if(!enabled) return false
        }
        return true
    }
}