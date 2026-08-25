package icu.windea.pls.ep.scope

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.scope.ParadoxScopeContext

/**
 * 用于为定义提供作用域上下文。
 */
interface ParadoxDefinitionScopeContextProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxDefinitionScopeContextProvider>("icu.windea.pls.definitionScopeContextProvider")
    }
}
