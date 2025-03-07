package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

@WithGameType(ParadoxGameType.Hoi4)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Hoi4.EventTree", storages = [Storage("paradox-language-support.xml")])
class Hoi4EventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<Hoi4EventTreeDiagramSettings.State>(project, State()) {
    companion object {
        const val ID = "pls.diagram.Hoi4.EventTree"
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

    override val groupName: String = PlsDiagramBundle.message("hoi4.eventTree.name")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val eventTypes = ParadoxEventManager.getTypes(project, ParadoxGameType.Hoi4)
        eventTypes.forEach { settings.eventType.putIfAbsent(it, true) }
        settings.updateSettings()

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, PlsDiagramBundle.message("hoi4.eventTree.settings.type"), { key ->
            when (key) {
                State.TypeSettings::hidden.name -> PlsDiagramBundle.message("hoi4.eventTree.settings.type.hidden")
                else -> null
            }
        })
        checkBoxGroup(settings.eventType, PlsDiagramBundle.message("hoi4.eventTree.settings.eventType"), { key ->
            PlsDiagramBundle.message("hoi4.eventTree.settings.eventType.option", key)
        })
    }
}
