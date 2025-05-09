package icu.windea.pls.lang.util

import icu.windea.pls.config.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxScriptedModifierManager {
    /**
     * 输入[definition]的定义类型应当保证是`scripted_modifier`。
     */
    fun resolveModifierCategory(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        val configGroup = definitionInfo.configGroup
        val value = definition.findProperty("category", inline = true)?.propertyValue?.stringValue()
        return ParadoxModifierManager.resolveModifierCategory(value, configGroup)
    }
}
