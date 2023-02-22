package icu.windea.pls.lang.support

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * 提供对参数的支持。
 * 
 * 例如，如何解析参数，如何获取参数的上下文，如何获取调用表达式中传入参数的上下文。
 * 
 * @see ParadoxParameterElement
 */
interface ParadoxParameterSupport {
    fun supports(context: ParadoxScriptDefinitionElement): Boolean
    
    fun findContext(element: PsiElement, file: PsiFile?) : ParadoxScriptDefinitionElement?
    
    fun resolveParameter(name: String, element: PsiElement, context: ParadoxScriptDefinitionElement): ParadoxParameterElement?
    
    fun resolveParameterWithContext(name: String, element: PsiElement, file: PsiFile?): ParadoxParameterElement? {
        val context = findContext(element, file) ?: return null
        return resolveParameter(name, element, context)
    }
    
    /**
     * @param element 调用表达式。（如，`some_scripted_effect = { PARAM = str }`）
     */
    fun resolveParameterFromInvocationExpression(name: String, element: ParadoxScriptProperty, config: CwtPropertyConfig): ParadoxParameterElement?
    
    /**
     * @return 此解析器是否适用。
     */
    fun processContextFromInvocationExpression(element: ParadoxScriptProperty, config: CwtPropertyConfig, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean
    
    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = false
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxParameterSupport>("icu.windea.pls.paradoxParameterSupport")
        
        fun supports(context: ParadoxScriptDefinitionElement): Boolean {
            return EP_NAME.extensionList.any { it.supports(context) }
        }
        
        fun findContext(element: PsiElement, file: PsiFile? = null): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.findContext(element, file) }
        }
        
        fun resolveParameter(name: String, element: PsiElement, file: PsiFile? = null): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.resolveParameterWithContext(name, element, file) }
        }
        
        fun resolveParameterWithContext(name: String, element: PsiElement, context: ParadoxScriptDefinitionElement): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.resolveParameter(name, element, context) }
        }
        
        fun resolveParameterFromInvocationExpression(name: String, element: ParadoxScriptProperty, config: CwtPropertyConfig): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.resolveParameterFromInvocationExpression(name, element, config) }
        }
        
        fun processContextFromInvocationExpression(element: ParadoxScriptProperty, config: CwtPropertyConfig, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensionList.any { it.processContextFromInvocationExpression(element, config, processor) }
        }
        
        fun getDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean {
            return EP_NAME.extensionList.any { it.buildDocumentationDefinition(element, builder) }
        }
    }
}
