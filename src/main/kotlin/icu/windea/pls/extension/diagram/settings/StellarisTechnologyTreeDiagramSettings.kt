package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.ThreeStateCheckBox.State.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.tool.localisation.*
import javax.swing.*

@WithGameType(ParadoxGameType.Stellaris)
@Service(Service.Level.PROJECT)
@State(name = "ParadoxDiagramSettings.Stellaris.TechnologyTree", storages = [Storage("paradox-language-support.xml")])
class StellarisTechnologyTreeDiagramSettings(
    val project: Project
) : ParadoxTechnologyTreeDiagramSettings<StellarisTechnologyTreeDiagramSettings.State>(State()) {
    companion object {
        const val ID = "settings.language.pls.diagram.Stellaris.TechnologyTree"
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
        
        val areaNames = mutableMapOf<String, String?>()
        val categoryNames = mutableMapOf<String, String?>()
        
        inner class TypeSettings {
            val start = type.getOrPut("start") { true }
            val rare = type.getOrPut("rare") { true }
            val dangerous = type.getOrPut("dangerous") { true }
            val insight = type.getOrPut("insight") { true }
            val repeatable = type.getOrPut("repeatable") { true }
            val other = type.getOrPut("other") { true }
        }
    }
    
    override fun initSettings() {
        //it.name is ok here
        val tiers = StellarisTechnologyHandler.getTechnologyTiers(project, null)
        tiers.forEach {
            state.tier.putIfAbsent(it.name, true)
        }
        val areas = StellarisTechnologyHandler.getResearchAreas()
        areas.forEach {
            state.area.putIfAbsent(it, true)
            state.areaNames.put(it, ParadoxPresentationHandler.getText(it.uppercase(), project))
        }
        val categories = StellarisTechnologyHandler.getTechnologyCategories(project, null)
        categories.forEach {
            state.category.putIfAbsent(it.name, true)
            state.categoryNames.put(it.name, ParadoxPresentationHandler.getNameText(it))
        }
        super.initSettings()
    }
}
