package icu.windea.pls.ep.scope

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于为定义提供支持的作用域。
 */
@WithGameTypeEP
interface ParadoxDefinitionSupportedScopesProvider {
    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionSupportedScopesProvider>("icu.windea.pls.definitionSupportedScopesProvider")

        fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                if (!ep.supports(definition, definitionInfo)) return@f null
                ep.getSupportedScopes(definition, definitionInfo)
            }
        }
    }
}
