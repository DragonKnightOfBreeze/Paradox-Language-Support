package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.components.*
import com.intellij.ui.dsl.builder.*
import kotlinx.coroutines.*

abstract class ParadoxDiagramSettings<T: ParadoxDiagramSettings.State>(initialState: T) : SimplePersistentStateComponent<T>(initialState) {
    abstract val id: String
    
    abstract class State: BaseState() {
        abstract var scopeType: String?
        
        fun updateSettings() = incrementModificationCount()
    }
    
    fun buildConfigurablePanel(coroutineScope: CoroutineScope, panel: Panel) {
        panel.buildConfigurablePanel(coroutineScope)
    }
    
    open fun Panel.buildConfigurablePanel(coroutineScope: CoroutineScope) {
        
    }
}