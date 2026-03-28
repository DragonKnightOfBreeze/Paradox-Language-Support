package icu.windea.pls.config.configGroup

import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.PlsModificationTrackers
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

val CwtConfigGroup.mockVariableConfig: CwtValueConfig
    by registerKey(CwtConfigGroup.Keys) {
        CwtValueConfig.createMock(this, "value[variable]")
    }

val CwtConfigGroup.scriptValueModificationTracker: ModificationTracker
    by registerKey(CwtConfigGroup.Keys) {
        val definitionType = ParadoxDefinitionTypes.scriptValue
        PlsModificationTrackers.scriptFileFromDefinitionTypes(this, definitionType)
    }

val CwtConfigGroup.definitionParameterModificationTracker: ModificationTracker
    by registerKey(CwtConfigGroup.Keys) {
        val definitionTypes = definitionTypesModel.supportParameters
        PlsModificationTrackers.scriptFileFromDefinitionTypes(this, definitionTypes)
    }

val CwtConfigGroup.definitionScopeContextModificationTracker: ModificationTracker
    by registerKey(CwtConfigGroup.Keys) {
        val definitionTypes = definitionTypesModel.supportScopeContextInference
        PlsModificationTrackers.scriptFileFromDefinitionTypes(this, definitionTypes)
    }
