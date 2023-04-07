package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyTreeDiagramSettingsConfigurable : BoundConfigurable(PlsDiagramBundle.message("paradox.technologyTree.name", ParadoxGameType.Stellaris)), SearchableConfigurable {
    val propertyGraph = PropertyGraph()
    
    override fun getId() = StellarisTechnologyTreeDiagramSettings.ID
    
    override fun createPanel(): DialogPanel {
        val settings = service<StellarisTechnologyTreeDiagramSettings>().state
        val typeState = propertyGraph.property(settings.typeState)
            .apply { afterChange { settings.typeState = it } }
        
        return panel {
            row {
                cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.type")))
                    .bindState(typeState)
            }
            settings.type.keys.forEach {
                row {
                    checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.${it}"))
                        .bindSelected(settings.type.toMutableProperty("it", true))
                        .actionListener { _, _ -> typeState.set(settings.typeState) }
                }
            }
        }
    }
    
    override fun apply() {
        super.apply()
        val settings = service<StellarisTechnologyTreeDiagramSettings>().state
        settings.updateSettings()
    }
}