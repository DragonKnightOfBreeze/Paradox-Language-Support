package icu.windea.pls.config.configExpression

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionMerger
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionPriorityProvider
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionResolver
import icu.windea.pls.ep.config.configExpression.CwtRuleBasedDataExpressionResolver

object CwtConfigExpressionService {
    private val CwtConfigGroup.dataExpressionPriorityCache by registerKey(CwtConfigGroup.Keys) {
        CacheBuilder("expireAfterAccess=30m").build<String, Double>().cancelable()
    }

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

    /**
     * @see CwtDataExpressionPriorityProvider.getPriority
     */
    fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Double {
        val cache = configGroup.dataExpressionPriorityCache
        return cache.get(configExpression.expressionString) { doGetPriority(configExpression, configGroup) }
    }

    private fun doGetPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Double {
        CwtDataExpressionPriorityProvider.EP_NAME.extensionList.forEach f@{ ep ->
            val r = ep.getPriority(configExpression, configGroup)
            if (r > 0) return r
        }
        return 0.0
    }
}
