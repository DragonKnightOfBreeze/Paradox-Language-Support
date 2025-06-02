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
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

@WithGameType(ParadoxGameType.Eu4)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Eu4.EventTree", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class Eu4EventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<Eu4EventTreeDiagramSettings.State>(project, State(), ParadoxGameType.Eu4) {
    companion object {
        const val ID = "pls.diagram.Eu4.EventTree"
    }

    override val id: String = ID

    class State : ParadoxDiagramSettings.State() {
        override var scopeType by string()

        @get:XMap
        var type by linkedMap<String, Boolean>()
        @get:XMap
        var attribute by linkedMap<String, Boolean>()

        val attributeSettings = AttributeSettings()

        inner class AttributeSettings {
            val hidden by attribute withDefault true
            val triggered by attribute withDefault true
        }
    }

    override val groupName: String = PlsDiagramBundle.message("eventTree.name.eu4")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val types = runReadAction { ParadoxEventManager.getAllTypes(ParadoxGameType.Eu4) }
        settings.type.retainSettings(types)
        settings.updateSettings()

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, PlsDiagramBundle.message("eventTree.settings.type"), { key ->
            PlsDocBundle.eventType(key, gameType)
        })
        checkBoxGroup(settings.attribute, PlsDiagramBundle.message("eventTree.settings.attribute"), { key ->
            PlsDocBundle.eventAttribute(key, gameType)
        })
    }
}

