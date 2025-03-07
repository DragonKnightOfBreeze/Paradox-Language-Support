package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

@WithGameType(ParadoxGameType.Ck3)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Ck3.EventTree", storages = [Storage("paradox-language-support.xml")])
class Ck3EventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<Ck3EventTreeDiagramSettings.State>(project, State()) {
    companion object {
        const val ID = "pls.diagram.Ck3.EventTree"
    }

    override val id: String = ID

    class State : ParadoxDiagramSettings.State() {
        override var scopeType by string()

        @get:XMap
        var type by linkedMap<String, Boolean>()
        @get:XMap
        var eventType by linkedMap<String, Boolean>()

        val typeSettings = TypeSettings()

        inner class TypeSettings {
            val hidden by type withDefault true
        }
    }

    override val groupName: String = PlsDiagramBundle.message("ck3.eventTree.name")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val eventTypes = runReadAction { ParadoxEventManager.getTypes(project, ParadoxGameType.Ck3) }
        eventTypes.forEach { settings.eventType.putIfAbsent(it, true) }
        settings.updateSettings()

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, PlsDiagramBundle.message("ck3.eventTree.settings.type"), { key ->
            when (key) {
                State.TypeSettings::hidden.name -> PlsDiagramBundle.message("ck3.eventTree.settings.type.hidden")
                else -> null
            }
        })
        checkBoxGroup(settings.eventType, PlsDiagramBundle.message("ck3.eventTree.settings.eventType"), { key ->
            PlsDiagramBundle.message("ck3.eventTree.settings.eventType.option", key)
        })
    }
}
