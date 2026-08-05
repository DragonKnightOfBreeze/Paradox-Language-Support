package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.progress.checkCanceled
import icu.windea.pls.config.attributes.CwtExpandableConfigAttributesEvaluator
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupDataModelBase

class CwtBaseConfigGroupPostProcessor : CwtConfigGroupPostProcessor {
    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        evaluateAttributes(configGroup)
    }

    private suspend fun evaluateAttributes(configGroup: CwtConfigGroup) {
        val dataModel = configGroup.dataModel
        if (dataModel !is CwtConfigGroupDataModelBase) return

        checkCanceled()
        dataModel.unions.forEach { (k, v) ->
            dataModel.unionAttributes[k] = CwtExpandableConfigAttributesEvaluator().evaluate(k, v, configGroup)
        }
        dataModel.unions.trim()

        checkCanceled()
        dataModel.singleAliases.forEach { (k, v) ->
            dataModel.singleAliasAttributes[k] = CwtExpandableConfigAttributesEvaluator().evaluate(k, v, configGroup)
        }
        dataModel.singleAliasAttributes.trim()

        checkCanceled()
        dataModel.aliasGroups.forEach { (k, v) ->
            dataModel.aliasAttributes[k] = CwtExpandableConfigAttributesEvaluator().evaluate(k, v.values, configGroup)
        }
        dataModel.aliasAttributes.trim()

        checkCanceled()
        dataModel.types.values.forEach { it.attributes }

        checkCanceled()
        dataModel.rows.values.forEach { it.attributes }

        checkCanceled()
        dataModel.declarations.values.forEach { it.attributes }
    }
}
