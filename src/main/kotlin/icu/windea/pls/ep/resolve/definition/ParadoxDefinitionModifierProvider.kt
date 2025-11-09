package icu.windea.pls.ep.resolve.definition

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.model.ParadoxDefinitionInfo

@WithGameTypeEP
interface ParadoxDefinitionModifierProvider {
    fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionModifierProvider>("icu.windea.pls.definitionModifierProvider")
    }
}
