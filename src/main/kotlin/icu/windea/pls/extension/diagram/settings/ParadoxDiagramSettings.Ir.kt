package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*

@WithGameType(ParadoxGameType.Ir)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Ir.EventTree", storages = [Storage("paradox-language-support.xml")])
class IrEventTreeDiagramSettings(
    val project: Project
) : ParadoxEventTreeDiagramSettings<IrEventTreeDiagramSettings.State>(State()) {
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

    override fun Panel.buildConfigurablePanel(coroutineScope: CoroutineScope) {
        val settings = state
        val eventTypes = ParadoxEventManager.getTypes(project, ParadoxGameType.Ir)
        eventTypes.forEach { settings.eventType.putIfAbsent(it, true) }
        settings.updateSettings()

        collapsibleGroup(PlsDiagramBundle.message("ir.eventTree.name")) {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
            }
            if (settings.type.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("ir.eventTree.settings.type")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .smaller()
                        .also { cb = it }
                }
                indent {
                    settings.type.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("ir.eventTree.settings.type.${key}"))
                                .bindSelected(settings.type.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .smaller()
                        }
                    }
                }
            }
            if (settings.eventType.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("ir.eventTree.settings.eventType")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .smaller()
                        .also { cb = it }
                }
                indent {
                    settings.eventType.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("ir.eventTree.settings.eventType.option", key))
                                .bindSelected(settings.eventType.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .smaller()
                        }
                    }
                }
            }
        }
    }
}
