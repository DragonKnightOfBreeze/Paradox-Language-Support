package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.CheckboxTree
import com.intellij.ui.CheckboxTree.CheckboxTreeCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.ui.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventTreeDiagramSettingsConfigurable(
    val project: Project
) : BoundConfigurable(PlsDiagramBundle.message("paradox.eventTree.name", ParadoxGameType.Stellaris)), SearchableConfigurable {
    val propertyGraph = PropertyGraph()
    
    override fun getId() = StellarisEventTreeDiagramSettings.ID
    
    override fun createPanel(): DialogPanel {
        initSettings()
        val settings = getSettings()
        val typeState = propertyGraph.property(settings.typeState)
            .apply { afterChange { settings.typeState = it } }
        
        return panel {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
            }
            row {
                cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.eventTree.settings.type")))
                    .applyToComponent { isThirdStateEnabled = false }
                    .bindState(typeState)
            }.customize(JBVerticalGaps())
            indent {
                settings.type.keys.forEach {
                    row {
                        checkBox(PlsDiagramBundle.message("stellaris.eventTree.settings.type.${it}"))
                            .bindSelected(settings.type.toMutableProperty(it, true))
                            .actionListener { _, _ -> typeState.set(settings.typeState) }
                    }.customize(JBVerticalGaps())
                }
            }
        }
    }
    
    override fun apply() {
        super.apply()
        val settings = getSettings()
        settings.updateSettings()
    }
    
    private fun getSettings() = project.service<StellarisEventTreeDiagramSettings>().state
    
    private fun initSettings() = project.service<StellarisEventTreeDiagramSettings>().initSettings()
}