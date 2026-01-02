package icu.windea.pls.ep.resolve.parameter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.codeInsight.documentation.DocumentationBuilder
import icu.windea.pls.lang.psi.mock.ParadoxLocalisationParameterElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 提供对本地化参数的支持。
 *
 * @see ParadoxLocalisationParameterElement
 */
interface ParadoxLocalisationParameterSupport {
    fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxLocalisationParameterElement?

    fun resolveParameter(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterElement?

    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterElement?

    /**
     * 构建参数的快速文档中的定义部分。
     *
     * @return 此扩展点是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxLocalisationParameterElement, builder: DocumentationBuilder): Boolean = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxLocalisationParameterSupport>("icu.windea.pls.localisationParameterSupport")
    }
}
