package icu.windea.pls.lang.parameter

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.cwt.config.*
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
     * @param element 传入参数名对应的PSI。
     * @param rangeInElement 传入参数名对应的在[element]中的文本范围。
     * @param config [element]对应的CWT规则。
     */
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement?
    
    fun getContainingContext(element: ParadoxParameterElement): ParadoxScriptDefinitionElement?
    
    fun getContainingContextReference(element: ParadoxParameterElement): ParadoxScriptDefinitionElement?
    
    /**
     * @param onlyMostRelevant 只获取最相关的上下文。
     * @return 此扩展点是否适用。
     */
    fun processContext(element: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean
    
    /**
     * @param onlyMostRelevant 只获取最相关的上下文。
     * @return 此扩展点是否适用。
     */
    fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean
    
    /**
     * 如果返回值不为null，表示推断得到的CWT规则可以通过一定条件进行缓存。
     */
    fun getModificationTracker(parameterElement: ParadoxParameterElement): ModificationTracker? = null
    
    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此扩展点是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = false
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxParameterSupport>("icu.windea.pls.parameterSupport")
        
        fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
            return EP_NAME.extensionList.any { it.isContext(element) }
        }
        
        fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.findContext(element) }
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
        
        fun getContainingContext(element: ParadoxParameterElement): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getContainingContext(element)
            }
        }
        
        fun getContainingContextReference(element: ParadoxParameterElement): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getContainingContextReference(element)
            }
        }
        
        fun processContext(element: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.processContext(element, onlyMostRelevant, processor)
            }
        }
        
        fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.processContext(element, contextReferenceInfo, onlyMostRelevant, processor)
            }
        }
        
        fun getDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.buildDocumentationDefinition(element, builder)
            }
        }
    }
}