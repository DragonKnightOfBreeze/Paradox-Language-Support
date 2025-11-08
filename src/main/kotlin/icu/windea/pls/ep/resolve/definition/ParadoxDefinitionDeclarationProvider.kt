package icu.windea.pls.ep.resolve.definition

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 为定义提供声明规则解析的扩展点。
 *
 * 典型用途：针对不同游戏或类型的定义，定制声明规则匹配逻辑。
 */
@WithGameTypeEP
interface ParadoxDefinitionDeclarationProvider {
    /**
     * 计算给定定义在指定匹配选项下的声明规则。
     */
    fun getDeclaration(
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        matchOptions: Int = ParadoxMatchOptions.Default
    ): CwtPropertyConfig?

    companion object INSTANCE {
        val EP_NAME: ExtensionPointName<ParadoxDefinitionDeclarationProvider> =
            ExtensionPointName("icu.windea.pls.definitionDeclarationProvider")

        fun getDeclaration(
            definition: ParadoxScriptDefinitionElement,
            definitionInfo: ParadoxDefinitionInfo,
            matchOptions: Int = ParadoxMatchOptions.Default
        ): CwtPropertyConfig? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                ep.getDeclaration(definition, definitionInfo, matchOptions)
            }
        }
    }
}
