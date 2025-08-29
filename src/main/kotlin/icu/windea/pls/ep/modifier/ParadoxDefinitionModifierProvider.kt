package icu.windea.pls.ep.modifier

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtModifierCategoryConfig
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

@WithGameTypeEP
interface ParadoxDefinitionModifierProvider {
    fun getModifierCategories(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionModifierProvider>("icu.windea.pls.definitionModifierProvider")

        fun getModifierCategories(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getModifierCategories(definition, definitionInfo)
            }
        }
    }
}

