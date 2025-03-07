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

@WithGameType(ParadoxGameType.Ir)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Ir.EventTree", storages = [Storage("paradox-language-support.xml")])
class IrEventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<IrEventTreeDiagramSettings.State>(project, State()) {
    companion object {
        const val ID = "pls.diagram.Ir.EventTree"
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

    override val groupName: String = PlsDiagramBundle.message("ir.eventTree.name")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val eventTypes = runReadAction { ParadoxEventManager.getTypes(project, ParadoxGameType.Ir) }
        eventTypes.forEach { settings.eventType.putIfAbsent(it, true) }
        settings.updateSettings()

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, PlsDiagramBundle.message("ir.eventTree.settings.type"), { key ->
            when (key) {
                State.TypeSettings::hidden.name -> PlsDiagramBundle.message("ir.eventTree.settings.type.hidden")
                else -> null
            }
        })
        checkBoxGroup(settings.eventType, PlsDiagramBundle.message("ir.eventTree.settings.eventType"), { key ->
            PlsDiagramBundle.message("ir.eventTree.settings.eventType.option", key)
        })
    }
}
