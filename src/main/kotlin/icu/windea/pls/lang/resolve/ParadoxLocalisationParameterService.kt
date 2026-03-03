package icu.windea.pls.lang.resolve

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.util.builders.DocumentationBuilder
import icu.windea.pls.ep.resolve.parameter.ParadoxLocalisationParameterSupport
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

object ParadoxLocalisationParameterService {
    /**
     * @see ParadoxLocalisationParameterSupport.resolveParameter
     */
    fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxLocalisationParameterLightElement? {
        return ParadoxLocalisationParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.resolveParameter(localisationElement, name)
        }
    }

    /**
     * @see ParadoxLocalisationParameterSupport.resolveParameter
     */
    fun resolveParameter(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterLightElement? {
        return ParadoxLocalisationParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.resolveParameter(element)
        }
    }

    /**
     * @see ParadoxLocalisationParameterSupport.resolveArgument
     */
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterLightElement? {
        return ParadoxLocalisationParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.resolveArgument(element, rangeInElement, config)
        }
    }

    /**
     * @see ParadoxLocalisationParameterSupport.buildDocumentationDefinition
     */
    fun getDocumentationDefinition(element: ParadoxLocalisationParameterLightElement, builder: DocumentationBuilder): Boolean {
        return ParadoxLocalisationParameterSupport.EP_NAME.extensionList.any { ep ->
            ep.buildDocumentationDefinition(element, builder)
        }
    }
}
