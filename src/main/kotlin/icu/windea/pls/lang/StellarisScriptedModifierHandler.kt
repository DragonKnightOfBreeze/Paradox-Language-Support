package icu.windea.pls.lang

import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
object StellarisScriptedModifierHandler {
    /**
     * 输入[definition]的定义类型应当保证是`scripted_modifier`。
     */
    fun resolveModifierCategory(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        if(selectGameType(definition) != ParadoxGameType.Stellaris) return null
        val configGroup = definitionInfo.configGroup
        val value = definition.findProperty("category")?.propertyValue?.stringValue()
        val finalValue = value ?: "economic_unit" //default to economic_unit
        val enumConfig = configGroup.enums.getValue("scripted_modifier_category")
        var keys = getModifierCategoryOptionValues(enumConfig, finalValue)
        if(keys == null) keys = getModifierCategoryOptionValues(enumConfig, "economic_unit")
        if(keys == null) keys = emptySet() //unexpected
        val modifierCategories = configGroup.modifierCategories
        return keys.associateWith { modifierCategories.getValue(it) }
    }
    
    fun getModifierCategoryOptionValues(enumConfig: CwtEnumConfig, finalValue: String): Set<String>? {
        val valueConfig = enumConfig.valueConfigMap.getValue(finalValue)
        return valueConfig.getOrPutUserData(StellarisEconomicCategoryHandler.modifierCategoriesKey, emptySet()) {
            valueConfig.options?.find { it.key == "modifier_categories" }
                ?.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue }
        }
    }
}
