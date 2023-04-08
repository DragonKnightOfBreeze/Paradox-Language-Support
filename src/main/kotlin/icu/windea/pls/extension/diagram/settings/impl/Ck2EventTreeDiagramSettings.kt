package icu.windea.pls.extension.diagram.settings.impl

import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Ck2)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Ck2.EventTree", storages = [Storage("paradox-language-support.xml")])
class Ck2EventTreeDiagramSettings(
    val project: Project
) : ParadoxEventTreeDiagramSettings<Ck2EventTreeDiagramSettings.State>(State()) {
    companion object {
        const val ID = "pls.diagram.Ck2.EventTree"
    }
    
    override val id: String = ID
    override val configurableClass: Class<out Configurable> = Ck2EventTreeDiagramSettingsConfigurable::class.java
    
    class State : ParadoxDiagramSettings.State() {
        override var scopeType by string()
        
        @get:XMap
        var type by linkedMap<String, Boolean>()
        @get:XMap
        var eventType by linkedMap<String, Boolean>()
        
        val typeSettings = TypeSettings()
        
        inner class TypeSettings {
            val hidden = type.getOrPut("hidden") { true }
            val triggered = type.getOrPut("triggered") { true }
        }
    }
    
    override fun initSettings() {
        val eventTypes = ParadoxEventHandler.getEventTypes(project, ParadoxGameType.Ck2)
        eventTypes.forEach { state.eventType.putIfAbsent(it, true) }
    }
}