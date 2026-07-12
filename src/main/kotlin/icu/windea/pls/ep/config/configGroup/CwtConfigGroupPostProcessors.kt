package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.progress.checkCanceled
import icu.windea.pls.config.attributes.CwtExpandableConfigAttributesEvaluator
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase

class CwtBaseConfigGroupPostProcessor : CwtConfigGroupPostProcessor {
    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        evaluateAttributes(configGroup)
    }

    private suspend fun evaluateAttributes(configGroup: CwtConfigGroup) {
        checkCanceled()
        configGroup.unions.forEach { (k, v) ->
            configGroup.unionAttributes[k] = CwtExpandableConfigAttributesEvaluator().evaluate(k, v, configGroup)
        }
        if (configGroup is CwtConfigGroupDataHolderBase) configGroup.unions.trim()

        checkCanceled()
        configGroup.singleAliases.forEach { (k, v) ->
            configGroup.singleAliasAttributes[k] = CwtExpandableConfigAttributesEvaluator().evaluate(k, v, configGroup)
        }
        if (configGroup is CwtConfigGroupDataHolderBase) configGroup.singleAliasAttributes.trim()

        checkCanceled()
        configGroup.aliasGroups.forEach { (k, v) ->
            configGroup.aliasAttributes[k] = CwtExpandableConfigAttributesEvaluator().evaluate(k, v.values, configGroup)
        }
        if (configGroup is CwtConfigGroupDataHolderBase) configGroup.aliasAttributes.trim()

        checkCanceled()
        configGroup.types.values.forEach { it.attributes }

        checkCanceled()
        configGroup.declarations.values.forEach { it.attributes }
    }
}
