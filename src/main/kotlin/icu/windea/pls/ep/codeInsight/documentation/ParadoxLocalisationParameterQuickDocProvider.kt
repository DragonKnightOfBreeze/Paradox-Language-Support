package icu.windea.pls.ep.codeInsight.documentation

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.text.DocumentationBuilder
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement

/**
 * 指定与本地化参数有关的快速文档的构建逻辑。
 */
interface ParadoxLocalisationParameterQuickDocProvider {
    /**
     * 构建参数的快速文档中的定义部分。
     * 如果返回 `true`，则表示此 EP 适用，因而终止遍历 EP。
     */
    fun buildDefinitionPart(element: ParadoxLocalisationParameterLightElement, builder: DocumentationBuilder): Boolean

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxLocalisationParameterQuickDocProvider>("icu.windea.pls.localisationParameterQuickDocProvider")
    }
}
