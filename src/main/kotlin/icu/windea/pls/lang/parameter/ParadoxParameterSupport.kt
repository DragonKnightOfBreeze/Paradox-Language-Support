package icu.windea.pls.lang.parameter

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 提供对脚本参数的支持。
 *
 * @see ParadoxParameterElement
 */
interface ParadoxParameterSupport {
    fun isContext(element: ParadoxScriptDefinitionElement): Boolean
    
    fun findContext(element: PsiElement): ParadoxScriptDefinitionElement?
    
    /**
     * 向上查找参数的上下文引用信息。
     *
     * @param element 开始查找的位置。
     * @param from 从哪里向上查找。
     * @param extraArgs 对于每个实现需要的额外参数可能是不同的。
     * @see icu.windea.pls.script.codeInsight.parameterInfo.ParadoxParameterInfoHandler
     */
    fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo?
    
    fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement?
    
    fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement?
    
    /**
     * @param extraArgs 对于每个实现需要的额外参数可能是不同的。
     */
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, vararg extraArgs: Any?): ParadoxParameterElement?
    
    fun getContainingContext(element: ParadoxParameterElement): ParadoxScriptDefinitionElement?
    
    /**
     * @return 此扩展点是否适用。
     */
    fun processContext(element: ParadoxParameterElement, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean
    
    /**
     * @return 此扩展点是否适用。
     */
    fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean
    
    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此扩展点是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = false
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxParameterSupport>("icu.windea.pls.parameterSupport")
        
        fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
            return EP_NAME.extensionList.any { it.isContext(element) }
        }
        
        fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.findContext(element) }
        }
        
        fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.getContextReferenceInfo(element, from, *extraArgs) }
        }
        
        fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.resolveParameter(element) }
        }
        
        fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.resolveConditionParameter(element) }
        }
        
        fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, vararg extraArgs: Any?): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.resolveArgument(element, rangeInElement, *extraArgs) }
        }
        
        fun getContainingContext(element: ParadoxParameterElement): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.getContainingContext(element) }
        }
        
        fun processContext(element: ParadoxParameterElement, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensionList.any { it.processContext(element, processor) }
        }
        
        fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensionList.any { it.processContext(element, contextReferenceInfo, processor) }
        }
        
        fun getDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean {
            return EP_NAME.extensionList.any { it.buildDocumentationDefinition(element, builder) }
        }
    }
}