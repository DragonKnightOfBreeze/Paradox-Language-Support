package icu.windea.pls.ep.resolve.definition

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationTarget
import icu.windea.pls.lang.codeInsight.navigation.GotoSuperDefinitionActionHandler
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 提供对定义的继承逻辑的支持。
 *
 * 这个扩展点目前主要用于以下功能：
 * - 快速文档（[ParadoxDocumentationTarget]）。
 * - 导航到父定义的导航动作（[GotoSuperDefinitionActionHandler]）。
 * - 解析子定义的子类型（[ParadoxDefinitionService.processSubtypeConfigsFromInherit]）。
 */
@WithGameTypeEP
interface ParadoxDefinitionInheritSupport {
    /**
     * 从指定的定义信息得到父定义。
     *
     * **注意**：需要避免递归。
     */
    fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxDefinitionElement?

    /**
     * 如果一个定义继承自另一个定义，在解析其子类型时，可能需要进行额外的处理。
     * 例如，依赖于其父定义的子类型，或是其父定义的特定成员属性的值。
     *
     * **注意**：解析时需要避免递归。并且，不能直接访问 `definitionInfo.subtypeConfigs`，需要改为访问 [subtypeConfigs]。
     */
    fun processSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, subtypeConfigs: MutableList<CwtSubtypeConfig>): Boolean = true

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionInheritSupport>("icu.windea.pls.definitionInheritSupport")
    }
}
