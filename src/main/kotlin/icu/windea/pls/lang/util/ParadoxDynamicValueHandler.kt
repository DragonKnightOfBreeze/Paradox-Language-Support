package icu.windea.pls.lang.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.script.psi.*

object ParadoxDynamicValueHandler {
    const val EVENT_TARGET_PREFIX = "event_target:"
    
    val EVENT_TARGETS = setOf("event_target", "global_event_target")
    
    fun getName(expression: String): String? {
        return expression.substringBefore('@').orNull()
    }
    
    fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return when(configExpression.type) {
            CwtDataTypes.Value -> Access.Read
            CwtDataTypes.ValueSet -> Access.Write
            CwtDataTypes.DynamicValue -> Access.ReadWrite
            else -> Access.ReadWrite
        }
    }
    
    fun resolveDynamicValue(element: ParadoxExpressionElement, name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxDynamicValueElement? {
        val gameType = configGroup.gameType ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val dynamicValueType = configExpression.value ?: return null
        return ParadoxDynamicValueElement(element, name, dynamicValueType, readWriteAccess, gameType, configGroup.project)
    }
    
    fun resolveDynamicValue(element: ParadoxExpressionElement, name: String, configExpressions: Iterable<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxDynamicValueElement? {
        val gameType = configGroup.gameType ?: return null
        val configExpression = configExpressions.firstOrNull() ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val dynamicValueTypes = configExpressions.mapNotNullTo(mutableSetOf()) { it.value }
        return ParadoxDynamicValueElement(element, name, dynamicValueTypes, readWriteAccess, gameType, configGroup.project)
    }
}
