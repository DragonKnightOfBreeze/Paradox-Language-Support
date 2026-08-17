package icu.windea.pls.ep.codeInsight.documentation

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.text.DocumentationBuilder
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 指定与修正有关的快速文档的构建逻辑。
 */
interface ParadoxModifierQuickDocProvider {
    fun supports(gameType: ParadoxGameType) = true

    /**
     * 构建修正的快速文档中的定义部分。
     * 如果返回 `true`，则表示此 EP 适用，因而终止遍历 EP。
     */
    fun buildDefinitionPart(element: ParadoxModifierLightElement, builder: DocumentationBuilder): Boolean

    /**
     * 构建定义的快速文档中的定义部分中的对应的生成的修正的那一部分。
     * 如果返回 `true`，则表示此 EP 适用，因而终止遍历 EP。
     */
    fun buildDefinitionPartForDefinition(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxModifierQuickDocProvider>("icu.windea.pls.modifierQuickDocProvider")
    }
}
