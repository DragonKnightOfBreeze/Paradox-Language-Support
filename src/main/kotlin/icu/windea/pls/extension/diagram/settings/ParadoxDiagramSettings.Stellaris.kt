package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.ui.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*

@WithGameType(ParadoxGameType.Stellaris)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Stellaris.EventTree", storages = [Storage("paradox-language-support.xml")])
class StellarisEventTreeDiagramSettings(
    val project: Project
) : ParadoxEventTreeDiagramSettings<StellarisEventTreeDiagramSettings.State>(State()) {
    companion object {
        const val ID = "pls.diagram.Stellaris.EventTree"
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
            val triggered by type withDefault true
            val major by type withDefault true
            val diplomatic by type withDefault true
        }
    }

    override fun Panel.buildConfigurablePanel(coroutineScope: CoroutineScope) {
        val settings = state
        val eventTypes = ParadoxEventManager.getTypes(project, ParadoxGameType.Stellaris)
        eventTypes.forEach { settings.eventType.putIfAbsent(it, true) }
        settings.updateSettings()

        collapsibleGroup(PlsDiagramBundle.message("stellaris.eventTree.name")) {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
            }
            if (settings.type.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.eventTree.settings.type")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .smaller()
                        .also { cb = it }
                }
                indent {
                    settings.type.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.eventTree.settings.type.${key}"))
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
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.eventTree.settings.eventType")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .smaller()
                        .also { cb = it }
                }
                indent {
                    settings.eventType.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.eventTree.settings.eventType.option", key))
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

@WithGameType(ParadoxGameType.Stellaris)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Stellaris.TechnologyTree", storages = [Storage("paradox-language-support.xml")])
class StellarisTechnologyTreeDiagramSettings(
    val project: Project
) : ParadoxTechnologyTreeDiagramSettings<StellarisTechnologyTreeDiagramSettings.State>(State()) {
    companion object {
        const val ID = "pls.diagram.Stellaris.TechnologyTree"
    }

    override val id: String = ID

    class State : ParadoxDiagramSettings.State() {
        override var scopeType by string()

        @get:XMap
        var type by linkedMap<String, Boolean>()
        @get:XMap
        var tier by linkedMap<String, Boolean>()
        @get:XMap
        var area by linkedMap<String, Boolean>()
        @get:XMap
        var category by linkedMap<String, Boolean>()

        val typeSettings = TypeSettings()

        inner class TypeSettings {
            val start by type withDefault true
            val rare by type withDefault true
            val dangerous by type withDefault true
            val insight by type withDefault true
            val repeatable by type withDefault true
        }
    }

    override fun Panel.buildConfigurablePanel(coroutineScope: CoroutineScope) {
        val settings = state
        val tiers = ParadoxTechnologyManager.Stellaris.getTechnologyTiers(project, null)
        tiers.forEach { settings.tier.putIfAbsent(it.name, true) }
        val areas = ParadoxTechnologyManager.Stellaris.getResearchAreas()
        areas.forEach { settings.area.putIfAbsent(it, true) }
        val categories = ParadoxTechnologyManager.Stellaris.getTechnologyCategories(project, null)
        categories.forEach { settings.category.putIfAbsent(it.name, true) }
        settings.updateSettings()

        val areaNameProviders = mutableMapOf<String, () -> String?>()
        areas.forEach { areaNameProviders.put(it) { ParadoxPresentationManager.getText(it.uppercase(), project) } }
        val categoryNameProviders = mutableMapOf<String, () -> String?>()
        categories.forEach { categoryNameProviders.put(it.name) { ParadoxPresentationManager.getNameText(it) } }

        collapsibleGroup(PlsDiagramBundle.message("stellaris.technologyTree.name")) {
            row {
                label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
            }
            if (settings.type.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.type")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .smaller()
                        .also { cb = it }
                }
                indent {
                    settings.type.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.${key}"))
                                .bindSelected(settings.type.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .smaller()
                        }
                    }
                }
            }
            if (settings.tier.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .smaller()
                        .also { cb = it }
                }
                indent {
                    settings.tier.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier.option", key))
                                .bindSelected(settings.tier.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .smaller()
                        }
                    }
                }
            }
            if (settings.area.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.area")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .smaller()
                        .also { cb = it }
                }
                indent {
                    settings.area.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.area.option", key))
                                .bindSelected(settings.area.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .smaller()

                            //add related localized name as comment lazily
                            comment("").customize(UnscaledGaps(3, 16, 3, 0)).applyToComponent t@{
                                val p = areaNameProviders.get(key) ?: return@t
                                coroutineScope.launch { text = readAction(p) }
                            }
                        }
                    }
                }
            }
            if (settings.category.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.category")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .smaller()
                        .also { cb = it }
                }
                indent {
                    settings.category.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.category.option", key))
                                .bindSelected(settings.category.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .smaller()

                            //add related localized name as comment lazily
                            comment("").customize(UnscaledGaps(3, 16, 3, 0)).applyToComponent t@{
                                val p = categoryNameProviders.get(key) ?: return@t
                                coroutineScope.launch { text = readAction(p) }
                            }
                        }
                    }
                }
            }
        }
    }
}
