package icu.windea.pls.lang.parameter

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 提供对脚本参数的支持。
 *
 * @see ParadoxParameterElement
 */
interface ParadoxParameterSupport {
    fun isContext(element: ParadoxScriptDefinitionElement): Boolean
    
    fun findContext(element: PsiElement): ParadoxScriptDefinitionElement?
    
    fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo?
    
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
    
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement?

    /**
     * @param onlyMostRelevant 只获取最相关的上下文。
     * @return 此扩展点是否适用。
     */
    fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean
    
    /**
     * @param onlyMostRelevant 只获取最相关的上下文。
     * @return 此扩展点是否适用。
     */
    fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean
    
    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此扩展点是否适用。
     */
    fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: StringBuilder): Boolean = false
    
    fun getModificationTracker(parameterElement: ParadoxParameterElement): ModificationTracker? = null
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxParameterSupport>("icu.windea.pls.parameterSupport")
        
        fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
            return EP_NAME.extensionList.any { it.isContext(element) }
        }
        
        fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.findContext(element) }
        }
        
        fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getContextInfo(element)
            }
        }
        
        fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getContextReferenceInfo(element, from, *extraArgs)
            }
        }
        
        fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveParameter(element)
            }
        }
        
        fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveConditionParameter(element)
            }
        }
        
        fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveArgument(element, rangeInElement, config)
            }
        }
        
        fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.processContext(parameterElement, onlyMostRelevant, processor)
            }
        }
        
        fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.processContext(element, contextReferenceInfo, onlyMostRelevant, processor)
            }
        }
        
        fun getDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: StringBuilder): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.buildDocumentationDefinition(parameterElement, builder)
            }
        }
    }
    
    object Keys {
        val support = Key.create<ParadoxParameterSupport>("paradox.parameterElement.support")
        val containingContext = Key.create<SmartPsiElementPointer<ParadoxScriptDefinitionElement>>("paradox.parameterElement.containingContext")
        val containingContextReference = Key.create<SmartPsiElementPointer<ParadoxScriptDefinitionElement>>("paradox.parameterElement.contextReference")
        val definitionName = Key.create<String>("paradox.parameterElement.definitionName")
        val definitionTypes = Key.create<List<String>>("paradox.parameterElement.definitionTypes")
        val inlineScriptExpression = Key.create<String>("paradox.parameterElement.inlineScriptExpression")
    }
}