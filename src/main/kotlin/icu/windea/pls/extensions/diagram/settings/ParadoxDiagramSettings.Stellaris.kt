package icu.windea.pls.extensions.diagram.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.XMap
import icu.windea.pls.ChronicleDocBundle
import icu.windea.pls.base.annotations.WithGameType
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.extensions.diagram.ChronicleDiagramBundle
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.lang.util.ParadoxTechnologyManager
import icu.windea.pls.lang.util.presentation.ParadoxPresentationUtil
import icu.windea.pls.model.ParadoxGameType
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
    }

    override val groupName: String = ChronicleDiagramBundle.message("eventTree.name.stellaris")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val types = ParadoxEventManager.getAllTypes(gameType)
        settings.type.retainSettings(types)
        val attributes = ParadoxEventManager.getAllAttributes(gameType)
        settings.attribute.retainSettings(attributes)
        settings.updateSettings()

        row {
            label(ChronicleDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.type, ChronicleDiagramBundle.message("eventTree.settings.type"), { key ->
            ChronicleDocBundle.eventType(key, gameType)
        })
        checkBoxGroup(settings.attribute, ChronicleDiagramBundle.message("eventTree.settings.attribute"), { key ->
            ChronicleDocBundle.eventAttribute(key, gameType)
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
    }

    override val groupName: String = ChronicleDiagramBundle.message("techTree.name.stellaris")

    override val groupBuilder: Panel.() -> Unit = {
        val settings = state
        val tiers = runSmartReadAction { ParadoxTechnologyManager.Stellaris.getAllTiers(project, null) }
        settings.tier.retainSettings(tiers) { it.name }
        val areas = ParadoxTechnologyManager.Stellaris.getAllResearchAreas()
        settings.area.retainSettings(areas)
        val categories = runSmartReadAction { ParadoxTechnologyManager.Stellaris.getAllCategories(project, null) }
        settings.category.retainSettings(categories) { it.name }
        val attributes = ParadoxTechnologyManager.Stellaris.getAllAttributes(gameType)
        settings.area.retainSettings(attributes)
        settings.updateSettings()

        val areaNameProviders = mutableMapOf<String, () -> String?>()
        areas.forEach { areaNameProviders.put(it) { ParadoxPresentationUtil.getText(it.uppercase(), project) } }
        val categoryNameProviders = mutableMapOf<String, () -> String?>()
        categories.forEach { categoryNameProviders.put(it.name) { ParadoxPresentationUtil.getNameTextOrKey(it) } }

        row {
            label(ChronicleDiagramBundle.message("settings.diagram.tooltip.selectNodes"))
        }
        checkBoxGroup(settings.tier, ChronicleDiagramBundle.message("techTree.settings.tier"), { key ->
            ChronicleDocBundle.technologyTier(key, gameType)
        })
        checkBoxGroup(settings.area, ChronicleDiagramBundle.message("techTree.settings.area"), { key ->
            ChronicleDocBundle.message("default.technology.area", key)
        }) { key ->
            areaNameProviders[key]?.invoke()
        }
        checkBoxGroup(settings.category, ChronicleDiagramBundle.message("techTree.settings.category"), { key ->
            ChronicleDocBundle.message("default.technology.category", key)
        }) { key ->
            categoryNameProviders[key]?.invoke()
        }
        checkBoxGroup(settings.attribute, ChronicleDiagramBundle.message("techTree.settings.attribute"), { key ->
            ChronicleDocBundle.technologyAttribute(key, gameType)
        })
    }
}
