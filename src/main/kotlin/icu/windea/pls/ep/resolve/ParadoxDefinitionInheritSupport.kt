package icu.windea.pls.ep.resolve

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.codeInsight.navigation.ParadoxGotoSuperDefinitionActionHandler
import icu.windea.pls.lang.documentation.ParadoxDocumentationTarget
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于提供对定义的继承逻辑的支持。
 *
 * 这个扩展点目前主要用于以下功能：
 *
 * - 快速文档（[ParadoxDocumentationTarget]）。
 * - 导航到父定义的导航动作（[ParadoxGotoSuperDefinitionActionHandler]）。
 */
@WithGameTypeEP
interface ParadoxDefinitionInheritSupport {
    fun getSuperDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionInheritSupport>("icu.windea.pls.definitionInheritSupport")

        fun getSuperDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getSuperDefinition(definition, definitionInfo)
            }
        }
    }
}
