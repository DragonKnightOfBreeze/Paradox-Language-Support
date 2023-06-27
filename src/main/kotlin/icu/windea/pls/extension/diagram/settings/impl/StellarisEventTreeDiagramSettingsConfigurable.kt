package icu.windea.pls.extension.diagram.settings.impl

import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventTreeDiagramSettingsConfigurable(
    val project: Project
) : BoundConfigurable(PlsDiagramBundle.message("stellaris.eventTree.name")), SearchableConfigurable {
    override fun getId() = StellarisEventTreeDiagramSettings.ID
    
    val settings = project.service<StellarisEventTreeDiagramSettings>().state
    
    fun initSettings() {
        project.service<StellarisEventTreeDiagramSettings>().initSettings()
    }
    
    override fun createPanel(): DialogPanel {
        initSettings()
        
        return panel {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
            }
            if(settings.type.isNotEmpty()) {
                lateinit var cb : Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.eventTree.settings.type")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(JBGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.type.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.eventTree.settings.type.${key}"))
                                .bindSelected(settings.type.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(JBGaps(3, 0, 3, 0))
                        }
                    }
                }
            }
            if(settings.eventType.isNotEmpty()) {
                lateinit var cb : Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.eventTree.settings.eventType")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(JBGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.eventType.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.eventTree.settings.eventType.option", key))
                                .bindSelected(settings.eventType.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(JBGaps(3, 0, 3, 0))
                        }
                    }
                }
            }
        }
    }
    
    override fun apply() {
        super.apply()
        settings.updateSettings()
    }
}
