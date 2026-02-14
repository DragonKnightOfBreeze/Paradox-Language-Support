package icu.windea.pls.config.configExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionMerger
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionResolver
import icu.windea.pls.ep.config.configExpression.CwtRuleBasedDataExpressionResolver

object CwtConfigExpressionService {
    val allRules by lazy {
        CwtDataExpressionResolver.EP_NAME.extensionList.filterIsInstance<CwtRuleBasedDataExpressionResolver>().flatMap { it.rules }
    }

    /**
     * @see CwtDataExpressionResolver.resolve
     */
    fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        CwtDataExpressionResolver.EP_NAME.extensionList.forEach f@{ ep ->
            val r = ep.resolve(expressionString, isKey)
            if (r != null) return r
        }
        return null
    }

    /**
     * @see CwtDataExpressionResolver.resolve
     */
    fun resolveTemplate(expressionString: String): CwtDataExpression? {
        CwtDataExpressionResolver.EP_NAME.extensionList.forEach f@{ ep ->
            if (ep !is CwtRuleBasedDataExpressionResolver) return@f
            val r = ep.resolve(expressionString, false)
            if (r != null) return r
        }
        return null
    }

    /**
     * @see CwtDataExpressionMerger.merge
     */
    fun merge(configExpression1: CwtDataExpression, configExpression2: CwtDataExpression, configGroup: CwtConfigGroup): String? {
        if (configExpression1 == configExpression2) return configExpression1.expressionString
        CwtDataExpressionMerger.EP_NAME.extensionList.forEach f@{ ep ->
            val r = ep.merge(configExpression1, configExpression2, configGroup)
            if (r != null) return r
        }
        return null
    }

    fun findLiterals(configExpression: CwtDataExpression, configGroup: CwtConfigGroup, result: MutableSet<String>) {
        val dataType = configExpression.type
        when (dataType) {
            CwtDataTypes.Bool -> {
                result += "yes"
                result += "no"
            }
            CwtDataTypes.Constant -> {
                val v = configExpression.value ?: return
                result += v
            }
            CwtDataTypes.EnumValue -> {
                val name = configExpression.value ?: return
                val nextConfig = configGroup.enums[name] ?: return
                val values = nextConfig.values
                result += values
            }
            CwtDataTypes.AliasName, CwtDataTypes.AliasKeysField -> {
                val name = configExpression.value ?: return
                val aliasConfigGroup = configGroup.aliasGroups[name] ?: return
                withRecursionGuard { // 这里需要防止递归
                    for (aliasConfigs in aliasConfigGroup.values) {
                        val e = aliasConfigs.firstOrNull()?.configExpression ?: continue
                        withRecursionCheck(e) {
                            findLiterals(e, configGroup, result)
                        }
                    }
                }
            }
            CwtDataTypes.SingleAliasRight -> {
                val name = configExpression.value ?: return
                val singleAliasConfig = configGroup.singleAliases[name] ?: return
                withRecursionGuard { // 这里需要防止递归
                    val e = singleAliasConfig.config.valueExpression
                    withRecursionCheck(e) {
                        findLiterals(e, configGroup, result)
                    }
                }
            }
        }
    }
}
