package icu.windea.pls.ep.codeInsight.documentation

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.util.builders.DocumentationBuilder
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement

/**
 * 指定与脚本参数有关的快速文档的构建逻辑。
 */
interface ParadoxParameterQuickDocProvider {
    /**
     * 构建参数的快速文档中的定义部分。
     * 如果返回 `true`，则表示此 EP 适用，因而终止遍历 EP。
     */
    fun buildDefinitionPart(element: ParadoxParameterLightElement, builder: DocumentationBuilder): Boolean

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxParameterQuickDocProvider>("icu.windea.pls.parameterQuickDocProvider")
    }
}
