package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.XMap
import icu.windea.pls.PlsDocBundle
import icu.windea.pls.core.collections.provideDelegate
import icu.windea.pls.core.collections.withDefault
import icu.windea.pls.extension.diagram.PlsDiagramBundle
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsConstants

@WithGameType(ParadoxGameType.Eu5)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Eu5.EventTree", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class Eu5EventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<Eu5EventTreeDiagramSettings.State>(project, State(), ParadoxGameType.Eu5) {
    companion object {
        const val ID = "pls.diagram.Eu5.EventTree"
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

    override val groupName: String = PlsDiagramBundle.message("eventTree.name.eu5")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val types = runReadAction { ParadoxEventManager.getAllTypes(ParadoxGameType.Eu5) }
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
