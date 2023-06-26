package icu.windea.pls.extension.diagram.settings.impl

import com.intellij.openapi.application.*
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
import kotlinx.coroutines.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyTreeDiagramSettingsConfigurable(
    val project: Project,
    val coroutineScope: CoroutineScope
) : BoundConfigurable(PlsDiagramBundle.message("stellaris.technologyTree.name")), SearchableConfigurable {
    override fun getId() = StellarisTechnologyTreeDiagramSettings.ID
    
    val settings = project.service<StellarisTechnologyTreeDiagramSettings>().state
    
    fun initSettings() {
        project.service<StellarisTechnologyTreeDiagramSettings>().initSettings()
    }
    
    override fun createPanel(): DialogPanel {
        initSettings()
        
        return panel {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
            }
            if(settings.type.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.type")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(JBGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.type.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.${key}"))
                                .bindSelected(settings.type.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(JBGaps(3, 0, 3, 0))
                        }
                    }
                }
            }
            if(settings.tier.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(JBGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.tier.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier.option", key))
                                .bindSelected(settings.tier.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(JBGaps(3, 0, 3, 0))
                        }
                    }
                }
            }
            if(settings.area.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.area")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(JBGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.area.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.area.option", key))
                                .bindSelected(settings.area.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(JBGaps(3, 0, 3, 0))
                            //add localized name as comment lazily
                            settings.areaNames.get(key)?.let { p ->
                                comment("")
                                    .customize(JBGaps(3, 16, 3, 0))
                                    .applyToComponent { coroutineScope.launch { text = readAction(p) } }
                            }
                        }
                    }
                }
            }
            if(settings.category.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.category")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(JBGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.category.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.category.option", key))
                                .bindSelected(settings.category.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(JBGaps(3, 0, 3, 0))
                            //add localized name as comment lazily
                            settings.categoryNames.get(key)?.let { p ->
                                comment("").customize(JBGaps(3, 16, 3, 0))
                                    .applyToComponent { coroutineScope.launch { text = readAction(p) } }
                            }
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

