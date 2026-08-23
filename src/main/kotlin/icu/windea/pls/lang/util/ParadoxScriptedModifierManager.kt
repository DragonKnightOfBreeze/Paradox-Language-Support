package icu.windea.pls.lang.util

import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.model.ParadoxDefinitionInfo

object ParadoxScriptedModifierManager {
    /**
     * 输入的 [definition] 的定义类型应当保证是 `scripted_modifier`。
     */
    fun getModifierCategory(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig> {
        val configGroup = definitionInfo.configGroup
        val property = selectScope { definition.properties().ofKey("category").one() }
        val value = property?.propertyValue?.stringValue()
        return ParadoxConfigService.getModifierCategories(value, configGroup)
    }
}
