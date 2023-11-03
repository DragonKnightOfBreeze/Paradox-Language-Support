package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.ui.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.*
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
    override val configurableClass: Class<out Configurable> = StellarisEventTreeDiagramSettingsConfigurable::class.java
    
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
    
    override fun initSettings() {
        val eventTypes = ParadoxEventHandler.getTypes(project, ParadoxGameType.Stellaris)
        eventTypes.forEach { state.eventType.putIfAbsent(it, true) }
    }
}

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
                        .customize(UnscaledGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.type.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.eventTree.settings.type.${key}"))
                                .bindSelected(settings.type.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(UnscaledGaps(3, 0, 3, 0))
                        }
                    }
                }
            }
            if(settings.eventType.isNotEmpty()) {
                lateinit var cb : Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.eventTree.settings.eventType")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(UnscaledGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.eventType.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.eventTree.settings.eventType.option", key))
                                .bindSelected(settings.eventType.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(UnscaledGaps(3, 0, 3, 0))
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
    override val configurableClass: Class<out Configurable> = StellarisTechnologyTreeDiagramSettingsConfigurable::class.java
    
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
        
        val areaNames = mutableMapOf<String, () -> String?>()
        val categoryNames = mutableMapOf<String, () -> String?>()
        
        fun a(){}
        
        inner class TypeSettings {
            val start by type withDefault true
            val rare by type withDefault true
            val dangerous by type withDefault true
            val insight by type withDefault true
            val repeatable by type withDefault true
        }
    }
    
    override fun initSettings() {
        //it.name is ok here
        val tiers = ParadoxTechnologyHandler.Stellaris.getTechnologyTiers(project, null)
        tiers.forEach { state.tier.putIfAbsent(it.name, true) }
        val areas = ParadoxTechnologyHandler.Stellaris.getResearchAreas()
        areas.forEach { state.area.putIfAbsent(it, true) }
        val categories = ParadoxTechnologyHandler.Stellaris.getTechnologyCategories(project, null)
        categories.forEach { state.category.putIfAbsent(it.name, true) }
        areas.forEach { state.areaNames.put(it) { ParadoxPresentationHandler.getText(it.uppercase(), project) } }
        categories.forEach { state.categoryNames.put(it.name) { ParadoxPresentationHandler.getNameText(it) } }
        super.initSettings()
    }
}

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
                        .customize(UnscaledGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.type.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.${key}"))
                                .bindSelected(settings.type.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(UnscaledGaps(3, 0, 3, 0))
                        }
                    }
                }
            }
            if(settings.tier.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(UnscaledGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.tier.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier.option", key))
                                .bindSelected(settings.tier.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(UnscaledGaps(3, 0, 3, 0))
                        }
                    }
                }
            }
            if(settings.area.isNotEmpty()) {
                lateinit var cb: Cell<ThreeStateCheckBox>
                row {
                    cell(ThreeStateCheckBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.area")))
                        .applyToComponent { isThirdStateEnabled = false }
                        .customize(UnscaledGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.area.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.area.option", key))
                                .bindSelected(settings.area.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(UnscaledGaps(3, 0, 3, 0))
                            //add localized name as comment lazily
                            settings.areaNames.get(key)?.let { p ->
                                comment("")
                                    .customize(UnscaledGaps(3, 16, 3, 0))
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
                        .customize(UnscaledGaps(3, 0, 3, 0))
                        .also { cb = it }
                }
                indent {
                    settings.category.keys.forEach { key ->
                        row {
                            checkBox(PlsDiagramBundle.message("stellaris.technologyTree.settings.category.option", key))
                                .bindSelected(settings.category.toMutableProperty(key, true))
                                .threeStateCheckBox(cb)
                                .customize(UnscaledGaps(3, 0, 3, 0))
                            //add localized name as comment lazily
                            settings.categoryNames.get(key)?.let { p ->
                                comment("").customize(UnscaledGaps(3, 16, 3, 0))
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