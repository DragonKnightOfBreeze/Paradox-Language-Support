package icu.windea.pls.extensions.diagram.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.XMap
import icu.windea.pls.ChronicleDocBundle
import icu.windea.pls.base.annotations.WithGameType
import icu.windea.pls.extensions.diagram.ChronicleDiagramBundle
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ChronicleConstants

@WithGameType(ParadoxGameType.Eu5)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Eu5.EventTree", storages = [Storage(ChronicleConstants.pluginSettingsFileName)])
class Eu5EventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<Eu5EventTreeDiagramSettings.State>(project, State(), ParadoxGameType.Eu5) {
    companion object {
        const val ID = "chronicle.diagram.Eu5.EventTree"
    }

    override val id: String = ID

    class State : ParadoxDiagramSettings.State() {
        override var scopeType by string()

        @get:XMap
        var type by linkedMap<String, Boolean>()
        @get:XMap
        var attribute by linkedMap<String, Boolean>()
    }

    override val groupName: String = ChronicleDiagramBundle.message("eventTree.name.eu5")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val types = ParadoxEventManager.getAllTypes(gameType)
        settings.type.retainSettings(types)
        val attributes = ParadoxEventManager.getAllAttributes(gameType)
        settings.attribute.retainSettings(attributes)
        settings.updateSettings()

        row {
            label(ChronicleDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, ChronicleDiagramBundle.message("eventTree.settings.type"), { key ->
            ChronicleDocBundle.eventType(key, gameType)
        })
        checkBoxGroup(settings.attribute, ChronicleDiagramBundle.message("eventTree.settings.attribute"), { key ->
            ChronicleDocBundle.eventAttribute(key, gameType)
        })
    }
}
