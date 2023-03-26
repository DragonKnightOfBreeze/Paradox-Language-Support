package icu.windea.pls.lang.modifier

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

interface ParadoxDefinitionModifierProvider {
    fun getModifierCategories(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionModifierProvider>("icu.windea.pls.definitionModifierProvider")
        
        fun getModifierCategories(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.getModifierCategories(definition, definitionInfo) }
        }
    }
}

