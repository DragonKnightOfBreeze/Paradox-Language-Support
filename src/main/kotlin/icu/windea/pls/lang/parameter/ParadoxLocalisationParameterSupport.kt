package icu.windea.pls.lang.parameter

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 提供对本地化参数的支持。
 *
 * @see ParadoxParameterElement
 */
interface ParadoxLocalisationParameterSupport {
    fun resolveParameter(element: ParadoxLocalisationPropertyReference): ParadoxParameterElement?
    
    fun resolveArgument(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxParameterElement?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxLocalisationParameterSupport>("icu.windea.pls.localisationParameterSupport")
        
        fun resolveParameter(element: ParadoxLocalisationPropertyReference): ParadoxParameterElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.resolveParameter(element) }
        }
        
        fun resolveArgument(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxParameterElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.resolveArgument(element, config) }
        }
    }
}