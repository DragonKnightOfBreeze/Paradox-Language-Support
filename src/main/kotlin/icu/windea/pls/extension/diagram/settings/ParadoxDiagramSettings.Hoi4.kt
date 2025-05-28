package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

@WithGameType(ParadoxGameType.Hoi4)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Hoi4.EventTree", storages = [Storage("paradox-language-support.xml")])
class Hoi4EventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<Hoi4EventTreeDiagramSettings.State>(project, State(), ParadoxGameType.Hoi4) {
    companion object {
        const val ID = "pls.diagram.Hoi4.EventTree"
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
        }
    }

    override val groupName: String = PlsDiagramBundle.message("eventTree.name.hoi4")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val types = runReadAction { ParadoxEventManager.getAllTypes(ParadoxGameType.Hoi4) }
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
