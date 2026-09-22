package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.progress.checkCanceled
import icu.windea.pls.config.attributes.CwtExpandableConfigAttributesEvaluator
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.model.CwtConfigGroupDataModelBase

class CwtAttributesConfigGroupPostProcessor : CwtConfigGroupPostProcessor {
    // NOTE 3.0.3 attributes should be pre-evaluated by `CwtConfigGroupPostProcessor`, instead of `CwtConfigGroupProcessor`

    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        val dataModel = configGroup.dataModel
        if (dataModel !is CwtConfigGroupDataModelBase) return

        checkCanceled()
        dataModel.unions.forEach { (k, v) ->
            dataModel.unionAttributes[k] = CwtExpandableConfigAttributesEvaluator().evaluate(k, v, configGroup)
        }

        checkCanceled()
        dataModel.singleAliases.forEach { (k, v) ->
            dataModel.singleAliasAttributes[k] = CwtExpandableConfigAttributesEvaluator().evaluate(k, v, configGroup)
        }

        checkCanceled()
        dataModel.aliasGroups.forEach { (k, v) ->
            dataModel.aliasAttributes[k] = CwtExpandableConfigAttributesEvaluator().evaluate(k, v.values, configGroup)
        }

        checkCanceled()
        dataModel.types.values.forEach { it.attributes }

        checkCanceled()
        dataModel.rows.values.forEach { it.attributes }

        checkCanceled()
        dataModel.declarations.values.forEach { it.attributes }
    }
}
