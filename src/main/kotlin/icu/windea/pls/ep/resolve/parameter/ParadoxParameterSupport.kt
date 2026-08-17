@file:Suppress("unused")

package icu.windea.pls.ep.resolve.parameter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.ParadoxParameterInfo
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 提供对脚本参数的支持。
 *
 * @see ParadoxParameterInfo
 * @see ParadoxParameterLightElement
 */
interface ParadoxParameterSupport {
    fun isContext(element: ParadoxDefinitionElement): Boolean

    fun findContext(element: PsiElement): ParadoxDefinitionElement?

    fun resolveParameter(element: ParadoxParameter): ParadoxParameterLightElement?

    fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterLightElement?

    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterLightElement?

    /**
     * 根据指定的 [element]，遍历所有作为参数上下文的目标（[ParadoxDefinitionElement]）。这里的参数使用读访问。
     * 如果 [onlyMostRelevant] 为 `true`，则仅遍历最相关的那个。
     * 如果返回 `false`，则会终止遍历，因而也会终止遍历 EP。
     */
    fun processContext(element: ParadoxParameterLightElement, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean

    /**
     * 根据指定的 [element] 和 [contextReferenceInfo]，遍历所有作为参数上下文引用的目标（[ParadoxDefinitionElement]）。这里的参数使用写访问。
     * 如果 [onlyMostRelevant] 为 `true`，则仅遍历最相关的那个。
     * 如果返回 `false`，则会终止遍历，因而也会终止遍历 EP。
     */
    fun processContextReference(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean

    /**
     * 向上查找参数的上下文引用信息。
     *
     * @param element 开始查找的位置。
     * @param from 从哪里向上查找。
     * @param extraArgs 对于每个实现需要的额外参数可能是不同的。
     */
    fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo?

    fun getContextKeyFromContext(context: ParadoxDefinitionElement): String?

    fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker? = null

    object Keys : KeyRegistry()

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxParameterSupport>("icu.windea.pls.parameterSupport")
    }
}
