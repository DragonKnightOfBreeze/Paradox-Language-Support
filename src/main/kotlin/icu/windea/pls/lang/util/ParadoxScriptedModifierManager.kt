package icu.windea.pls.lang.util

import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.select.*
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.select.ofKey
import icu.windea.pls.lang.select.one
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

object ParadoxScriptedModifierManager {
    /**
     * 输入的 [definition] 的定义类型应当保证是 `scripted_modifier`。
     */
    fun resolveModifierCategory(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig> {
        val configGroup = definitionInfo.configGroup
        val property = selectScope { definition.properties().ofKey("category").one() }
        val value = property?.propertyValue?.stringValue()
        return ParadoxModifierManager.resolveModifierCategory(value, configGroup)
    }
}
