package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*

abstract class ParadoxDiagramSettings<T: ParadoxDiagramSettings.State>(initialState: T) : SimplePersistentStateComponent<T>(initialState) {
    abstract val id: String
    
    abstract class State: BaseState() {
        abstract var scopeType: String?
        
        fun updateSettings() = incrementModificationCount()
    }
    
    open fun initSettings() {
        state.updateSettings()
    }
}