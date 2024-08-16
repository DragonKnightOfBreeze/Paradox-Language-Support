package icu.windea.pls.config.configGroup

import com.intellij.openapi.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.scope.*
import icu.windea.pls.lang.*

@Tags(Tag.Computed)
val CwtConfigGroup.definitionParameterModificationTracker: ModificationTracker
    by createKeyDelegate(CwtConfigGroup.Keys) {
        val definitionTypes = definitionTypesSupportParameters
        val configs = definitionTypes.mapNotNull { types[it] }
        val pattern = ParadoxModificationTrackers.getPatternFromTypeConfigs(configs)
        ParadoxModificationTrackers.ScriptFileTracker(pattern)
    }

@Tags(Tag.Computed)
val CwtConfigGroup.definitionScopeContextModificationTracker: ModificationTracker
    by createKeyDelegate(CwtConfigGroup.Keys) {
        val definitionTypes = ParadoxBaseDefinitionInferredScopeContextProvider.Constants.DEFINITION_TYPES
        val configs = definitionTypes.mapNotNull { types[it] }
        val pattern = ParadoxModificationTrackers.getPatternFromTypeConfigs(configs)
        ParadoxModificationTrackers.ScriptFileTracker(pattern)
    }
