package icu.windea.pls.ep.resolve.scope

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 用于为定义提供支持的作用域。
 */
@WithGameTypeEP
interface ParadoxDefinitionSupportedScopesProvider {
    fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getSupportedScopes(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionSupportedScopesProvider>("icu.windea.pls.definitionSupportedScopesProvider")
    }
}
