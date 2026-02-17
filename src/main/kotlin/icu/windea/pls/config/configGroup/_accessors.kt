package icu.windea.pls.config.configGroup

import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

val CwtConfigGroup.mockVariableConfig: CwtValueConfig
    by registerKey(CwtConfigGroup.Keys) {
        CwtValueConfig.createMock(this, "value[variable]")
    }

val CwtConfigGroup.scriptValueModificationTracker: ModificationTracker
    by registerKey(CwtConfigGroup.Keys) {
        val definitionType = ParadoxDefinitionTypes.scriptValue
        val config = types[definitionType]
        if (config == null) return@registerKey ModificationTracker.NEVER_CHANGED
        val patterns = config.filePathPatterns.joinToString(";")
        ParadoxModificationTrackers.ScriptFile(patterns)
    }

val CwtConfigGroup.definitionParameterModificationTracker: ModificationTracker
    by registerKey(CwtConfigGroup.Keys) {
        val definitionTypes = definitionTypesModel.supportParameters
        val configs = definitionTypes.mapNotNull { types[it] }
        if (configs.isEmpty()) return@registerKey ModificationTracker.NEVER_CHANGED
        val patterns = configs.flatMapTo(sortedSetOf()) { it.filePathPatterns }.joinToString(";")
        ParadoxModificationTrackers.ScriptFile(patterns)
    }

val CwtConfigGroup.definitionScopeContextModificationTracker: ModificationTracker
    by registerKey(CwtConfigGroup.Keys) {
        val definitionTypes = definitionTypesModel.supportScopeContextInference
        val configs = definitionTypes.mapNotNull { types[it] }
        if (configs.isEmpty()) return@registerKey ModificationTracker.NEVER_CHANGED
        val patterns = configs.flatMapTo(sortedSetOf()) { it.filePathPatterns }.joinToString(";")
        ParadoxModificationTrackers.ScriptFile(patterns)
    }
