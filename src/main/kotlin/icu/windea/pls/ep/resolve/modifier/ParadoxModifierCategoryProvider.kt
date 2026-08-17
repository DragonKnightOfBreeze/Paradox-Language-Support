package icu.windea.pls.ep.resolve.modifier

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModifierInfo

/**
 * 指定修正的修正分类。
 */
interface ParadoxModifierCategoryProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun getModifierCategories(modifier: ParadoxModifierLightElement): Map<String, CwtModifierCategoryConfig>?

    fun getModifierCategories(modifierInfo: ParadoxModifierInfo): Map<String, CwtModifierCategoryConfig>?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxModifierCategoryProvider>("icu.windea.pls.modifierCategoryProvider")
    }
}
