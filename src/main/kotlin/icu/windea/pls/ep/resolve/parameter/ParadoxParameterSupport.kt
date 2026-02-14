@file:Suppress("unused")

package icu.windea.pls.ep.resolve.parameter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.util.builders.DocumentationBuilder
import icu.windea.pls.core.util.KeyRegistryWithSync
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.ParadoxParameterInfo
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 提供对脚本参数的支持。
 *
 * @see ParadoxParameterElement
 */
interface ParadoxParameterSupport {
    fun isContext(element: ParadoxDefinitionElement): Boolean

    fun findContext(element: PsiElement): ParadoxDefinitionElement?

    fun getContextKeyFromContext(context: ParadoxDefinitionElement): String?

    fun getContextInfo(element: ParadoxDefinitionElement): ParadoxParameterContextInfo?

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
     *
     * @return 此扩展点是否适用。
     */
    fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean

    /**
     * @param onlyMostRelevant 是否只遍历最相关的那个上下文。
     *
     * @return 此扩展点是否适用。
     */
    fun processContextReference(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean

    fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker? = null

    /**
     * 构建参数的快速文档中的定义部分。
     *
     * @return 此扩展点是否适用。
     */
    fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean = false

    object Keys : KeyRegistryWithSync()

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxParameterSupport>("icu.windea.pls.parameterSupport")
    }
}
