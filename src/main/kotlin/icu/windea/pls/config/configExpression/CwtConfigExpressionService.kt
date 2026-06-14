package icu.windea.pls.config.configExpression

import com.intellij.util.Processor
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.text.TextPattern
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionSupport

object CwtConfigExpressionService {
    /**
     * @see CwtDataExpressionSupport.resolve
     */
    fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        CwtDataExpressionSupport.EP_NAME.extensionList.forEach { ep ->
            val r = ep.resolve(expressionString, isKey)
            if (r != null) return r
        }
        return null
    }

    /**
     * @see CwtDataExpressionSupport.resolveTemplate
     */
    fun resolveTemplate(expressionString: String): CwtDataExpression? {
        CwtDataExpressionSupport.EP_NAME.extensionList.forEach { ep ->
            val r = ep.resolveTemplate(expressionString)
            if (r != null) return r
        }
        return null
    }

    /**
     * @see CwtDataExpressionSupport.processTextPatterns
     */
    fun processTextPatterns(consumer: Processor<TextPattern<*>>): Boolean {
        return CwtDataExpressionSupport.EP_NAME.extensionList.process { ep ->
            ep.processTextPatterns(consumer)
        }
    }

    fun collectLiterals(configExpression: CwtDataExpression, configGroup: CwtConfigGroup, result: MutableSet<String>) {
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
                            collectLiterals(e, configGroup, result)
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
                        collectLiterals(e, configGroup, result)
                    }
                }
            }
        }
    }
}
