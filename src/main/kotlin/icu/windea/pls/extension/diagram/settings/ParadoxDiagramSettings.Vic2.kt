package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

@WithGameType(ParadoxGameType.Vic2)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Vic2.EventTree", storages = [Storage("paradox-language-support.xml")])
class Vic2EventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<Vic2EventTreeDiagramSettings.State>(project, State(), ParadoxGameType.Vic2) {
    companion object {
        const val ID = "pls.diagram.Vic2.EventTree"
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
            val triggered by type withDefault true
            val major by type withDefault true
        }
    }

    override val groupName: String = PlsDiagramBundle.message("eventTree.name.vic2")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val eventTypes = runReadAction { ParadoxEventManager.getTypes(project, ParadoxGameType.Vic2) }
        eventTypes.forEach { settings.eventType.putIfAbsent(it, true) }
        settings.updateSettings()

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, PlsDiagramBundle.message("eventTree.settings.type"), { key ->
            PlsDocBundle.eventType(key, gameType)
        })
        checkBoxGroup(settings.eventType, PlsDiagramBundle.message("eventTree.settings.eventType"), { key ->
            PlsDiagramBundle.message("eventTree.settings.eventType.option", key)
        })
    }
}
