package icu.windea.pls.lang.resolve

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.ep.resolve.parameter.ParadoxLocalisationParameterSupport
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

@Optimized
object ParadoxLocalisationParameterService {
    /**
     * @see ParadoxLocalisationParameterSupport.resolveParameter
     */
    fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxLocalisationParameterLightElement? {
        val supports = ParadoxLocalisationParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.resolveParameter(localisationElement, name)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxLocalisationParameterSupport.resolveParameter
     */
    fun resolveParameter(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterLightElement? {
        val supports = ParadoxLocalisationParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.resolveParameter(element)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxLocalisationParameterSupport.resolveArgument
     */
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterLightElement? {
        val supports = ParadoxLocalisationParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.resolveArgument(element, rangeInExpression, config)?.let { return it }
        }
        return null
    }
}
