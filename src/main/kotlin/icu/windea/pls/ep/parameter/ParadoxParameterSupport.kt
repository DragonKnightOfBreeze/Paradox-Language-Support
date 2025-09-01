@file:Suppress("unused")

package icu.windea.pls.ep.parameter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.documentation.DocumentationBuilder
import icu.windea.pls.core.util.SyncedKeyRegistry
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.elementInfo.ParadoxParameterInfo
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 提供对脚本参数的支持。
 *
 * @see icu.windea.pls.lang.psi.mock.ParadoxParameterElement
 */
interface ParadoxParameterSupport {
    fun isContext(element: ParadoxScriptDefinitionElement): Boolean

    fun findContext(element: PsiElement): ParadoxScriptDefinitionElement?

    fun getContextKeyFromContext(context: ParadoxScriptDefinitionElement): String?

    fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo?

    /**
     * 向上查找参数的上下文引用信息。
     *
     * @param element 开始查找的位置。
     * @param from 从哪里向上查找。
     * @param extraArgs 对于每个实现需要的额外参数可能是不同的。
     * @see icu.windea.pls.lang.codeInsight.parameterInfo.ParadoxParameterInfoHandler
     */
    fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo?

    fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement?

    fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement?

    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement?

    /**
     * @param onlyMostRelevant 是否只遍历最相关的那个上下文。
     * @return 此扩展点是否适用。
     */
    fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean

    /**
     * @param onlyMostRelevant 是否只遍历最相关的那个上下文。
     * @return 此扩展点是否适用。
     */
    fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean

    fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker? = null

    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此扩展点是否适用。
     */
    fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxParameterSupport>("icu.windea.pls.parameterSupport")

        fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
            return EP_NAME.extensionList.any { it.isContext(element) }
        }

        fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.findContext(element) }
        }

        fun getContextKeyFromContext(element: ParadoxScriptDefinitionElement): String? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.getContextKeyFromContext(element) }
        }

        fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getContextInfo(element)
            }
        }

        fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getContextReferenceInfo(element, from, *extraArgs)
                    ?.also { it.support = ep }
            }
        }

        fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveParameter(element)
                    ?.also { it.support = ep }
            }
        }

        fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveConditionParameter(element)
                    ?.also { it.support = ep }
            }
        }

        fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.resolveArgument(element, rangeInElement, config)
                    ?.also { it.support = ep }
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

        fun getDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean {
            return EP_NAME.extensionList.any { ep ->
                ep.buildDocumentationDefinition(parameterElement, builder)
            }
        }
    }

    object Keys : SyncedKeyRegistry()
}
