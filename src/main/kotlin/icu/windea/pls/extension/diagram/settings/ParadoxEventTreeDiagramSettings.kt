package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.extension.diagram.provider.*

abstract class ParadoxEventTreeDiagramSettings<T: BaseState>(initialState: T): ParadoxDiagramSettings<T>(initialState)

@Service(Service.Level.APP)
@State(name = "ParadoxDiagramSettings." + StellarisEventTreeDiagramProvider.ID, storages = [Storage("paradox-language-support.xml")])
class StellarisEventTreeDiagramSettings: ParadoxEventTreeDiagramSettings<StellarisEventTreeDiagramSettings.State>(StellarisEventTreeDiagramSettings.State()) {
    class State() :BaseState() {
        @get:Property(surroundWithTag = false)
        var type by property(TypeState())
    }
    
    class TypeState(): BaseState() {
        var hidden by property(true)
        var triggered by property(true)
        var major by property(true)
        var diplomatic by property(true)
        var other by property(true)
    }
}