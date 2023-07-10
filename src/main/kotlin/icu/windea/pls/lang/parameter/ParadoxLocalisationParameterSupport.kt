package icu.windea.pls.lang.parameter

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 提供对本地化参数的支持。
 *
 * @see ParadoxParameterElement
 */
interface ParadoxLocalisationParameterSupport {
    fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String) : ParadoxParameterElement?
    
    fun resolveParameter(element: ParadoxLocalisationPropertyReference): ParadoxParameterElement?
    
    /**
     * @param element 传入参数名对应的PSI。
     * @param rangeInElement 传入参数名对应的在[element]中的文本范围。
     * @param config [element]对应的CWT规则。
     */
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement?
    
    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = false
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxLocalisationParameterSupport>("icu.windea.pls.localisationParameterSupport")
        
        fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveParameter(localisationElement, name)
            }
        }
        
        fun resolveParameter(element: ParadoxLocalisationPropertyReference): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveParameter(element)
            }
        }
        
        fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveArgument(element, rangeInElement, config) 
            }
        }
        
        fun getDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.buildDocumentationDefinition(element, builder) 
            }
        }
    }
}