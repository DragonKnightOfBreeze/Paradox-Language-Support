package icu.windea.pls.config.configGroup

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.scope.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*

@Tags(Tag.Computed)
val CwtConfigGroup.definitionParameterModificationTracker: ModificationTracker
    by createKey(CwtConfigGroup.Keys) {
        val definitionTypes = definitionTypesSupportParameters
        val configs = definitionTypes.mapNotNull { types[it] }
        val patterns = configs.flatMapTo(sortedSetOf()) { CwtConfigManager.getFilePathPatterns(it) }
        ParadoxModificationTrackers.ScriptFileTracker(patterns.joinToString(";"))
    }

@Tags(Tag.Computed)
val CwtConfigGroup.definitionScopeContextModificationTracker: ModificationTracker
    by createKey(CwtConfigGroup.Keys) {
        val definitionTypes = ParadoxBaseDefinitionInferredScopeContextProvider.Constants.DEFINITION_TYPES
        val configs = definitionTypes.mapNotNull { types[it] }
        val patterns = configs.flatMapTo(sortedSetOf()) { CwtConfigManager.getFilePathPatterns(it) }
        ParadoxModificationTrackers.ScriptFileTracker(patterns.joinToString(";"))
    }
