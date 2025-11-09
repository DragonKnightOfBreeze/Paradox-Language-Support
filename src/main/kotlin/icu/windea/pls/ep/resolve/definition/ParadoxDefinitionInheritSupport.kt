package icu.windea.pls.ep.resolve.definition

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.codeInsight.navigation.GotoSuperDefinitionActionHandler
import icu.windea.pls.lang.documentation.ParadoxDocumentationTarget
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于提供对定义的继承逻辑的支持。
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
    fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement?

    fun getModificationTracker(definitionInfo: ParadoxDefinitionInfo): ModificationTracker? = null

    /**
     * 如果一个定义继承自另一个定义，在解析其子类型时，可能需要进行额外的处理。
     * 例如，依赖于其父定义的子类型，或是其父定义的特定成员属性的值。
     *
     * 如果实现了这个方法，需要同时考虑实现 [getModificationTracker]。
     *
     * **注意**：需要避免递归。
     */
    fun processSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, subtypeConfigs: MutableList<CwtSubtypeConfig>) {}

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionInheritSupport>("icu.windea.pls.definitionInheritSupport")
    }
}
