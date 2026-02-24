package icu.windea.pls.lang.resolve

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue

@Suppress("unused")
object ParadoxTriggerService {
    fun isWithinTriggerClause(element: ParadoxScriptMember): Boolean? {
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        val parentConfigs = generateSequence(config) { it.parentConfig }
        return parentConfigs.any { it is CwtPropertyConfig && it.aliasConfig?.name == "trigger" }
    }

    fun isNumberRepresentable(element: ParadoxScriptProperty): Boolean? {
        val propertyKey = element.propertyKey
        if (!isNumberRepresentable(propertyKey)) return false
        val propertyValue = element.propertyValue ?: return null
        if (!isNumberRepresentable(propertyValue)) return false
        return true
    }

    private fun isNumberRepresentable(element: ParadoxScriptPropertyKey): Boolean {
        // always true
        return true
    }

    private fun isNumberRepresentable(element: ParadoxScriptValue): Boolean {
        // string literal, or number after revolution and evaluation
        val resolved = element.resolved()
        return when (resolved) {
            is ParadoxScriptInt -> true
            is ParadoxScriptFloat -> true
            is ParadoxScriptInlineMath -> true
            is ParadoxScriptString -> true
            else -> false
        }
    }
}

