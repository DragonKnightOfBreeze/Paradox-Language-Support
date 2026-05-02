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
    var showNameForDefinition: Boolean // region
        get() = state.showNameForDefinition
        set(value) = run { updateState { it.copy(showNameForDefinition = value) } } // endregion
    var showSubtypesForDefinition: Boolean // region
        get() = state.showSubtypesForDefinition
        set(value) = run { updateState { it.copy(showSubtypesForDefinition = value) } } // endregion
    var truncateSubtypesForDefinition: Boolean // region
        get() = state.truncateSubtypesForDefinition
        set(value) = run { updateState { it.copy(truncateSubtypesForDefinition = value) } } // endregion
    var showSubtypesForDefinitionReference: Boolean // region
        get() = state.showSubtypesForDefinitionReference
        set(value) = run { updateState { it.copy(showSubtypesForDefinitionReference = value) } } // endregion
    var truncateSubtypesForDefinitionReference: Boolean // region
        get() = state.truncateSubtypesForDefinitionReference
        set(value) = run { updateState { it.copy(truncateSubtypesForDefinitionReference = value) } } // endregion
    var showSubtypesForCsvDefinitionReference: Boolean // region
        get() = state.showSubtypesForCsvDefinitionReference
        set(value) = run { updateState { it.copy(showSubtypesForCsvDefinitionReference = value) } } // endregion
    var truncateSubtypesForCsvDefinitionReference: Boolean // region
        get() = state.truncateSubtypesForCsvDefinitionReference
        set(value) = run { updateState { it.copy(truncateSubtypesForCsvDefinitionReference = value) } } // endregion
    var showSubtypesForDefinitionInjection: Boolean // region
        get() = state.showSubtypesForDefinitionInjection
        set(value) = run { updateState { it.copy(showSubtypesForDefinitionInjection = value) } } // endregion
    var truncateSubtypesForDefinitionInjection: Boolean // region
        get() = state.truncateSubtypesForDefinitionInjection
        set(value) = run { updateState { it.copy(truncateSubtypesForDefinitionInjection = value) } } // endregion

    data class State(
        @JvmField val showNameForDefinition: Boolean = true,
        @JvmField val showSubtypesForDefinition: Boolean = true,
        @JvmField val truncateSubtypesForDefinition: Boolean = false,
        @JvmField val showSubtypesForDefinitionReference: Boolean = true,
        @JvmField val truncateSubtypesForDefinitionReference: Boolean = false,
        @JvmField val showSubtypesForCsvDefinitionReference: Boolean = true,
        @JvmField val truncateSubtypesForCsvDefinitionReference: Boolean = false,
        @JvmField val showSubtypesForDefinitionInjection: Boolean = true,
        @JvmField val truncateSubtypesForDefinitionInjection: Boolean = false,
    )

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ParadoxDeclarativeHintsSettings = project.service()
    }
}
