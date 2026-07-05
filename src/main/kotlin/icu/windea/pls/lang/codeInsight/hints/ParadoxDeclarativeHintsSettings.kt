package icu.windea.pls.lang.codeInsight.hints

import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import icu.windea.pls.model.constants.ChronicleConstants

@Service(Service.Level.PROJECT)
@State(name = "ParadoxDeclarativeHintsSettings", storages = [Storage(ChronicleConstants.pluginSettingsFileName)])
class ParadoxDeclarativeHintsSettings : SerializablePersistentStateComponent<ParadoxDeclarativeHintsSettings.State>(State()) {
    var showDefinitionName: Boolean // region
        get() = state.showDefinitionName
        set(value) = run { updateState { it.copy(showDefinitionName = value) } } // endregion
    var showDefinitionSubtypes: Boolean // region
        get() = state.showDefinitionSubtypes
        set(value) = run { updateState { it.copy(showDefinitionSubtypes = value) } } // endregion
    var truncateDefinitionSubtypes: Int // region
        get() = state.truncateDefinitionSubtypes
        set(value) = run { updateState { it.copy(truncateDefinitionSubtypes = value) } } // endregion
    var showDefinitionSubtypesForReferences: Boolean // region
        get() = state.showDefinitionSubtypesForReferences
        set(value) = run { updateState { it.copy(showDefinitionSubtypesForReferences = value) } } // endregion
    var truncateDefinitionSubtypesForReferences: Int // region
        get() = state.truncateDefinitionSubtypesForReferences
        set(value) = run { updateState { it.copy(truncateDefinitionSubtypesForReferences = value) } } // endregion
    var showDefinitionSubtypesForInjections: Boolean // region
        get() = state.showDefinitionSubtypesForInjections
        set(value) = run { updateState { it.copy(showDefinitionSubtypesForInjections = value) } } // endregion
    var truncateDefinitionSubtypesForInjections: Int // region
        get() = state.truncateDefinitionSubtypesForInjections
        set(value) = run { updateState { it.copy(truncateDefinitionSubtypesForInjections = value) } } // endregion
    var showArrayValueForDefines: Boolean // region
        get() = state.showArrayValueForDefine
        set(value) = run { updateState { it.copy(showArrayValueForDefine = value) } } // endregion
    var truncateArrayValueForDefines: Int // region
        get() = state.truncateArrayValueForDefine
        set(value) = run { updateState { it.copy(truncateArrayValueForDefine = value) } } // endregion

    data class State(
        @JvmField val showDefinitionName: Boolean = true,
        @JvmField val showDefinitionSubtypes: Boolean = true,
        @JvmField val truncateDefinitionSubtypes: Int = -1,
        @JvmField val showDefinitionSubtypesForReferences: Boolean = true,
        @JvmField val truncateDefinitionSubtypesForReferences: Int = -1,
        @JvmField val showDefinitionSubtypesForInjections: Boolean = true,
        @JvmField val truncateDefinitionSubtypesForInjections: Int = -1,
        @JvmField val showArrayValueForDefine: Boolean = true,
        @JvmField val truncateArrayValueForDefine: Int = -1,
    )

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ParadoxDeclarativeHintsSettings = project.service()
    }
}
