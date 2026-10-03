package icu.windea.pls.config.util

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast

@Suppress("unused")
@Optimized
object CwtConfigExpressionManager {
    fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Double {
        val dataType = configExpression.type
        return dataType.priority ?: dataType.priorityProvider?.invoke(configExpression, configGroup) ?: 0.0
    }

    fun resolvePlaceholder(locationExpression: CwtLocationExpression, name: String): String? {
        if (!locationExpression.isPlaceholder) return null
        val r = buildString { for (c in locationExpression.location) if (c == '$') append(name) else append(c) }
        return when {
            locationExpression is CwtLocalisationLocationExpression && locationExpression.forceUpperCase -> r.uppercase()
            else -> r
        }
    }

    fun extract(templateExpression: CwtTemplateExpression, referenceName: String): String {
        if (templateExpression.referenceExpressions.size != 1) throw IllegalStateException()
        return buildString {
            templateExpression.snippetExpressions.forEachFast { snippetExpression ->
                when (snippetExpression.type) {
                    CwtDataTypes.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceName)
                }
            }
        }
    }

    fun extract(templateExpression: CwtTemplateExpression, referenceNames: Map<CwtDataExpression, String>): String {
        if (templateExpression.referenceExpressions.size != referenceNames.size) throw IllegalStateException()
        return buildString {
            templateExpression.snippetExpressions.forEachFast { snippetExpression ->
                when (snippetExpression.type) {
                    CwtDataTypes.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceNames.getValue(snippetExpression))
                }
            }
        }
    }
}
