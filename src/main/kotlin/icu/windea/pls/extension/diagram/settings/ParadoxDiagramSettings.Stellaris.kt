package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.PlsConstants

@WithGameType(ParadoxGameType.Stellaris)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Stellaris.EventTree", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class StellarisEventTreeDiagramSettings(
    project: Project
) : ParadoxEventTreeDiagramSettings<StellarisEventTreeDiagramSettings.State>(project, State(), ParadoxGameType.Stellaris) {
    companion object {
        const val ID = "pls.diagram.Stellaris.EventTree"
    }

    override val id: String = ID

    class State : ParadoxDiagramSettings.State() {
        override var scopeType by string()

        @get:XMap
        var type by linkedMap<String, Boolean>()
        @get:XMap
        var attribute by linkedMap<String, Boolean>()

        val attributeSettings = AttributeSettings()

        inner class AttributeSettings {
            val hidden by attribute withDefault true
            val triggered by attribute withDefault true
            val major by attribute withDefault true
            val diplomatic by attribute withDefault true
        }
    }

    override val groupName: String = PlsDiagramBundle.message("eventTree.name.stellaris")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val types = runReadAction { ParadoxEventManager.getAllTypes(ParadoxGameType.Stellaris) }
        settings.type.retainSettings(types)
        settings.updateSettings()

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, PlsDiagramBundle.message("eventTree.settings.type"), { key ->
            PlsDocBundle.eventType(key, gameType)
        })
        checkBoxGroup(settings.attribute, PlsDiagramBundle.message("eventTree.settings.attribute"), { key ->
            PlsDocBundle.eventAttribute(key, gameType)
        })
    }
}

@WithGameType(ParadoxGameType.Stellaris)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Stellaris.TechTree", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class StellarisTechTreeDiagramSettings(
    project: Project
) : ParadoxTechTreeDiagramSettings<StellarisTechTreeDiagramSettings.State>(project, State(), ParadoxGameType.Stellaris) {
    companion object {
        const val ID = "pls.diagram.Stellaris.TechTree"
    }

    override val id: String = ID

    class State : ParadoxDiagramSettings.State() {
        override var scopeType by string()

        @get:XMap
        var tier by linkedMap<String, Boolean>()
        @get:XMap
        var area by linkedMap<String, Boolean>()
        @get:XMap
        var category by linkedMap<String, Boolean>()
        @get:XMap
        var attribute by linkedMap<String, Boolean>()

        val attributeSettings = AttributeSettings()

        inner class AttributeSettings {
            val start by attribute withDefault true
            val rare by attribute withDefault true
            val dangerous by attribute withDefault true
            val insight by attribute withDefault true
            val repeatable by attribute withDefault true
        }
    }

    override val groupName: String = PlsDiagramBundle.message("techTree.name.stellaris")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val tiers = runReadAction { ParadoxTechnologyManager.Stellaris.getAllTiers(project, null) }
        settings.tier.retainSettings(tiers) { it.name }
        val areas = runReadAction { ParadoxTechnologyManager.Stellaris.getAllResearchAreas() }
        settings.area.retainSettings(areas)
        val categories = runReadAction { ParadoxTechnologyManager.Stellaris.getAllCategories(project, null) }
        settings.category.retainSettings(categories) { it.name }
        settings.updateSettings()

        val areaNameProviders = mutableMapOf<String, () -> String?>()
        areas.forEach { areaNameProviders.put(it) { ParadoxPresentationManager.getText(it.uppercase(), project) } }
        val categoryNameProviders = mutableMapOf<String, () -> String?>()
        categories.forEach { categoryNameProviders.put(it.name) { ParadoxPresentationManager.getNameText(it) } }

        row {
            label(PlsDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.tier, PlsDiagramBundle.message("techTree.settings.tier"), { key ->
            PlsDocBundle.technologyTier(key, gameType)
        })
        checkBoxGroup(settings.area, PlsDiagramBundle.message("techTree.settings.area"), { key ->
            PlsDocBundle.message("default.technology.area", key)
        }) { key ->
            areaNameProviders[key]?.invoke()
        }
        checkBoxGroup(settings.category, PlsDiagramBundle.message("techTree.settings.category"), { key ->
            PlsDocBundle.message("default.technology.category", key)
        }) { key ->
            categoryNameProviders[key]?.invoke()
        }
        checkBoxGroup(settings.attribute, PlsDiagramBundle.message("techTree.settings.attribute"), { key ->
            PlsDocBundle.technologyAttribute(key, gameType)
        })
    }
}
