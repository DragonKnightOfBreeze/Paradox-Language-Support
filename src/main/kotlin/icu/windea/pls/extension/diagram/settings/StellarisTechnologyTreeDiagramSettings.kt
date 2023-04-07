package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.ThreeStateCheckBox
import com.intellij.util.ui.ThreeStateCheckBox.State.*
import com.intellij.util.xmlb.annotations.*
import com.intellij.util.xmlb.annotations.Transient
import icu.windea.pls.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Stellaris)
@Service(Service.Level.APP)
@State(name = "ParadoxDiagramSettings.Stellaris.TechnologyTree", storages = [Storage("paradox-language-support.xml")])
class StellarisTechnologyTreeDiagramSettings : ParadoxTechnologyTreeDiagramSettings<StellarisTechnologyTreeDiagramSettings.State>(StellarisTechnologyTreeDiagramSettings.State()) {
    companion object {
        const val ID = "settings.language.pls.diagram.Stellaris.TechnologyTree"
    }
    
    override val id: String = ID
    
    class State() : ParadoxDiagramSettings.State() {
        override var scopeType by string()
        
        @get:Property(surroundWithTag = false)
        var type by linkedMap<String, Boolean>()
        @get:Property(surroundWithTag = false)
        var tier by linkedMap<String, Boolean>()
        @get:Property(surroundWithTag = false)
        var area by linkedMap<String, Boolean>()
        @get:Property(surroundWithTag = false)
        var category by linkedMap<String, Boolean>()
        
        var typeState by type.toThreeStateProperty()
        var tierState by tier.toThreeStateProperty()
        var areaState by area.toThreeStateProperty()
        var categoryState by category.toThreeStateProperty()
        
        val typeSettings = TypeSettings()
        
        inner class TypeSettings {
            val start by type
            val rare by type
            val dangerous by type
            val insight by type
            val repeatable by type
            val other by type
        }
    }
}

