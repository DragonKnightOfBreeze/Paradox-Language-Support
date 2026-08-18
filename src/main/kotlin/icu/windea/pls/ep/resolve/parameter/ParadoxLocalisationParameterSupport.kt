package icu.windea.pls.ep.resolve.parameter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
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

    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterLightElement?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxLocalisationParameterSupport>("icu.windea.pls.localisationParameterSupport")
    }
}
