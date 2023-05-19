package icu.windea.pls.lang.parameter

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
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
    
    /**
     * @param extraArgs 对于每个实现需要的额外参数可能是不同的。
     */
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, vararg extraArgs: Any?): ParadoxParameterElement?
    
    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = false
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxLocalisationParameterSupport>("icu.windea.pls.localisationParameterSupport")
        
        fun resolveParameter(element: ParadoxLocalisationPropertyReference): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveParameter(element)
            }
        }
        
        fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, vararg extraArgs: Any?): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveArgument(element, rangeInElement, *extraArgs) 
            }
        }
        
        fun getDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.buildDocumentationDefinition(element, builder) 
            }
        }
    }
}