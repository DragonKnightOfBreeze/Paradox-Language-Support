package icu.windea.pls.config.data

import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.ep.resolve.scope.ParadoxBaseDefinitionInferredScopeContextProvider
import icu.windea.pls.lang.ParadoxModificationTrackers

val CwtConfigGroup.mockVariableConfig: CwtValueConfig
    by registerKey(CwtConfigGroup.Keys) {
        CwtValueConfig.create(emptyPointer(), this, "value[variable]")
    }

val CwtConfigGroup.definitionParameterModificationTracker: ModificationTracker
    by registerKey(CwtConfigGroup.Keys) {
        val definitionTypes = definitionTypesModel.supportParameters
        val configs = definitionTypes.mapNotNull { types[it] }
        val patterns = configs.flatMapTo(sortedSetOf()) { it.filePathPatterns }
        ParadoxModificationTrackers.ScriptFile(patterns.joinToString(";"))
    }

val CwtConfigGroup.definitionScopeContextModificationTracker: ModificationTracker
    by registerKey(CwtConfigGroup.Keys) {
        val definitionTypes = ParadoxBaseDefinitionInferredScopeContextProvider.Constants.DEFINITION_TYPES
        val configs = definitionTypes.mapNotNull { types[it] }
        val patterns = configs.flatMapTo(sortedSetOf()) { it.filePathPatterns }
        ParadoxModificationTrackers.ScriptFile(patterns.joinToString(";"))
    }
