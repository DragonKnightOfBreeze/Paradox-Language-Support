package icu.windea.pls.ep.resolve.scope

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于为定义提供支持的作用域。
 */
interface ParadoxDefinitionSupportedScopesProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getSupportedScopes(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxDefinitionSupportedScopesProvider>("icu.windea.pls.definitionSupportedScopesProvider")
    }
}
