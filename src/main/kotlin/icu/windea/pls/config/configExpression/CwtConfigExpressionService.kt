package icu.windea.pls.config.configExpression

import com.intellij.util.Processor
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.manipulation.CwtConfigExpansionService
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.text.TextPattern
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
            in CwtDataTypeSets.Expandable -> {
                // NOTE 3.0.3 recursion guard is required here
                CwtConfigExpansionService.expandExpandable(dataExpression, configGroup, "configExpression.collectLiterals") { e ->
                    collectLiterals(e, configGroup, result)
                    true
                }
            }
        }
    }
}
