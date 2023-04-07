package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.ThreeStateCheckBox
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventTreeDiagramSettingsConfigurable : BoundConfigurable(PlsDiagramBundle.message("paradox.eventTree.name", ParadoxGameType.Stellaris)), SearchableConfigurable {
    val propertyGraph = PropertyGraph()
    
    override fun getId() = StellarisEventTreeDiagramSettings.ID
    
    override fun createPanel(): DialogPanel {
        val settings = service<StellarisEventTreeDiagramSettings>().state
        val typeState = propertyGraph.property(settings.typeState)
            .apply { afterChange { settings.typeState = it } }
        
        fun Cell<JBCheckBox>.bindTypeState() = actionListener { _, _ -> typeState.set(settings.typeState) }
        
        return panel {
            row {
                cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.eventTree.settings.type")))
                    .bindState(typeState)
            }
            settings.type.keys.forEach {
                row {
                    checkBox(PlsDiagramBundle.message("stellaris.eventTree.settings.type.${it}"))
                        .bindSelected(settings.type.toMutableProperty("it", true))
                        .bindTypeState()
                }
            }
        }
    }
    
    override fun apply() {
        super.apply()
        val settings = service<StellarisEventTreeDiagramSettings>().state
        settings.updateSettings()
    }
}