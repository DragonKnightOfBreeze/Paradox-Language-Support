package icu.windea.pls.ep.resolve.definition

import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 基础实现：基于 CWT 子类型规则与现有匹配逻辑，计算定义的子类型集合。
 *
 * 适用于全部游戏类型，作为通用回退；更特殊的实现可通过 EP 以更高优先级覆盖。
 */
class ParadoxBaseDefinitionSubtypesProvider : ParadoxDefinitionSubtypesProvider {
    override fun getSubtypeConfigs(
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        matchOptions: Int
    ): List<CwtSubtypeConfig>? {
        val typeConfig = definitionInfo.typeConfig
        val typeKey = definitionInfo.typeKey
        val configGroup = definitionInfo.configGroup
        val result = buildList {
            for (subtypeConfig in typeConfig.subtypes.values) {
                if (ParadoxDefinitionManager.matchesSubtype(definition, typeKey, subtypeConfig, this, configGroup, matchOptions)) {
                    this += subtypeConfig
                }
            }
        }
        return result.optimized()
    }
}
