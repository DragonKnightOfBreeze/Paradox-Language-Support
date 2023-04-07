package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.ui.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyTreeDiagramSettingsConfigurable(
    val project: Project
) : BoundConfigurable(PlsDiagramBundle.message("paradox.technologyTree.name", ParadoxGameType.Stellaris)), SearchableConfigurable {
    val propertyGraph = PropertyGraph()
    
    override fun getId() = StellarisTechnologyTreeDiagramSettings.ID
    
    override fun createPanel(): DialogPanel {
        initSettings()
        val settings = getSettings()
        val typeState = propertyGraph.property(settings.typeState)
            .apply { afterChange { settings.typeState = it } }
        val tierState = propertyGraph.property(settings.tierState)
            .apply { afterChange { settings.tierState = it } }
        val areaState = propertyGraph.property(settings.areaState)
            .apply { afterChange { settings.areaState = it } }
        val categoryState = propertyGraph.property(settings.categoryState)
            .apply { afterChange { settings.categoryState = it } }
        
        return panel {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
            }
            if(settings.type.isNotEmpty()) {
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.type")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .bindState(typeState)
                        .customize(JBGaps(3, 0, 3, 0))
                }
            }
            indent {
                settings.type.keys.forEach {
                    row {
                        checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.${it}"))
                            .bindSelected(settings.type.toMutableProperty(it, true))
                            .actionListener { _, _ -> typeState.set(settings.typeState) }
                            .customize(JBGaps(3, 0, 3, 0))
                    }
                }
            }
            if(settings.tier.isNotEmpty()) {
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .bindState(tierState)
                        .customize(JBGaps(3, 0, 3, 0))
                }
            }
            indent {
                settings.tier.keys.forEach {
                    row {
                        checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier.option", it))
                            .bindSelected(settings.tier.toMutableProperty(it, true))
                            .actionListener { _, _ -> tierState.set(settings.tierState) }
                            .customize(JBGaps(3, 0, 3, 0))
                    }
                }
            }
            if(settings.area.isNotEmpty()) {
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.area")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .bindState(areaState)
                        .customize(JBGaps(3, 0, 3, 0))
                }
            }
            indent {
                settings.area.keys.forEach {
                    row {
                        checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.area.option", it))
                            .bindSelected(settings.area.toMutableProperty(it, true))
                            .actionListener { _, _ -> areaState.set(settings.areaState) }
                            .customize(JBGaps(3, 0, 3, 0))
                    }
                }
            }
            if(settings.category.isNotEmpty()) {
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.category")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .bindState(categoryState)
                        .customize(JBGaps(3, 0, 3, 0))
                }
            }
            indent {
                settings.category.keys.forEach {
                    row {
                        checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.category.option", it))
                            .bindSelected(settings.category.toMutableProperty(it, true))
                            .actionListener { _, _ -> categoryState.set(settings.categoryState) }
                            .customize(JBGaps(3, 0, 3, 0))
                    }
                }
            }
        }
    }
    
    override fun apply() {
        super.apply()
        val settings = getSettings()
        settings.updateSettings()
    }
    
    private fun getSettings() = project.service<StellarisTechnologyTreeDiagramSettings>().state
    
    private fun initSettings() = project.service<StellarisTechnologyTreeDiagramSettings>().initSettings()
}