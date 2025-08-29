package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.XMap
import icu.windea.pls.PlsDocBundle
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.collections.provideDelegate
import icu.windea.pls.core.collections.withDefault
import icu.windea.pls.extension.diagram.PlsDiagramBundle
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsConstants

@WithGameType(ParadoxGameType.Vic2)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Vic2.EventTree", storages = [Storage(PlsConstants.pluginSettingsFileName)])
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
        var attribute by linkedMap<String, Boolean>()

        val attributeSettings = AttributeSettings()

        inner class AttributeSettings {
            val triggered by attribute withDefault true
            val major by attribute withDefault true
        }
    }

    override val groupName: String = PlsDiagramBundle.message("eventTree.name.vic2")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val types = runReadAction { ParadoxEventManager.getAllTypes(ParadoxGameType.Vic2) }
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
