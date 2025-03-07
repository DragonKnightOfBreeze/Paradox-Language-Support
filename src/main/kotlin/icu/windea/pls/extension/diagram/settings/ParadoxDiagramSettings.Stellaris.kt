package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

@WithGameType(ParadoxGameType.Stellaris)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Stellaris.EventTree", storages = [Storage("paradox-language-support.xml")])
class StellarisEventTreeDiagramSettings(
    project: Project
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

    override val groupName: String = PlsDiagramBundle.message("stellaris.eventTree.name")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val eventTypes = ParadoxEventManager.getTypes(project, ParadoxGameType.Stellaris)
        eventTypes.forEach { settings.eventType.putIfAbsent(it, true) }
        settings.updateSettings()

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, PlsDiagramBundle.message("stellaris.eventTree.settings.type"), { key ->
            when (key) {
                State.TypeSettings::hidden.name -> PlsDiagramBundle.message("stellaris.eventTree.settings.type.hidden")
                State.TypeSettings::triggered.name -> PlsDiagramBundle.message("stellaris.eventTree.settings.type.triggered")
                State.TypeSettings::major.name -> PlsDiagramBundle.message("stellaris.eventTree.settings.type.major")
                State.TypeSettings::diplomatic.name -> PlsDiagramBundle.message("stellaris.eventTree.settings.type.diplomatic")
                else -> null
            }
        })
        checkBoxGroup(settings.eventType, PlsDiagramBundle.message("stellaris.eventTree.settings.eventType"), { key ->
            PlsDiagramBundle.message("stellaris.eventTree.settings.eventType.option", key)
        })
    }
}

@WithGameType(ParadoxGameType.Stellaris)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Stellaris.TechnologyTree", storages = [Storage("paradox-language-support.xml")])
class StellarisTechnologyTreeDiagramSettings(
    project: Project
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

    override val groupName: String = PlsDiagramBundle.message("stellaris.technologyTree.name")

    override val groupBuilder: Panel.() -> Unit = {
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

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, PlsDiagramBundle.message("stellaris.technologyTree.settings.type"), { key ->
            when (key) {
                State.TypeSettings::start.name -> PlsDiagramBundle.message("stellaris.technologyTree.settings.type.start")
                State.TypeSettings::rare.name -> PlsDiagramBundle.message("stellaris.technologyTree.settings.type.rare")
                State.TypeSettings::dangerous.name -> PlsDiagramBundle.message("stellaris.technologyTree.settings.type.dangerous")
                State.TypeSettings::insight.name -> PlsDiagramBundle.message("stellaris.technologyTree.settings.type.insight")
                State.TypeSettings::repeatable.name -> PlsDiagramBundle.message("stellaris.technologyTree.settings.type.repeatable")
                else -> null
            }
        })
        checkBoxGroup(settings.tier, PlsDiagramBundle.message("stellaris.technologyTree.settings.tier"), { key ->
            PlsDiagramBundle.message("stellaris.technologyTree.settings.tier.option", key)
        })
        checkBoxGroup(settings.area, PlsDiagramBundle.message("stellaris.technologyTree.settings.area"), { key ->
            PlsDiagramBundle.message("stellaris.technologyTree.settings.area.option", key)
        }) { key ->
            areaNameProviders[key]?.invoke()
        }
        checkBoxGroup(settings.category, PlsDiagramBundle.message("stellaris.technologyTree.settings.category"), { key ->
            PlsDiagramBundle.message("stellaris.technologyTree.settings.category.option", key)
        }) { key ->
            categoryNameProviders[key]?.invoke()
        }
    }
}
