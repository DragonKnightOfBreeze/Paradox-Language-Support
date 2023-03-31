package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.settings.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class StellarisEventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Stellaris) {
    companion object {
        val nodeDataKey = Key.create<ParadoxEventDataProvider.Data>("paradox.eventTree.node.data")
    }
    
    private val _itemPropertyKeys = arrayOf(
        "picture",
        "hide_window", "is_triggered_only", "major", "diplomatic"
    )
    
    private val _additionalDiagramSettings = buildList {
        DiagramConfigGroup(PlsDiagramBundle.message("stellaris.eventTree.settings.type")).apply {
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.eventTree.settings.type.hidden"), true))
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.eventTree.settings.type.triggered"), true))
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.eventTree.settings.type.major"), true))
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.eventTree.settings.type.diplomatic"), true))
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.eventTree.settings.type.other"), true))
        }.also { add(it) }
    }.toTypedArray()
    
    override fun getItemPropertyKeys() = _itemPropertyKeys
    
    override fun getAdditionalDiagramSettings() = _additionalDiagramSettings
    
    override fun showNode(element: ParadoxScriptDefinitionElement): Boolean {
        if(element !is ParadoxScriptProperty) return false
        val data = element.getData<ParadoxEventDataProvider.Data>()
        if(data == null) return true
        val settings = getAdditionalDiagramSettings()
        if(settings.isEmpty()) return true
        val configuration = DiagramConfiguration.getInstance()
        
        val hidden = data.hide_window
        val triggered = data.is_triggered_only
        val major = data.major
        val diplomatic = data.diplomatic
        val other = !hidden && !triggered && !major && !diplomatic
        
        for(setting in settings) {
            when(setting.name) {
                PlsDiagramBundle.message("stellaris.eventTree.settings.type") -> {
                    val enabled = setting.elements.any { config ->
                        val e = configuration.isEnabledByDefault(this, config.name)
                        when(config.name) {
                            PlsDiagramBundle.message("stellaris.eventTree.settings.type.hidden") -> if(hidden) e else false
                            PlsDiagramBundle.message("stellaris.eventTree.settings.type.triggered") -> if(triggered) e else false
                            PlsDiagramBundle.message("stellaris.eventTree.settings.type.major") -> if(major) e else false
                            PlsDiagramBundle.message("stellaris.eventTree.settings.type.diplomatic") -> if(diplomatic) e else false
                            PlsDiagramBundle.message("stellaris.eventTree.settings.type.other") -> if(other) e else false
                            else -> false
                        }
                    }
                    if(!enabled) return false
                }
            }
        }
        return true
    }
    
    override fun handleNode(node: ParadoxDefinitionDiagramNode) {
        putDefinitionData(node, nodeDataKey)
    }
}