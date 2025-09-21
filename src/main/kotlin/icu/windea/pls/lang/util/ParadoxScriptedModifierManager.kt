package icu.windea.pls.lang.util

import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.stringValue

object ParadoxScriptedModifierManager {
    /**
     * 输入[definition]的定义类型应当保证是`scripted_modifier`。
     */
    fun resolveModifierCategory(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig> {
        val configGroup = definitionInfo.configGroup
        val value = definition.findProperty("category", inline = true)?.propertyValue?.stringValue()
        return ParadoxModifierManager.resolveModifierCategory(value, configGroup)
    }
}
