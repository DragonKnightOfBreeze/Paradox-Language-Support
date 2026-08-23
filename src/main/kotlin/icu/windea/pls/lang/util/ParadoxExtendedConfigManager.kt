package icu.windea.pls.lang.util

import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression

object ParadoxExtendedConfigManager {
    fun checkExtendedConfig(key: String, element: PsiElement, expectedConfig: CwtMemberConfig<*>): Boolean {
        val configGroup = expectedConfig.configGroup
        return ProcessorScope.anyFrom({ expectedConfig.expandConfigExpression { process(it) } }) { checkExtendedConfig(key, it, element, configGroup) }
    }

    fun checkExtendedConfig(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Boolean {
        if (expectedConfigs.isEmpty()) return false
        val value = element.value
        val configGroup = expectedConfigs.first().configGroup
        return ProcessorScope.anyFrom({ expectedConfigs.expandConfigExpression { process(it) } }) { checkExtendedConfig(value, it, element, configGroup) }
    }

    fun checkExtendedConfig(key: String, configExpression: CwtDataExpression, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        // NOTE 3.0.2 only for definition references and inline script expressions atm
        if (configExpression.type in CwtDataTypeSets.DefinitionAware) {
            val definitionType = configExpression.metadata.value ?: return false
            if (checkExtendedConfig(key, definitionType, element, configGroup)) return true
        }
        if (configExpression == ParadoxInlineScriptManager.inlineScriptPathExpression) {
            val config = configGroup.extendedInlineScripts.findByPattern(key, element, configGroup)
            if (config != null) return true
        }
        return false
    }

    fun checkExtendedConfig(key: String, definitionType: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val configs = configGroup.extendedDefinitions.findByPattern(key, element, configGroup).orEmpty()
        val config = configs.findFast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionType) }
        if (config != null) return true
        if (definitionType == ParadoxDefinitionTypes.gameRule) {
            val config = configGroup.extendedGameRules.findByPattern(key, element, configGroup)
            if (config != null) return true
        }
        if (definitionType == ParadoxDefinitionTypes.onAction) {
            val config = configGroup.extendedOnActions.findByPattern(key, element, configGroup)
            if (config != null) return true
        }
        return false
    }
}
