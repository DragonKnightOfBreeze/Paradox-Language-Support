package icu.windea.pls.ep.resolve.parameter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.util.builders.DocumentationBuilder
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 提供对本地化参数的支持。
 *
 * @see ParadoxLocalisationParameterLightElement
 */
interface ParadoxLocalisationParameterSupport {
    fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxLocalisationParameterLightElement?

    fun resolveParameter(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterLightElement?

    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterLightElement?

    /**
     * 构建参数的快速文档中的定义部分。
     *
     * @return 此扩展点是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxLocalisationParameterLightElement, builder: DocumentationBuilder): Boolean = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxLocalisationParameterSupport>("icu.windea.pls.localisationParameterSupport")
    }
}
