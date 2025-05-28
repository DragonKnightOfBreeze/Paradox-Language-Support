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

@WithGameType(ParadoxGameType.Ck3)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Ck3.EventTree", storages = [Storage("paradox-language-support.xml")])
class Ck3EventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<Ck3EventTreeDiagramSettings.State>(project, State(), ParadoxGameType.Ck3) {
    companion object {
        const val ID = "pls.diagram.Ck3.EventTree"
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

    override val groupName: String = PlsDiagramBundle.message("eventTree.name.ck3")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val types = runReadAction { ParadoxEventManager.getAllTypes(ParadoxGameType.Ck3) }
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
