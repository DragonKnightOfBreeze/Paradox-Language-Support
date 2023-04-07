package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.ThreeStateCheckBox
import com.intellij.util.ui.ThreeStateCheckBox.State.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Stellaris)
@Service(Service.Level.APP)
@State(name = "ParadoxDiagramSettings.Stellaris.EventTree", storages = [Storage("paradox-language-support.xml")])
class StellarisEventTreeDiagramSettings : ParadoxEventTreeDiagramSettings<StellarisEventTreeDiagramSettings.State>(StellarisEventTreeDiagramSettings.State()) {
    companion object {
        const val ID = "settings.language.pls.diagram.Stellaris.EventTree"
    }
    
    override val id: String = ID
    
    class State() : ParadoxDiagramSettings.State() {
        override var scopeType by string()
        
        @get:Property(surroundWithTag = false)
        var type by linkedMap<String, Boolean>()
        
        var typeState by type.toThreeStateProperty()
        
        val typeSettings = TypeSettings()
        
        inner class TypeSettings {
            var hidden by type
            var triggered by type
            var major by type
            var diplomatic by type
            var other by type
        }
    }
}

