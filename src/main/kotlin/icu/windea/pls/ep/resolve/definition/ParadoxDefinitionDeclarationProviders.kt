package icu.windea.pls.ep.resolve.definition

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configGroup.declarations
import icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 基础实现：基于现有声明上下文解析器，计算定义的声明规则。
 *
 * 适用于全部游戏类型，作为通用回退；更特殊的实现可通过 EP 覆盖。
 */
class ParadoxBaseDefinitionDeclarationProvider : ParadoxDefinitionDeclarationProvider {
    override fun getDeclaration(
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        matchOptions: Int
    ): CwtPropertyConfig? {
        val declarationConfig = definitionInfo.configGroup.declarations.get(definitionInfo.type) ?: return null
        val subtypes = definitionInfo.getSubtypeConfigs(matchOptions).map { it.name }
        val context = CwtDeclarationConfigContextProvider.getContext(
            definition,
            definitionInfo.name,
            definitionInfo.type,
            subtypes,
            definitionInfo.configGroup
        )
        return context?.getConfig(declarationConfig)
    }
}
