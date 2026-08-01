package icu.windea.pls.ep.resolve.definition

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType

interface ParadoxDefinitionModifierProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxDefinitionModifierProvider>("icu.windea.pls.definitionModifierProvider")
    }
}
