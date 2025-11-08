package icu.windea.pls.ep.resolve.definition

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 为定义提供子类型解析的扩展点。
 *
 * 典型用途：在不同游戏类型或规则差异下，定制子类型匹配逻辑。
 */
@WithGameTypeEP
interface ParadoxDefinitionSubtypesProvider {
    /**
     * 计算给定定义在指定匹配选项下的子类型规则列表。
     */
    fun getSubtypeConfigs(
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        matchOptions: Int = ParadoxMatchOptions.Default
    ): List<CwtSubtypeConfig>?

    companion object INSTANCE {
        val EP_NAME: ExtensionPointName<ParadoxDefinitionSubtypesProvider> =
            ExtensionPointName("icu.windea.pls.definitionSubtypesProvider")

        fun getSubtypeConfigs(
            definition: ParadoxScriptDefinitionElement,
            definitionInfo: ParadoxDefinitionInfo,
            matchOptions: Int = ParadoxMatchOptions.Default
        ): List<CwtSubtypeConfig>? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                ep.getSubtypeConfigs(definition, definitionInfo, matchOptions)
            }
        }
    }
}
