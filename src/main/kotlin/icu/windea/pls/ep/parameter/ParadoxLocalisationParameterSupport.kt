package icu.windea.pls.ep.parameter

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 提供对本地化参数的支持。
 *
 * @see icu.windea.pls.lang.psi.mock.ParadoxLocalisationParameterElement
 */
interface ParadoxLocalisationParameterSupport {
    fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxLocalisationParameterElement?

    fun resolveParameter(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterElement?

    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterElement?

    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxLocalisationParameterElement, builder: DocumentationBuilder): Boolean = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxLocalisationParameterSupport>("icu.windea.pls.localisationParameterSupport")

        fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxLocalisationParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveParameter(localisationElement, name)
            }
        }

        fun resolveParameter(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveParameter(element)
            }
        }

        fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveArgument(element, rangeInElement, config)
            }
        }

        fun getDocumentationDefinition(element: ParadoxLocalisationParameterElement, builder: DocumentationBuilder): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.buildDocumentationDefinition(element, builder)
            }
        }
    }
}
