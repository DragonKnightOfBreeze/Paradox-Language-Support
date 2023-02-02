package icu.windea.pls.config.core.component

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * 处理如何获取参数的上下文以及调用表达式中传入参数的上下文。
 * 
 * 对于快速文档中的参数信息：目前仅为支持参数的定义提供。
 */
interface ParadoxParameterResolver {
    companion object INSTANCE {
        @JvmStatic val EP_NAME = ExtensionPointName.create<ParadoxParameterResolver>("icu.windea.pls.paradoxParameterResolver")
        
        fun supports(context: ParadoxScriptDefinitionElement): Boolean {
            return EP_NAME.extensions.any { it.supports(context) }
        }
        
        fun findContext(element: PsiElement, file: PsiFile? = null): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.findContext(element, file) }
        }
        
        fun resolveParameter(name: String, element: PsiElement, file: PsiFile? = null): ParadoxParameterElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.resolveParameterWithContext(name, element, file) }
        }
        
        fun resolveParameterWithContext(name: String, element: PsiElement, context: ParadoxScriptDefinitionElement): ParadoxParameterElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.resolveParameter(name, element, context) }
        }
        
        fun resolveParameterFromInvocationExpression(name: String, element: ParadoxScriptProperty, config: CwtPropertyConfig): ParadoxParameterElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.resolveParameterFromInvocationExpression(name, element, config) }
        }
        
        fun processContextFromInvocationExpression(element: ParadoxScriptProperty, config: CwtPropertyConfig, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensions.any { it.processContextFromInvocationExpression(element, config, processor) }
        }
    }
    
    fun supports(context: ParadoxScriptDefinitionElement): Boolean
    
    fun findContext(element: PsiElement, file: PsiFile?) : ParadoxScriptDefinitionElement?
    
    fun resolveParameter(name: String, element: PsiElement, context: ParadoxScriptDefinitionElement): ParadoxParameterElement?
    
    fun resolveParameterWithContext(name: String, element: PsiElement, file: PsiFile?): ParadoxParameterElement? {
        val context = findContext(element, file) ?: return null
        return resolveParameter(name, element, context)
    }
    
    fun resolveParameterFromInvocationExpression(name: String, element: ParadoxScriptProperty, config: CwtPropertyConfig): ParadoxParameterElement?
    
    /**
     * @return 是否可以用来获取参数上下文。
     */
    fun processContextFromInvocationExpression(element: ParadoxScriptProperty, config: CwtPropertyConfig, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean
}
