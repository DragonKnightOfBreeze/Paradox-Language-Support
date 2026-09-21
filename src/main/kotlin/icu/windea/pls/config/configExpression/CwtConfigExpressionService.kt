package icu.windea.pls.config.configExpression

import com.intellij.util.Processor
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.expandUnionValues
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.text.TextPattern
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionSupport
import icu.windea.pls.ep.config.configExpression.CwtTextPatternBasedDataExpressionSupport

@Optimized
object CwtConfigExpressionService {
    /**
     * @see CwtDataExpressionSupport.resolve
     */
    fun resolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression? {
        CwtDataExpressionSupport.EP_NAME.extensionList.forEachFast { ep ->
            val r = ep.resolve(expressionString, role)
            if (r != null) return r
        }
        return null
    }

    /**
     * @see CwtDataExpressionSupport.resolveTemplate
     */
    fun resolveTemplate(expressionString: String): CwtDataExpression? {
        CwtDataExpressionSupport.EP_NAME.extensionList.forEachFast { ep ->
            val r = ep.resolveTemplate(expressionString)
            if (r != null) return r
        }
        return null
    }

    /**
     * @see CwtTextPatternBasedDataExpressionSupport.processTextPatterns
     */
    fun processTextPatterns(consumer: Processor<TextPattern<*>>): Boolean {
        CwtDataExpressionSupport.EP_NAME.extensionList.forEachFast { ep ->
            if (ep is CwtTextPatternBasedDataExpressionSupport) {
                ep.processTextPatterns(consumer).let { if (!it) return false }
            }
        }
        return true
    }

    fun collectLiterals(dataExpression: CwtDataExpression, configGroup: CwtConfigGroup, result: MutableSet<String>) {
        val dataType = dataExpression.type
        when (dataType) {
            CwtDataTypes.Bool -> {
                result += "yes"
                result += "no"
            }
            CwtDataTypes.Constant -> {
                val v = dataExpression.expressionString
                result += v
            }
            CwtDataTypes.EnumValue -> {
                val name = dataExpression.metadata.value ?: return
                val nextConfig = configGroup.enums[name] ?: return
                val values = nextConfig.values
                result += values
            }
            CwtDataTypes.UnionValue -> {
                val name = dataExpression.metadata.value ?: return
                val unionConfig = configGroup.unions[name] ?: return
                // NOTE 3.0.1 recursion guard is required here
                withRecursionGuard("CwtConfigExpressionService.collectLiterals") {
                    unionConfig.expandUnionValues { valueConfig ->
                        val e = valueConfig.configExpression
                        withRecursionCheck(e) {
                            collectLiterals(e, configGroup, result)
                        }
                        true
                    }
                }
            }
            CwtDataTypes.AliasKeysField, CwtDataTypes.AliasName -> {
                val name = dataExpression.metadata.value ?: return
                val aliasConfigGroup = configGroup.aliasGroups[name] ?: return
                // NOTE 3.0.1 recursion guard is required here
                withRecursionGuard("CwtConfigExpressionService.collectLiterals") {
                    withRecursionCheck(name) {
                        for (aliasConfigs in aliasConfigGroup.values) {
                            val e = aliasConfigs.firstOrNull()?.configExpression ?: continue
                            withRecursionCheck(e) {
                                collectLiterals(e, configGroup, result)
                            }
                        }
                    }
                }
            }
            CwtDataTypes.SingleAliasRight -> {
                val name = dataExpression.metadata.value ?: return
                val singleAliasConfig = configGroup.singleAliases[name] ?: return
                // NOTE 3.0.1 recursion guard is required here
                withRecursionGuard("CwtConfigExpressionService.collectLiterals") {
                    val e = singleAliasConfig.config.valueExpression
                    withRecursionCheck(e) {
                        collectLiterals(e, configGroup, result)
                    }
                }
            }
        }
    }
}
