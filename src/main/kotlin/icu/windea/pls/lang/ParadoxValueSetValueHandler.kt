package icu.windea.pls.lang

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetValueHandler {
    const val EVENT_TARGET_PREFIX = "event_target:"
    
    val EVENT_TARGETS = setOf("event_target", "global_event_target")
    
    fun getName(expression: String): String? {
        return expression.substringBefore('@').orNull()
    }
    
    fun getReadWriteAccess(configExpression: CwtDataExpression): Access {
        return when(configExpression.type) {
            CwtDataTypes.Value -> Access.Read
            CwtDataTypes.ValueSet -> Access.Write
            CwtDataTypes.ValueOrValueSet -> Access.ReadWrite
            else -> Access.ReadWrite
        }
    }
    
    fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
        val gameType = configGroup.gameType ?: return null
        if(element !is ParadoxScriptStringExpressionElement) return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val valueSetName = configExpression.value ?: return null
        return ParadoxValueSetValueElement(element, name, valueSetName, readWriteAccess, gameType, configGroup.project)
    }
    
    fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpressions: Iterable<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxValueSetValueElement? {
        val gameType = configGroup.gameType ?: return null
        if(element !is ParadoxScriptStringExpressionElement) return null
        val configExpression = configExpressions.firstOrNull() ?: return null
        val readWriteAccess = getReadWriteAccess(configExpression)
        val valueSetNames = configExpressions.mapNotNullTo(mutableSetOf()) { it.value }
        return ParadoxValueSetValueElement(element, name, valueSetNames, readWriteAccess, gameType, configGroup.project)
    }
}
