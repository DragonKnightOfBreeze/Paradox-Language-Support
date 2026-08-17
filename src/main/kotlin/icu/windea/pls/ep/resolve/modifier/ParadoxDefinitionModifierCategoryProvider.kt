package icu.windea.pls.ep.resolve.modifier

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 指定定义的修正分类。
 */
interface ParadoxDefinitionModifierCategoryProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun getModifierCategories(definition: ParadoxDefinitionElement): Map<String, CwtModifierCategoryConfig>?

    fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxDefinitionModifierCategoryProvider>("icu.windea.pls.definitionModifierCategoryProvider")
    }
}
