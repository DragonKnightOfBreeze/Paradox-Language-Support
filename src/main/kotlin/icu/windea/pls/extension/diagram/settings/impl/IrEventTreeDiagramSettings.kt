package icu.windea.pls.extension.diagram.settings.impl

import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Ir)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Ir.EventTree", storages = [Storage("paradox-language-support.xml")])
class IrEventTreeDiagramSettings(
    val project: Project
) : ParadoxEventTreeDiagramSettings<IrEventTreeDiagramSettings.State>(State()) {
    companion object {
        const val ID = "pls.diagram.Ir.EventTree"
    }
    
    override val id: String = ID
    override val configurableClass: Class<out Configurable> = IrEventTreeDiagramSettingsConfigurable::class.java
    
    class State : ParadoxDiagramSettings.State() {
        override var scopeType by string()
        
        @get:XMap
        var type by linkedMap<String, Boolean>()
        @get:XMap
        var eventType by linkedMap<String, Boolean>()
        
        val typeSettings = TypeSettings()
        
        inner class TypeSettings {
            val hidden = type.getOrPut("hidden") { true }
        }
    }
    
    override fun initSettings() {
        val eventTypes = ParadoxEventHandler.getEventTypes(project, ParadoxGameType.Ir)
        eventTypes.forEach { state.eventType.putIfAbsent(it, true) }
    }
}