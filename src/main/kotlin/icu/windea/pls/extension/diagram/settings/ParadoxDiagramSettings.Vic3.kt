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

@WithGameType(ParadoxGameType.Vic3)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Vic3.EventTree", storages = [Storage("paradox-language-support.xml")])
class Vic3EventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<Vic3EventTreeDiagramSettings.State>(project, State()) {
    companion object {
        const val ID = "pls.diagram.Vic3.EventTree"
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

    override val groupName: String = PlsDiagramBundle.message("vic3.eventTree.name")

    override suspend fun createGroup(panel: Panel) = with(panel) {
        val settings = state
        val eventTypes = readAction { ParadoxEventManager.getTypes(project, ParadoxGameType.Vic3) }
        eventTypes.forEach { settings.eventType.putIfAbsent(it, true) }
        settings.updateSettings()

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, PlsDiagramBundle.message("vic3.eventTree.settings.type"), { key ->
            when (key) {
                State.TypeSettings::hidden.name -> PlsDiagramBundle.message("vic3.eventTree.settings.type.hidden")
                else -> null
            }
        })
        checkBoxGroup(settings.eventType, PlsDiagramBundle.message("vic3.eventTree.settings.eventType"), { key ->
            PlsDiagramBundle.message("vic3.eventTree.settings.eventType.option", key)
        })
    }
}
