package icu.windea.pls.lang.codeInsight.hints

import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import icu.windea.pls.model.constants.PlsConstants

@Service(Service.Level.PROJECT)
@State(name = "ParadoxDeclarativeHintsSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class ParadoxDeclarativeHintsSettings : SerializablePersistentStateComponent<ParadoxDeclarativeHintsSettings.State>(State()) {
    var showNameForDefinition: Boolean
        get() = state.showNameForDefinition
        set(value) = run { updateState { it.copy(showNameForDefinition = value) } }
    var showSubtypesForDefinition: Boolean
        get() = state.showSubtypesForDefinition
        set(value) = run { updateState { it.copy(showSubtypesForDefinition = value) } }
    var truncateSubtypesForDefinition: Boolean
        get() = state.truncateSubtypesForDefinition
        set(value) = run { updateState { it.copy(truncateSubtypesForDefinition = value) } }
    var showSubtypesForDefinitionReference: Boolean
        get() = state.showSubtypesForDefinitionReference
        set(value) = run { updateState { it.copy(showSubtypesForDefinitionReference = value) } }
    var truncateSubtypesForDefinitionReference: Boolean
        get() = state.truncateSubtypesForDefinitionReference
        set(value) = run { updateState { it.copy(truncateSubtypesForDefinitionReference = value) } }
    var showSubtypesForCsvDefinitionReference: Boolean
        get() = state.showSubtypesForCsvDefinitionReference
        set(value) = run { updateState { it.copy(showSubtypesForCsvDefinitionReference = value) } }
    var truncateSubtypesForCsvDefinitionReference: Boolean
        get() = state.truncateSubtypesForCsvDefinitionReference
        set(value) = run { updateState { it.copy(truncateSubtypesForCsvDefinitionReference = value) } }

    data class State(
        @JvmField val showNameForDefinition: Boolean = true,
        @JvmField val showSubtypesForDefinition: Boolean = true,
        @JvmField val truncateSubtypesForDefinition: Boolean = false,
        @JvmField val showSubtypesForDefinitionReference: Boolean = true,
        @JvmField val truncateSubtypesForDefinitionReference: Boolean = false,
        @JvmField val showSubtypesForCsvDefinitionReference: Boolean = true,
        @JvmField val truncateSubtypesForCsvDefinitionReference: Boolean = false,
    )

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ParadoxDeclarativeHintsSettings = project.service()
    }
}
