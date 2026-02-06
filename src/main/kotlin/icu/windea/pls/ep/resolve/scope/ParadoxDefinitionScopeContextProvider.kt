package icu.windea.pls.ep.resolve.scope

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 用于为定义提供作用域上下文。
 */
@WithGameTypeEP
interface ParadoxDefinitionScopeContextProvider {
    fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionScopeContextProvider>("icu.windea.pls.definitionScopeContextProvider")
    }
}
