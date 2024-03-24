package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.ui.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

@WithGameType(ParadoxGameType.Vic3)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Vic3.EventTree", storages = [Storage("paradox-language-support.xml")])
class Vic3EventTreeDiagramSettings(
    val project: Project
) : ParadoxEventTreeDiagramSettings<Vic3EventTreeDiagramSettings.State>(State()) {
    companion object {
        const val ID = "pls.diagram.Vic3.EventTree"
    }
    
    override val id: String = ID
    override val configurableClass: Class<out Configurable> = Vic3EventTreeDiagramSettingsConfigurable::class.java
    
    class State : ParadoxDiagramSettings.State() {
        override var scopeType by string()
        
        @get:XMap
        var type by linkedMap<String, Boolean>()
        @get:XMap
        var eventType by linkedMap<String, Boolean>()
        
        val typeSettings = TypeSettings()
        
        inner class TypeSettings {
            val hidden  by type withDefault true
        }
    }
    
    override fun initSettings() {
        val eventTypes = ParadoxEventHandler.getTypes(project, ParadoxGameType.Vic3)
        eventTypes.forEach { state.eventType.putIfAbsent(it, true) }
    }
}

@WithGameType(ParadoxGameType.Vic3)
class Vic3EventTreeDiagramSettingsConfigurable(
    val project: Project
) : BoundConfigurable(PlsDiagramBundle.message("vic3.eventTree.name")), SearchableConfigurable {
    override fun getId() = Vic3EventTreeDiagramSettings.ID
    
    val settings = project.service<Vic3EventTreeDiagramSettings>().state
    
    fun initSettings() {
        project.service<Vic3EventTreeDiagramSettings>().initSettings()
    }
    
    override fun createPanel(): DialogPanel {
        initSettings()
        
        return panel {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
            }
            if(settings.type.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("vic3.eventTree.settings.type")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(UnscaledGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.type.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("vic3.eventTree.settings.type.${key}"))
                                .bindSelected(settings.type.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(UnscaledGaps(3, 0, 3, 0))
                        }
                    }
                }
            }
            if(settings.eventType.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("vic3.eventTree.settings.eventType")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(UnscaledGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.eventType.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("vic3.eventTree.settings.eventType.option", key))
                                .bindSelected(settings.eventType.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(UnscaledGaps(3, 0, 3, 0))
                        }
                    }
                }
            }
        }
    }
    
    override fun apply() {
        super.apply()
        settings.updateSettings()
    }
}