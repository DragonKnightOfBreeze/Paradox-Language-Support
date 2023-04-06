package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.extension.diagram.provider.*

abstract class ParadoxTechlonogyTreeDiagramSettings<T: BaseState>(initialState: T): ParadoxDiagramSettings<T>(initialState)

@Service(Service.Level.APP)
@State(name = "ParadoxDiagramSettings." + StellarisTechnologyTreeDiagramProvider.ID, storages = [Storage("paradox-language-support.xml")])
class StellarisTechlonogyTreeDiagramSettings: ParadoxTechlonogyTreeDiagramSettings<StellarisTechlonogyTreeDiagramSettings.State>(StellarisTechlonogyTreeDiagramSettings.State()) {
    class State() :BaseState() {
        @get:Property(surroundWithTag = false)
        var type by property(TypeState())
        @get:Property(surroundWithTag = false)
        var tier by linkedMap<String, Boolean>()
        @get:Property(surroundWithTag = false)
        var area by linkedMap<String, Boolean>()
        @get:Property(surroundWithTag = false)
        var category by linkedMap<String, Boolean>()
    }
    
    class TypeState(): BaseState() {
        var start by property(true)
        var rare by property(true)
        var dangerous by property(true)
        var insight by property(true)
        var repeatable by property(true)
        var other by property(true)
    }
}